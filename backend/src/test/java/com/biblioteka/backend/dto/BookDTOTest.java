package com.biblioteka.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookDTOTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Powinien przejść, gdy BookDTO jest w pełni poprawne")
    void validBookDtoTest() {
        BookDTO dto = createBaseValidBook();
        Set<ConstraintViolation<BookDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Lista błędów powinna być pusta");
    }

    @ParameterizedTest
    @ValueSource(strings = {"9788375906257", "837590625X", "9791234567890"})
    @DisplayName("Powinien akceptować poprawne formaty ISBN (10 i 13 cyfr)")
    void validIsbnTest(String isbn) {
        BookDTO dto = createBaseValidBook();
        dto.setIsbn(isbn);
        Set<ConstraintViolation<BookDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "ISBN " + isbn + " powinien być uznany za poprawny");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "9788375906257123", "ABC1234567", "978-83-7590"})
    @DisplayName("Powinien odrzucać nieprawidłowe formaty ISBN")
    void invalidIsbnTest(String isbn) {
        BookDTO dto = createBaseValidBook();
        dto.setIsbn(isbn);
        Set<ConstraintViolation<BookDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "ISBN " + isbn + " powinien zostać odrzucony");
    }

    @ParameterizedTest
    @ValueSource(strings = {"J.K. Rowling", "Jean-Pierre D'Arras", "Maria Skłodowska-Curie", "Janusz A. Zajdel"})
    @DisplayName("Powinien akceptować złożone nazwiska autorów")
    void validAuthorTest(String author) {
        BookDTO dto = createBaseValidBook();
        dto.setAuthor(author);
        Set<ConstraintViolation<BookDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Autor " + author + " powinien być uznany za poprawnego");
    }

    @Test
    @DisplayName("Powinien odrzucić rok wydania z przyszłości")
    void futureReleaseYearTest() {
        BookDTO dto = createBaseValidBook();
        dto.setReleaseYear(2150);
        Set<ConstraintViolation<BookDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nie może być z przyszłości")),
                "Powinien pojawić się błąd dotyczący daty z przyszłości");
    }

    private BookDTO createBaseValidBook() {
        return BookDTO.builder()
                .title("Testowy Tytuł")
                .author("Testowy Autor")
                .isbn("9788375906257")
                .category("Fantasy")
                .releaseYear(2020)
                .build();
    }
}