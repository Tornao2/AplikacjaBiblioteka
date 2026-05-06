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

class UserDTOTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Poprawny DTO")
    void validDto() {
        UserDTO dto = createValid();
        assertTrue(validator.validate(dto).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"jan.kowalski", "Ania123", "Bąk_01", "M-Nowak"})
    @DisplayName("Poprawne nazwy użytkownika")
    void validUsernames(String username) {
        UserDTO dto = createValid();
        dto.setUsername(username);
        assertTrue(validator.validate(dto).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ja", "user@123", "nazwa z spacją"})
    @DisplayName("Błędne nazwy użytkownika")
    void invalidUsernames(String username) {
        UserDTO dto = createValid();
        dto.setUsername(username);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Błędny format email")
    void invalidEmail() {
        UserDTO dto = createValid();
        dto.setEmail("zly.email.com");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Poprawne łączenie imienia i nazwiska")
    void getFullNameTest() {
        UserDTO dto = createValid();
        assertEquals("Jan Kowalski", dto.getFullName());
    }

    private UserDTO createValid() {
        return UserDTO.builder()
                .username("janek")
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan@test.pl")
                .role(UserRole.Czytelnik)
                .build();
    }
}