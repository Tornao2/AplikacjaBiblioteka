package com.biblioteka.backend.service;

import com.biblioteka.backend.entity.Loan;
import com.biblioteka.backend.entity.SystemSettings;
import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.entity.UserRole;
import com.biblioteka.backend.repository.LoanRepository;
import com.biblioteka.backend.repository.SystemSettingsRepository;
import com.biblioteka.backend.repository.UserRepository;
import com.biblioteka.backend.service.strategia.PenaltyContext;
import com.biblioteka.backend.service.strategia.PenaltyStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenaltyTaskServiceTest {

    @Mock private SystemSettingsRepository systemSettingsRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private UserRepository userRepository;
    @Mock private PenaltyContext penaltyContext;
    @Mock private PenaltyStrategy penaltyStrategy;

    @InjectMocks
    private PenaltyTaskService penaltyTaskService;

    @Test
    @DisplayName("Update: Poprawne naliczenie kary dla spóźnionego wypożyczenia")
    void shouldUpdatePenaltyForOverdueLoans() {
        SystemSettings settings = SystemSettings.builder().dailyPenaltyRate(0.5).build();
        Loan loan = Loan.builder().id(1L).userId(100L).dueDate(LocalDate.now().minusDays(5)).overduePay(0L).build();
        User user = User.builder().id(100L).role(UserRole.Czytelnik).build();
        when(systemSettingsRepository.findById(1L)).thenReturn(Optional.of(settings));
        when(loanRepository.findAllByReturnDateIsNullAndDueDateBefore(any(LocalDate.now().getClass())))
                .thenReturn(List.of(loan));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(penaltyContext.getStrategy(anyString())).thenReturn(penaltyStrategy);
        when(penaltyStrategy.calculate(any(), any(), eq(50L))).thenReturn(250L);
        penaltyTaskService.updatePenalties();
        assertEquals(250L, loan.getOverduePay());
        verify(loanRepository).save(loan);
    }

    @Test
    @DisplayName("Update: Użycie domyślnej stawki gdy brak ustawień w bazie")
    void shouldUseDefaultRateWhenSettingsNotFound() {
        when(systemSettingsRepository.findById(1L)).thenReturn(Optional.empty());
        Loan loan = Loan.builder().id(1L).userId(100L).dueDate(LocalDate.now().minusDays(2)).build();
        User user = User.builder().id(100L).role(UserRole.Czytelnik).build();
        when(loanRepository.findAllByReturnDateIsNullAndDueDateBefore(any())).thenReturn(List.of(loan));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(penaltyContext.getStrategy(anyString())).thenReturn(penaltyStrategy);
        penaltyTaskService.updatePenalties();
        verify(penaltyStrategy).calculate(any(), any(), eq(50L));
    }

    @Test
    @DisplayName("Update: Pominięcie gdy użytkownik nie istnieje")
    void shouldSkipWhenUserNotFound() {
        when(systemSettingsRepository.findById(1L)).thenReturn(Optional.empty());
        Loan loan = Loan.builder().id(1L).userId(999L).dueDate(LocalDate.now().minusDays(1)).build();
        when(loanRepository.findAllByReturnDateIsNullAndDueDateBefore(any())).thenReturn(List.of(loan));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        penaltyTaskService.updatePenalties();
        verify(penaltyContext, never()).getStrategy(anyString());
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update: Brak akcji gdy nie ma spóźnionych wypożyczeń")
    void shouldDoNothingWhenNoOverdueLoans() {
        when(systemSettingsRepository.findById(1L)).thenReturn(Optional.of(new SystemSettings()));
        when(loanRepository.findAllByReturnDateIsNullAndDueDateBefore(any())).thenReturn(List.of());
        penaltyTaskService.updatePenalties();
        verifyNoInteractions(userRepository);
        verify(loanRepository, never()).save(any());
    }
}