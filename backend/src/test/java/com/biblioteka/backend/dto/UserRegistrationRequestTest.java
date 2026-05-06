package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.UserRole;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserRegistrationRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Poprawne żądanie")
    void validRequest() {
        UserRegistrationRequest request = createValid();
        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user.1", "admin_pro", "test1234"})
    @DisplayName("Poprawne loginy")
    void validUsernames(String username) {
        UserRegistrationRequest request = createValid();
        request.setUsername(username);
        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "user@123", "nazwa ze spacją", "bardzo_dlugi_login_przekraczajacy_limit"})
    @DisplayName("Błędne loginy")
    void invalidUsernames(String username) {
        UserRegistrationRequest request = createValid();
        request.setUsername(username);
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Zbyt krótkie hasło")
    void shortPassword() {
        UserRegistrationRequest request = createValid();
        request.setPassword("1234");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Błędny email")
    void invalidEmail() {
        UserRegistrationRequest request = createValid();
        request.setEmail("not-an-email");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Brak roli")
    void missingRole() {
        UserRegistrationRequest request = createValid();
        request.setRole(null);
        assertFalse(validator.validate(request).isEmpty());
    }

    private UserRegistrationRequest createValid() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("nowy_user");
        request.setPassword("Tajne123");
        request.setFirstName("Anna");
        request.setLastName("Nowak");
        request.setEmail("ania@test.pl");
        request.setRole(UserRole.Czytelnik);
        return request;
    }
}