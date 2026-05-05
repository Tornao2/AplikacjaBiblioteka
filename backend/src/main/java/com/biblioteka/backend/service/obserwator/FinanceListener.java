package com.biblioteka.backend.service.obserwator;

import com.biblioteka.backend.dto.FinanceDTO;
import com.biblioteka.backend.entity.FinanceType;
import com.biblioteka.backend.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FinanceListener {
    private final FinanceService financeService;
    @EventListener
    public void onBookReturned(LoanReturnedEvent event) {
        if (event.penaltyAmount() > 0) {
            financeService.addEntry(FinanceDTO.builder()
                    .date(LocalDate.now())
                    .type(FinanceType.INCOME)
                    .amount(BigDecimal.valueOf(event.penaltyAmount()))
                    .description("Opłata za przetrzymanie: " + event.bookTitle() +
                            " (Wypożyczenie ID: " + event.loanId() + ")")
                    .build());
        }
    }
}