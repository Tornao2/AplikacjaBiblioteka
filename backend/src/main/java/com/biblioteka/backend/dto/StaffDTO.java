package com.biblioteka.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {
    private Long id;
    @NotNull(message = "Dane użytkownika są wymagane")
    @Valid
    private UserDTO user;
    @NotBlank(message = "Numer telefonu jest wymagany")
    @Pattern(regexp = "^(\\+48|0)?\\d{9}$", message = "Numer telefonu musi składać się z 9 cyfr")
    private String phoneNumber;
    @NotNull(message = "Data zatrudnienia jest wymagana")
    @PastOrPresent(message = "Data zatrudnienia nie może być z przyszłości")
    private LocalDate hireDate;
    @NotNull(message = "Pensja jest wymagana")
    @DecimalMin(value = "0.01", message = "Pensja musi być większa od zera")
    private Double salary;
}