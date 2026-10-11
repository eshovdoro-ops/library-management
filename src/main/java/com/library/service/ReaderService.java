package com.library.service;

import com.library.dto.ReaderDtos.Request;
import com.library.dto.ReaderDtos.Response;
import com.library.entity.Reader;
import com.library.exception.ConflictException;
import com.library.exception.NotFoundException;
import com.library.repository.LoanRepository;
import com.library.repository.ReaderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ReaderService {

    private final ReaderRepository readers;
    private final LoanRepository loans;

    public ReaderService(ReaderRepository readers, LoanRepository loans) {
        this.readers = readers;
        this.loans = loans;
    }

    @Transactional(readOnly = true)
    public Page<Response> search(String q, Pageable pageable) {
        return readers.search(SearchUtil.pattern(q), pageable).map(Response::from);
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        return Response.from(find(id));
    }

    @Transactional
    public Response create(Request r) {
        Reader reader = new Reader();
        apply(reader, r);
        return Response.from(readers.saveAndFlush(reader));
    }

    @Transactional
    public Response update(Long id, Request r) {
        Reader reader = find(id);
        apply(reader, r);
        return Response.from(readers.saveAndFlush(reader));
    }

    @Transactional
    public void delete(Long id) {
        Reader reader = find(id);
        if (loans.existsByReaderId(id)) {
            throw new ConflictException("Нельзя удалить читателя: по нему есть записи о выдаче");
        }
        readers.delete(reader);
    }

    private void apply(Reader reader, Request r) {
        reader.setFullName(r.fullName().trim());
        reader.setEmail(r.email().trim().toLowerCase(Locale.ROOT));
        reader.setPhone(r.phone() == null || r.phone().isBlank() ? null : r.phone().trim());
    }

    private Reader find(Long id) {
        return readers.findById(id).orElseThrow(() -> new NotFoundException("Читатель с id=" + id + " не найден"));
    }
}
