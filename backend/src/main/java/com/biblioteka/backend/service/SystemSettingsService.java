package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.SystemSettingsDTO;
import com.biblioteka.backend.entity.SystemSettings;
import com.biblioteka.backend.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemSettingsService {
    private final SystemSettingsRepository settingsRepository;
    private static final Long SETTINGS_ID = 1L;
    private final SystemLogService logService;

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
        String oldValues = String.format("Czas wypożyczenia: %d dni, Limit: %d książek, Kara: %.2f zł",
                settings.getMaxLoanDuration(), settings.getUserLoanLimit(), settings.getDailyPenaltyRate());

        settings.setId(SETTINGS_ID);
        settings.setMaxLoanDuration(dto.getMaxLoanDuration());
        settings.setUserLoanLimit(dto.getUserLoanLimit());
        settings.setDailyPenaltyRate(dto.getDailyPenaltyRate());
        SystemSettings saved = settingsRepository.save(settings);
        String newValues = String.format("Czas wypożyczenia: %d dni, Limit: %d książek, Kara: %.2f zł",
                saved.getMaxLoanDuration(), saved.getUserLoanLimit(), saved.getDailyPenaltyRate());
        String username = getCurrentUsername();
        logService.addLog(username, "SETTINGS_UPDATE",
                "Zmieniono ustawienia systemu. Stare: [" + oldValues + "] Nowe: [" + newValues + "]",
                "INFO");
        return mapToDTO(saved);
    }

    private SystemSettings createDefaultSettings() {
        SystemSettings defaultSettings = SystemSettings.builder()
                .id(SETTINGS_ID)
                .maxLoanDuration(30)
                .userLoanLimit(5)
                .dailyPenaltyRate(0.50)
                .build();
        logService.addLog("SYSTEM", "SETTINGS_INIT",
                "Utworzono domyślne ustawienia systemu (30 dni, limit 5 książek, kara 0.50 zł).",
                "INFO");
        return settingsRepository.save(defaultSettings);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private SystemSettingsDTO mapToDTO(SystemSettings entity) {
        return SystemSettingsDTO.builder()
                .maxLoanDuration(entity.getMaxLoanDuration())
                .userLoanLimit(entity.getUserLoanLimit())
                .dailyPenaltyRate(entity.getDailyPenaltyRate())
                .build();
    }
}