package com.biblioteka.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogDTO {
    @NotNull(message = "Data i czas logu są wymagane")
    @PastOrPresent(message = "Data logu nie może być z przyszłości")
    private LocalDateTime timestamp;
    @NotBlank(message = "Nazwa użytkownika nie może być pusta")
    @Size(min = 2, max = 50, message = "Nazwa użytkownika musi mieć od 2 do 50 znaków")
    private String user;
    @NotBlank(message = "Akcja nie może być pusta")
    @Size(min = 3, max = 100, message = "Nazwa akcji musi mieć od 3 do 100 znaków")
    private String action;
    @NotBlank(message = "Szczegóły logu są wymagane")
    @Size(max = 4000, message = "Szczegóły logu nie mogą przekraczać 4000 znaków")
    private String details;
    @NotBlank(message = "Poziom ważności jest wymagany")
    @Pattern(regexp = "^(INFO|WARNING|CRITICAL|ERROR)$", message = "Nieprawidłowy poziom ważności.")
    private String severity;
}