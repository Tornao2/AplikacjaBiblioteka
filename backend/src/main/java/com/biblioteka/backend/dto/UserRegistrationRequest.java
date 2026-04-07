package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationRequest {
    @NotBlank(message = "Login jest wymagany")
    @Size(min = 4, max = 20, message = "Login musi mieć od 4 do 20 znaków")
    @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Login może zawierać tylko litery, cyfry, kropki i podkreślenia")
    private String username;
    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 5, message = "Hasło musi mieć minimum 5 znaków")
    private String password;
    @NotBlank(message = "Imię nie może być puste")
    @Size(max = 50)
    private String firstName;
    @NotBlank(message = "Nazwisko nie może być puste")
    @Size(max = 50)
    private String lastName;
    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Podaj poprawny adres email")
    private String email;
    @NotNull(message = "Rola musi być określona")
    private UserRole role;
}