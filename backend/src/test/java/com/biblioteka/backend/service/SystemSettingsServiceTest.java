package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.SystemSettingsDTO;
import com.biblioteka.backend.entity.SystemSettings;
import com.biblioteka.backend.repository.SystemSettingsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemSettingsServiceTest {

    @Mock private SystemSettingsRepository settingsRepository;
    @Mock private SystemLogService logService;
    @InjectMocks private SystemSettingsService settingsService;

    @Test
    @DisplayName("Pobieranie: tworzy domyślne gdy brak w bazie")
    void shouldCreateDefaultIfNotFound() {
        when(settingsRepository.findById(1L)).thenReturn(Optional.empty());
        when(settingsRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        SystemSettingsDTO result = settingsService.getSettings();
        assertEquals(30, result.getMaxLoanDuration());
        assertEquals(5, result.getUserLoanLimit());
        verify(logService).addLog(eq("SYSTEM"), eq("SETTINGS_INIT"), anyString(), eq("INFO"));
    }

    @Test
    @DisplayName("Aktualizacja: zapisuje nowe wartości i loguje zmianę")
    void shouldUpdateSettingsAndLogDetails() {
        SystemSettings existing = SystemSettings.builder()
                .id(1L).maxLoanDuration(14).userLoanLimit(3).dailyPenaltyRate(0.20)
                .build();
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(settingsRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        SystemSettingsDTO dto = SystemSettingsDTO.builder()
                .maxLoanDuration(60).userLoanLimit(10).dailyPenaltyRate(1.0)
                .build();
        SystemSettingsDTO result = settingsService.updateSettings(dto);
        assertEquals(60, result.getMaxLoanDuration());
        verify(logService).addLog(any(), eq("SETTINGS_UPDATE"), contains("Nowe:"), eq("INFO"));
        verify(settingsRepository).save(argThat(s -> s.getMaxLoanDuration() == 60));
    }
}