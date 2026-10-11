package com.library.service;

import com.library.dto.BookDtos.Request;
import com.library.dto.BookDtos.Response;
import com.library.entity.Book;
import com.library.exception.ConflictException;
import com.library.exception.NotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository books;
    private final LoanRepository loans;

    public BookService(BookRepository books, LoanRepository loans) {
        this.books = books;
        this.loans = loans;
    }

    @Transactional(readOnly = true)
    public Page<Response> search(String q, boolean onlyAvailable, Pageable pageable) {
        return books.search(SearchUtil.pattern(q), onlyAvailable, pageable).map(Response::from);
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        return Response.from(find(id));
    }

    @Transactional
    public Response create(Request r) {
        Book b = new Book();
        apply(b, r);
        b.setAvailableCopies(r.totalCopies());
        return Response.from(books.saveAndFlush(b));
    }

    @Transactional
    public Response update(Long id, Request r) {
        Book b = books.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
        int borrowed = b.getTotalCopies() - b.getAvailableCopies();
        if (r.totalCopies() < borrowed) {
            throw new ConflictException("Общее количество не может быть меньше числа выданных экземпляров (" + borrowed + ")");
        }
        apply(b, r);
        b.setAvailableCopies(r.totalCopies() - borrowed);
        return Response.from(books.saveAndFlush(b));
    }

    @Transactional
    public void delete(Long id) {
        Book b = find(id);
        if (loans.existsByBookId(id)) {
            throw new ConflictException("Нельзя удалить книгу: по ней есть записи о выдаче");
        }
        books.delete(b);
    }

    private void apply(Book b, Request r) {
        b.setTitle(r.title().trim());
        b.setAuthor(r.author().trim());
        b.setIsbn(r.isbn() == null || r.isbn().isBlank() ? null : r.isbn().trim());
        b.setPublicationYear(r.publicationYear());
        b.setTotalCopies(r.totalCopies());
    }

    private Book find(Long id) {
        return books.findById(id).orElseThrow(() -> notFound(id));
    }

    private NotFoundException notFound(Long id) {
        return new NotFoundException("Книга с id=" + id + " не найдена");
    }
}
