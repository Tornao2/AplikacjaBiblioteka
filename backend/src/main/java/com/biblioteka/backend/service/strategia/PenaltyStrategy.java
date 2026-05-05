package com.biblioteka.backend.service.strategia;

import java.time.LocalDate;

public interface PenaltyStrategy {
    Long calculate(LocalDate dueDate, LocalDate returnDate, Long dailyRate);
    String getName();
}