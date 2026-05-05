package com.biblioteka.backend.service.strategia;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class StandardPenaltyStrategy implements PenaltyStrategy {
    @Override
    public Long calculate(LocalDate dueDate, LocalDate returnDate, Long dailyRate) {
        if (returnDate.isBefore(dueDate) || returnDate.isEqual(dueDate)) {
            return 0L;
        }
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
        return dailyRate*daysOverdue;
    }

    @Override
    public String getName() { return "STANDARD"; }
}