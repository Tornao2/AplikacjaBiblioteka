package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class StaffRegistrationRequest {
    @NotBlank(message = "Login jest wymagany")
    @Size(min = 4, max = 20, message = "Login musi mieć od 4 do 20 znaków")
    private String username;
    @Size(min = 5, message = "Hasło musi mieć minimum 5 znaków")
    private String password;
    @NotBlank(message = "Imię jest wymagane")
    @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]*$", message = "Imię musi zaczynać się od wielkiej litery")
    private String firstName;
    @NotBlank(message = "Nazwisko jest wymagane")
    @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]*(-[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]*)?$", message = "Niepoprawny format nazwiska")
    private String lastName;
    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Błędny format adresu email")
    private String email;
    @NotBlank(message = "Numer telefonu jest wymagany")
    @Pattern(regexp = "^(\\+48|0)?\\d{9}$", message = "Numer telefonu musi składać się z 9 cyfr")
    private String phoneNumber;
    @NotNull(message = "Data zatrudnienia jest wymagana")
    @PastOrPresent(message = "Data zatrudnienia nie może być z przyszłości")
    private LocalDate hireDate;
    @NotNull(message = "Pensja jest wymagana")
    private Double salary;
    @NotNull(message = "Rola musi być określona")
    private UserRole role;
}