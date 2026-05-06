package com.biblioteka.backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SystemLogDTOTest {
    private Validator validator;
    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Poprawny DTO")
    void validDto() {
        SystemLogDTO dto = createValid();
        assertTrue(validator.validate(dto).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INFO", "WARNING", "ERROR", "CRITICAL"})
    @DisplayName("Poprawne poziomy ważności")
    void validSeverity(String level) {
        SystemLogDTO dto = createValid();
        dto.setSeverity(level);
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Błędny poziom ważności")
    void invalidSeverity() {
        SystemLogDTO dto = createValid();
        dto.setSeverity("DEBUG");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Data z przyszłości")
    void futureDate() {
        SystemLogDTO dto = createValid();
        dto.setTimestamp(LocalDateTime.now().plusDays(1));
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Przekroczenie limitu szczegółów")
    void detailsTooLong() {
        SystemLogDTO dto = createValid();
        dto.setDetails("a".repeat(4001));
        assertFalse(validator.validate(dto).isEmpty());
    }

    private SystemLogDTO createValid() {
        return SystemLogDTO.builder()
                .timestamp(LocalDateTime.now())
                .user("admin")
                .action("LOGIN")
                .details("Poprawne logowanie do systemu")
                .severity("INFO")
                .build();
    }
}