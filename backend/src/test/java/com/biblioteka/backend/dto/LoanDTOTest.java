package com.biblioteka.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoanDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Powinien przejść, gdy LoanDTO jest w pełni poprawne")
    void validLoanDtoTest() {
        LoanDTO dto = createBaseValidLoan();
        Set<ConstraintViolation<LoanDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Lista błędów powinna być pusta");
    }

    @Test
    @DisplayName("Powinien odrzucić brak ID książki i użytkownika")
    void missingIdsTest() {
        LoanDTO dto = createBaseValidLoan();
        dto.setBookId(null);
        dto.setUserId(null);
        Set<ConstraintViolation<LoanDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Identyfikator książki jest wymagany")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Identyfikator użytkownika jest wymagany")));
    }

    @Test
    @DisplayName("Powinien odrzucić datę wypożyczenia z przyszłości")
    void futureLoanDateTest() {
        LoanDTO dto = createBaseValidLoan();
        dto.setLoanDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<LoanDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nie może być z przyszłości")));
    }

    @Test
    @DisplayName("Powinien odrzucić termin zwrotu z przeszłości")
    void pastDueDateTest() {
        LoanDTO dto = createBaseValidLoan();
        dto.setDueDate(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<LoanDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nie może być z przeszłości")));
    }

    @Test
    @DisplayName("Powinien odrzucić ujemną karę za przetrzymanie")
    void negativeOverduePayTest() {
        LoanDTO dto = createBaseValidLoan();
        dto.setOverduePay(-10L);
        Set<ConstraintViolation<LoanDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nie może być ujemna")));
    }

    private LoanDTO createBaseValidLoan() {
        return LoanDTO.builder()
                .bookId(1L)
                .userId(1L)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .overduePay(0L)
                .extended(false)
                .build();
    }
}