package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.SystemSettingsDTO;
import com.biblioteka.backend.entity.SystemSettings;
import com.biblioteka.backend.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemSettingsService {
    private final SystemSettingsRepository settingsRepository;
    private static final Long SETTINGS_ID = 1L;

    @Transactional
    public SystemSettingsDTO getSettings() {
        SystemSettings settings = settingsRepository.findById(SETTINGS_ID)
                .orElseGet(this::createDefaultSettings);
        return mapToDTO(settings);
    }

    @Transactional
    public SystemSettingsDTO updateSettings(SystemSettingsDTO dto) {
        SystemSettings settings = settingsRepository.findById(SETTINGS_ID)
                .orElseGet(SystemSettings::new);
        settings.setId(SETTINGS_ID);
        settings.setMaxLoanDuration(dto.getMaxLoanDuration());
        settings.setUserLoanLimit(dto.getUserLoanLimit());
        settings.setDailyPenaltyRate(dto.getDailyPenaltyRate());

        SystemSettings saved = settingsRepository.save(settings);
        return mapToDTO(saved);
    }

    private SystemSettings createDefaultSettings() {
        SystemSettings defaultSettings = SystemSettings.builder()
                .id(SETTINGS_ID)
                .maxLoanDuration(30)
                .userLoanLimit(5)
                .dailyPenaltyRate(0.50)
                .build();
        return settingsRepository.save(defaultSettings);
    }

    private SystemSettingsDTO mapToDTO(SystemSettings entity) {
        return SystemSettingsDTO.builder()
                .maxLoanDuration(entity.getMaxLoanDuration())
                .userLoanLimit(entity.getUserLoanLimit())
                .dailyPenaltyRate(entity.getDailyPenaltyRate())
                .build();
    }
}