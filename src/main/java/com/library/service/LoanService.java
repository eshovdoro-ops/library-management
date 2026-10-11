package com.library.service;

import com.library.dto.LoanDtos.Filter;
import com.library.dto.LoanDtos.Response;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.Reader;
import com.library.exception.ConflictException;
import com.library.exception.NotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.ReaderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class LoanService {

    private final LoanRepository loans;
    private final BookRepository books;
    private final ReaderRepository readers;
    private final FineCalculator fines;
    private final Clock clock;
    private final int loanDays;
    private final int maxActive;

    public LoanService(LoanRepository loans, BookRepository books, ReaderRepository readers,
                       FineCalculator fines, Clock clock,
                       @Value("${app.loan.days}") int loanDays,
                       @Value("${app.loan.max-active}") int maxActive) {
        this.loans = loans;
        this.books = books;
        this.readers = readers;
        this.fines = fines;
        this.clock = clock;
        this.loanDays = loanDays;
        this.maxActive = maxActive;
    }

    /** Выдача книги. Строка книги блокируется (SELECT ... FOR UPDATE), чтобы не выдать последний экземпляр дважды. */
    @Transactional
    public Response issue(Long bookId, Long readerId) {
        Book book = books.findByIdForUpdate(bookId)
                .orElseThrow(() -> new NotFoundException("Книга с id=" + bookId + " не найдена"));
        Reader reader = readers.findById(readerId)
                .orElseThrow(() -> new NotFoundException("Читатель с id=" + readerId + " не найден"));
        LocalDate today = LocalDate.now(clock);

        if (book.getAvailableCopies() <= 0) {
            throw new ConflictException("Свободных экземпляров книги нет");
        }
        if (loans.existsByReaderIdAndReturnedAtIsNullAndDueDateBefore(readerId, today)) {
            throw new ConflictException("У читателя есть просроченные книги — выдача невозможна");
        }
        if (loans.countByReaderIdAndReturnedAtIsNull(readerId) >= maxActive) {
            throw new ConflictException("Достигнут лимит выданных книг: " + maxActive);
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setReader(reader);
        loan.setIssuedAt(today);
        loan.setDueDate(today.plusDays(loanDays));
        return toResponse(loans.save(loan));
    }

    @Transactional
    public Response returnBook(Long loanId) {
        Loan loan = loans.findByIdForUpdate(loanId)
                .orElseThrow(() -> new NotFoundException("Выдача с id=" + loanId + " не найдена"));
        if (loan.getReturnedAt() != null) {
            throw new ConflictException("Книга по этой выдаче уже возвращена");
        }
        Book book = books.findByIdForUpdate(loan.getBook().getId())
                .orElseThrow(() -> new NotFoundException("Книга не найдена"));
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        loan.setReturnedAt(LocalDate.now(clock));
        return toResponse(loan);
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        return toResponse(loans.findWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Выдача с id=" + id + " не найдена")));
    }

    @Transactional(readOnly = true)
    public Page<Response> list(Filter filter, Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        Page<Loan> page = switch (filter) {
            case ACTIVE -> loans.findActive(pageable);
            case OVERDUE -> loans.findOverdue(today, pageable);
            case RETURNED -> loans.findReturned(pageable);
            case ALL -> loans.findAll(pageable);
        };
        return page.map(this::toResponse);
    }

    Response toResponse(Loan l) {
        LocalDate end = l.getReturnedAt() != null ? l.getReturnedAt() : LocalDate.now(clock);
        long overdue = fines.daysOverdue(l.getDueDate(), end);
        String status = l.getReturnedAt() != null ? "RETURNED" : (overdue > 0 ? "OVERDUE" : "ACTIVE");
        return new Response(l.getId(), l.getBook().getId(), l.getBook().getTitle(),
                l.getReader().getId(), l.getReader().getFullName(),
                l.getIssuedAt(), l.getDueDate(), l.getReturnedAt(),
                status, overdue, fines.fine(overdue));
    }
}
