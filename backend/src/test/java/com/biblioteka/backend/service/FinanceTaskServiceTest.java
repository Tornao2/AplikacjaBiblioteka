package com.biblioteka.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceTaskServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @InjectMocks private FinanceTaskService financeTaskService;

    @Test
    @DisplayName("Salaries: sukces - poprawne księgowanie")
    void processSalariesSuccess() {
        BigDecimal total = new BigDecimal("15000.50");
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class))).thenReturn(total);
        financeTaskService.processMonthlySalaries();
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(jdbcTemplate).update(anyString(), amountCaptor.capture(), anyString());
        assertEquals(total, amountCaptor.getValue());
    }

    @Test
    @DisplayName("Salaries: pominięcie gdy suma wynosi zero")
    void shouldSkipWhenSumIsZero() {
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class))).thenReturn(BigDecimal.ZERO);
        financeTaskService.processMonthlySalaries();
        verify(jdbcTemplate, never()).update(anyString(), any(), any());
    }

    @Test
    @DisplayName("Salaries: pominięcie gdy brak danych (null)")
    void shouldSkipWhenSumIsNull() {
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class))).thenReturn(null);
        financeTaskService.processMonthlySalaries();
        verify(jdbcTemplate, never()).update(anyString(), any(), any());
    }

    @Test
    @DisplayName("Salaries: błąd bazy przy pobieraniu sumy")
    void shouldThrowWhenDatabaseError() {
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class)))
                .thenThrow(new org.springframework.dao.DataAccessException("DB Error") {});
        assertThrows(org.springframework.dao.DataAccessException.class,
                () -> financeTaskService.processMonthlySalaries());
        verify(jdbcTemplate, never()).update(anyString(), any(), any());
    }
}