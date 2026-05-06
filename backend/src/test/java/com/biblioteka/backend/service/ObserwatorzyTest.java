package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.FinanceDTO;
import com.biblioteka.backend.entity.FinanceType;
import com.biblioteka.backend.service.obserwator.FinanceListener;
import com.biblioteka.backend.service.obserwator.LoanReturnedEvent;
import com.biblioteka.backend.service.obserwator.NotificationListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObserwatorzyTest {
    @Mock private FinanceService financeService;
    @Mock private SystemLogService systemLogService;

    @InjectMocks private FinanceListener financeListener;
    @InjectMocks private NotificationListener notificationListener;

    @Test
    @DisplayName("FinanceListener: tworzy wpis gdy jest kara")
    void financeListenerShouldAddEntryWhenPenaltyExists() {
        LoanReturnedEvent event = new LoanReturnedEvent(1L, 10L, 100L, 15.50, "Java Core", "admin");
        financeListener.onBookReturned(event);
        ArgumentCaptor<FinanceDTO> captor = ArgumentCaptor.forClass(FinanceDTO.class);
        verify(financeService).addEntry(captor.capture());
        FinanceDTO dto = captor.getValue();
        assertEquals(FinanceType.INCOME, dto.getType());
        assertEquals(BigDecimal.valueOf(15.50), dto.getAmount());
        assertTrue(dto.getDescription().contains("Java Core"));
    }

    @Test
    @DisplayName("FinanceListener: ignoruje zdarzenie gdy kara = 0")
    void financeListenerShouldIgnoreZeroPenalty() {
        LoanReturnedEvent event = new LoanReturnedEvent(1L, 10L, 100L, 0.0, "Java Core", "admin");
        financeListener.onBookReturned(event);
        verify(financeService, never()).addEntry(any());
    }

    @Test
    @DisplayName("NotificationListener: zawsze dodaje log po zwrocie")
    void notificationListenerShouldAlwaysLog() {
        LoanReturnedEvent event = new LoanReturnedEvent(1L, 10L, 100L, 0.0, "Java Core", "user123");
        notificationListener.onBookReturned(event);
        verify(systemLogService).addLog(
                eq("user123"),
                eq("LOAN_RETURN"),
                contains("ID: 10"),
                eq("INFO")
        );
    }
}