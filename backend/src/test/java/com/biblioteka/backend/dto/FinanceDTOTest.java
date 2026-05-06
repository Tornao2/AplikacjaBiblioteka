package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.FinanceType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FinanceDTOTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Powinien przejść, gdy FinanceDTO jest w pełni poprawne")
    void validFinanceDtoTest() {
        FinanceDTO dto = createBaseValidFinance();
        Set<ConstraintViolation<FinanceDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Lista błędów powinna być pusta");
    }

    @Test
    @DisplayName("Powinien odrzucić kwotę mniejszą niż 0.01")
    void invalidAmountTest() {
        FinanceDTO dto = createBaseValidFinance();
        dto.setAmount(new BigDecimal("0.00"));
        Set<ConstraintViolation<FinanceDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("musi być większa od zera")));
    }

    @Test
    @DisplayName("Powinien odrzucić datę z przyszłości")
    void futureDateTest() {
        FinanceDTO dto = createBaseValidFinance();
        dto.setDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<FinanceDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nie może być z przyszłości")));
    }

    @Test
    @DisplayName("Brak typu transakcji")
    void missingTypeTest() {
        FinanceDTO dto = createBaseValidFinance();
        dto.setType(null);
        Set<ConstraintViolation<FinanceDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("type")));
    }

    @Test
    @DisplayName("Powinien odrzucić zbyt długi opis")
    void tooLongDescriptionTest() {
        FinanceDTO dto = createBaseValidFinance();
        dto.setDescription("a".repeat(501));
        Set<ConstraintViolation<FinanceDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nie może przekraczać 500 znaków")));
    }

    private FinanceDTO createBaseValidFinance() {
        return FinanceDTO.builder()
                .date(LocalDate.now())
                .type(FinanceType.INCOME)
                .amount(new BigDecimal("100.00"))
                .description("Prawidłowy wpis")
                .build();
    }
}