package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.BookStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long id;
    @NotBlank(message = "Tytuł nie może być pusty")
    @Size(max = 255, message = "Tytuł jest za długi")
    private String title;
    @NotBlank(message = "Autor nie może być pusty")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Imie lub nazwisko autora zawiera niedozwolone znaki")
    private String author;
    @NotBlank(message = "Numer ISBN jest wymagany")
    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", message = "Nieprawidłowy format ISBN ")
    private String isbn;
    @NotBlank(message = "Kategoria musi zostać wybrana")
    private String category;
    private BookStatus status;
    @Size(min = 10, max = 2000, message = "Opis musi mieć od 10 do 2000 znaków")
    private String description;
    @NotNull(message = "Rok wydania jest wymagany")
    @Max(value = 2026, message = "Rok wydania nie może być z przyszłości")
    private Integer releaseYear;
}