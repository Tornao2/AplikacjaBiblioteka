package com.biblioteka.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
    private Long id;
    @NotNull(message = "Identyfikator książki jest wymagany")
    private Long bookId;
    @NotNull(message = "Identyfikator użytkownika jest wymagany")
    private Long userId;
    private String bookTitle;
    private String bookAuthor;
    private String userFullName;
    private String userEmail;
    @NotNull(message = "Data wypożyczenia jest wymagana")
    @PastOrPresent(message = "Data wypożyczenia nie może być z przyszłości")
    private LocalDate loanDate;
    @NotNull(message = "Termin zwrotu jest wymagany")
    @FutureOrPresent(message = "Termin zwrotu nie może być z przeszłości")
    private LocalDate dueDate;
    @PastOrPresent(message = "Data zwrotu nie może być z przyszłości")
    private LocalDate returnDate;
    private boolean extended;
    @Min(value = 0, message = "Kara za przetrzymanie nie może być ujemna")
    private Long overduePay;
}