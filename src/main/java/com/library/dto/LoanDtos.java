package com.library.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class LoanDtos {
    private LoanDtos() {}

    public enum Filter { ALL, ACTIVE, OVERDUE, RETURNED }

    public record IssueRequest(@NotNull Long bookId, @NotNull Long readerId) {}

    public record Response(Long id, Long bookId, String bookTitle, Long readerId, String readerName,
                           LocalDate issuedAt, LocalDate dueDate, LocalDate returnedAt,
                           String status, long daysOverdue, BigDecimal fine) {}
}
