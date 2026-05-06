package com.biblioteka.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SystemSettingsTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Poprawne ustawienia")
    void validDto() {
        SystemSettingsDTO dto = createValid();
        assertTrue(validator.validate(dto).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 366})
    @DisplayName("Niepoprawny czas wypożyczenia")
    void invalidLoanDuration(int duration) {
        SystemSettingsDTO dto = createValid();
        dto.setMaxLoanDuration(duration);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 101})
    @DisplayName("Niepoprawny limit wypożyczeń")
    void invalidLoanLimit(int limit) {
        SystemSettingsDTO dto = createValid();
        dto.setUserLoanLimit(limit);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 100.01})
    @DisplayName("Niepoprawna stawka kary")
    void invalidPenaltyRate(double rate) {
        SystemSettingsDTO dto = createValid();
        dto.setDailyPenaltyRate(rate);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Zły format precyzji kary")
    void invalidPenaltyFormat() {
        SystemSettingsDTO dto = createValid();
        dto.setDailyPenaltyRate(0.123);
        Set<ConstraintViolation<SystemSettingsDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("max 0.0x")));
    }

    private SystemSettingsDTO createValid() {
        return SystemSettingsDTO.builder()
                .maxLoanDuration(30)
                .userLoanLimit(5)
                .dailyPenaltyRate(0.50)
                .build();
    }
}