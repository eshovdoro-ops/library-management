package com.library.service;

import com.library.dto.DebtReport;
import com.library.dto.DebtReport.Row;
import com.library.entity.Loan;
import com.library.entity.Reader;
import com.library.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final LoanRepository loans;
    private final FineCalculator fines;
    private final Clock clock;

    public ReportService(LoanRepository loans, FineCalculator fines, Clock clock) {
        this.loans = loans;
        this.fines = fines;
        this.clock = clock;
    }

    /** Аналитический отчёт: просроченные выдачи, сгруппированные по читателям, со штрафами. */
    @Transactional(readOnly = true)
    public DebtReport debts() {
        LocalDate today = LocalDate.now(clock);
        List<Loan> overdue = loans.findAllOverdue(today);

        Map<Long, List<Loan>> byReader = new LinkedHashMap<>();
        for (Loan l : overdue) {
            byReader.computeIfAbsent(l.getReader().getId(), k -> new java.util.ArrayList<>()).add(l);
        }

        List<Row> rows = byReader.values().stream().map(list -> {
            Reader r = list.get(0).getReader();
            long max = 0;
            BigDecimal total = BigDecimal.ZERO;
            for (Loan l : list) {
                long days = fines.daysOverdue(l.getDueDate(), today);
                max = Math.max(max, days);
                total = total.add(fines.fine(days));
            }
            return new Row(r.getId(), r.getFullName(), r.getEmail(), r.getPhone(), list.size(), max, total);
        }).sorted(Comparator.comparing(Row::totalFine).reversed()).toList();

        BigDecimal totalFine = rows.stream().map(Row::totalFine).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DebtReport(today, fines.getFinePerDay(), overdue.size(), rows.size(), totalFine, rows);
    }
}
