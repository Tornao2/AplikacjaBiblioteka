package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.FinanceDTO;
import com.biblioteka.backend.entity.Finance;
import com.biblioteka.backend.entity.FinanceType;
import com.biblioteka.backend.repository.FinanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {
    @Mock private FinanceRepository financeRepository;
    @Mock private SystemLogService logService;
    @InjectMocks private FinanceService financeService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Add: błąd gdy brak typu")
    void addEntryMissingType() {
        FinanceDTO dto = FinanceDTO.builder().amount(new BigDecimal("10")).build();
        Exception ex = assertThrows(RuntimeException.class, () -> financeService.addEntry(dto));
        assertEquals("Typ transakcji (INCOME/EXPENSE) jest wymagany.", ex.getMessage());
        verifyNoInteractions(financeRepository);
    }

    @Test
    @DisplayName("Add: błąd gdy kwota <= 0")
    void addEntryInvalidAmount() {
        FinanceDTO dto = FinanceDTO.builder()
                .type(FinanceType.INCOME)
                .amount(BigDecimal.ZERO)
                .build();
        Exception ex = assertThrows(RuntimeException.class, () -> financeService.addEntry(dto));
        assertEquals("Kwota transakcji musi być większa od zera.", ex.getMessage());
    }

    @Test
    @DisplayName("Add: błąd gdy data z przyszłości")
    void addEntryFutureDate() {
        FinanceDTO dto = FinanceDTO.builder()
                .type(FinanceType.INCOME)
                .amount(new BigDecimal("100"))
                .date(LocalDate.now().plusDays(1))
                .build();
        Exception ex = assertThrows(RuntimeException.class, () -> financeService.addEntry(dto));
        assertEquals("Data transakcji nie może być z przyszłości.", ex.getMessage());
    }

    @Test
    @DisplayName("Update: błąd gdy ID nie istnieje")
    void updateEntryNotFound() {
        Long id = 99L;
        when(financeRepository.findById(id)).thenReturn(Optional.empty());
        FinanceDTO dto = FinanceDTO.builder().build();
        assertThrows(RuntimeException.class, () -> financeService.updateEntry(id, dto));
    }

    @Test
    @DisplayName("Update: błąd gdy nowa kwota jest ujemna")
    void updateEntryInvalidAmount() {
        Long id = 1L;
        Finance existing = Finance.builder().id(id).build();
        FinanceDTO updateDto = FinanceDTO.builder()
                .type(FinanceType.EXPENSE)
                .amount(new BigDecimal("-5.00"))
                .build();
        when(financeRepository.findById(id)).thenReturn(Optional.of(existing));
        assertThrows(RuntimeException.class, () -> financeService.updateEntry(id, updateDto));
    }

    @Test
    @DisplayName("Delete: błąd gdy ID nie istnieje")
    void deleteEntryNotFound() {
        Long id = 99L;
        when(financeRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> financeService.deleteEntry(id));
        verify(financeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete: sukces i logowanie SYSTEM")
    void deleteEntrySuccess() {
        Long id = 1L;
        Finance finance = Finance.builder().id(id).description("Test").build();
        when(financeRepository.findById(id)).thenReturn(Optional.of(finance));
        financeService.deleteEntry(id);
        verify(financeRepository).delete(finance);
        verify(logService).addLog(eq("SYSTEM"), eq("FINANCE_DELETE"), anyString(), eq("INFO"));
    }
}