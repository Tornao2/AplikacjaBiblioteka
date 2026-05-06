package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StaffDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Powinien przejść, gdy StaffDTO i zagnieżdżony UserDTO są poprawne")
    void validStaffDtoTest() {
        StaffDTO dto = createValidStaffDto();
        Set<ConstraintViolation<StaffDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Nie powinno być żadnych błędów walidacji");
    }

    @Test
    @DisplayName("Powinien wykryć błąd w zagnieżdżonym obiekcie UserDTO")
    void invalidNestedUserTest() {
        UserDTO invalidUser = new UserDTO();
        invalidUser.setEmail("zly-email");
        StaffDTO dto = createValidStaffDto();
        dto.setUser(invalidUser);
        Set<ConstraintViolation<StaffDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Powinien wykryć błędy wewnątrz UserDTO");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("user.email")),
                "Błąd powinien dotyczyć pola email w obiekcie user");
    }

    @Test
    @DisplayName("Powinien odrzucić ujemną lub zerową pensję")
    void invalidSalaryTest() {
        StaffDTO dto = createValidStaffDto();
        dto.setSalary(0.0);
        Set<ConstraintViolation<StaffDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("musi być większa od zera")));
    }

    @Test
    @DisplayName("Powinien odrzucić brak numeru telefonu")
    void missingPhoneTest() {
        StaffDTO dto = createValidStaffDto();
        dto.setPhoneNumber(null);
        Set<ConstraintViolation<StaffDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    private StaffDTO createValidStaffDto() {
        UserDTO user = UserDTO.builder()
                .username("admin_test")
                .firstName("Jan")
                .lastName("Kowalski")
                .email("test@biblioteka.pl")
                .role(UserRole.Bibliotekarz)
                .build();

        return StaffDTO.builder()
                .id(1L)
                .user(user)
                .phoneNumber("123456789")
                .hireDate(LocalDate.now())
                .salary(5000.0)
                .build();
    }
}