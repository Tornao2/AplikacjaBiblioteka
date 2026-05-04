package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi mieć od 3 do 50 znaków")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+$",
            message = "Nazwa użytkownika może zawierać tylko litery (w tym polskie), cyfry, kropki, myślniki i podkreślenia"
    )
    private String username;
    @NotBlank(message = "Imię jest wymagane")
    @Size(max = 50, message = "Imię jest za długie")
    private String firstName;
    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(max = 50, message = "Nazwisko jest za długie")
    private String lastName;
    @NotBlank(message = "Adres e-mail jest wymagany")
    @Email(message = "Niepoprawny format adresu e-mail")
    @Size(max = 100, message = "Adres e-mail jest za długi")
    private String email;
    @NotNull(message = "Rola użytkownika musi zostać wybrana")
    private UserRole role;
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}