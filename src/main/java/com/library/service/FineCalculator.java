package com.library.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class FineCalculator {

    private final BigDecimal finePerDay;

    public FineCalculator(@Value("${app.loan.fine-per-day}") BigDecimal finePerDay) {
        this.finePerDay = finePerDay;
    }

    public long daysOverdue(LocalDate dueDate, LocalDate endDate) {
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, endDate));
    }

    public BigDecimal fine(long daysOverdue) {
        return finePerDay.multiply(BigDecimal.valueOf(daysOverdue)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getFinePerDay() { return finePerDay; }
}
