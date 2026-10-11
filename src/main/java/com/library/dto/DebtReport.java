package com.library.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Аналитический отчёт «Задолженности читателей». */
public record DebtReport(LocalDate generatedAt, BigDecimal finePerDay,
                         int overdueLoans, int debtors, BigDecimal totalFine, List<Row> rows) {

    public record Row(Long readerId, String fullName, String email, String phone,
                      int overdueLoans, long maxDaysOverdue, BigDecimal totalFine) {}
}
