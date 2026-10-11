package com.library;

import com.library.dto.LoanDtos.Response;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.Reader;
import com.library.exception.ConflictException;
import com.library.exception.NotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.ReaderRepository;
import com.library.service.FineCalculator;
import com.library.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 11);

    @Mock LoanRepository loans;
    @Mock BookRepository books;
    @Mock ReaderRepository readers;

    LoanService service;
    Book book;
    Reader reader;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T00:00:00Z"), ZoneOffset.UTC);
        service = new LoanService(loans, books, readers, new FineCalculator(new BigDecimal("0.50")), clock, 14, 5);

        book = new Book();
        book.setId(1L);
        book.setTitle("Война и мир");
        book.setTotalCopies(2);
        book.setAvailableCopies(2);

        reader = new Reader();
        reader.setId(10L);
        reader.setFullName("Иван Иванов");

        when(books.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(readers.findById(10L)).thenReturn(Optional.of(reader));
        when(loans.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void issue_success_decrementsCopiesAndSetsDueDate() {
        Response r = service.issue(1L, 10L);
        assertEquals(1, book.getAvailableCopies());
        assertEquals(TODAY.plusDays(14), r.dueDate());
        assertEquals("ACTIVE", r.status());
    }

    @Test
    void issue_noCopies_conflict() {
        book.setAvailableCopies(0);
        assertThrows(ConflictException.class, () -> service.issue(1L, 10L));
    }

    @Test
    void issue_readerHasOverdue_conflict() {
        when(loans.existsByReaderIdAndReturnedAtIsNullAndDueDateBefore(10L, TODAY)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.issue(1L, 10L));
        assertEquals(2, book.getAvailableCopies());
    }

    @Test
    void issue_limitReached_conflict() {
        when(loans.countByReaderIdAndReturnedAtIsNull(10L)).thenReturn(5L);
        assertThrows(ConflictException.class, () -> service.issue(1L, 10L));
    }

    @Test
    void issue_bookNotFound() {
        when(books.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.issue(99L, 10L));
    }

    @Test
    void return_overdue_calculatesFineAndRestoresCopy() {
        book.setAvailableCopies(1);
        Loan loan = new Loan();
        loan.setId(5L);
        loan.setBook(book);
        loan.setReader(reader);
        loan.setIssuedAt(TODAY.minusDays(24));
        loan.setDueDate(TODAY.minusDays(10));
        when(loans.findByIdForUpdate(5L)).thenReturn(Optional.of(loan));

        Response r = service.returnBook(5L);

        assertEquals(2, book.getAvailableCopies());
        assertEquals(TODAY, loan.getReturnedAt());
        assertEquals("RETURNED", r.status());
        assertEquals(10, r.daysOverdue());
        assertEquals(0, new BigDecimal("5.00").compareTo(r.fine()));
    }

    @Test
    void return_twice_conflict() {
        Loan loan = new Loan();
        loan.setId(5L);
        loan.setBook(book);
        loan.setReader(reader);
        loan.setIssuedAt(TODAY.minusDays(3));
        loan.setDueDate(TODAY.plusDays(11));
        loan.setReturnedAt(TODAY.minusDays(1));
        when(loans.findByIdForUpdate(5L)).thenReturn(Optional.of(loan));
        assertThrows(ConflictException.class, () -> service.returnBook(5L));
    }
}
