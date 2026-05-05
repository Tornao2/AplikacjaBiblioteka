package com.biblioteka.backend.service.obserwator;

public record LoanReturnedEvent(
        Long loanId,
        Long bookId,
        Long userId,
        Double penaltyAmount,
        String bookTitle,
        String username
) {}