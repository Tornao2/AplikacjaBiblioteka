package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StaffRegistrationRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Powinien przejść, gdy żądanie rejestracji jest poprawne")
    void validStaffRegistrationTest() {
        StaffRegistrationRequest request = createValidStaffRequest();
        Set<ConstraintViolation<StaffRegistrationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Lista błędów powinna być pusta");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Kowalski", "Nowak-Jeziorański", "Mickiewicz-Wajda", "Bąk"})
    @DisplayName("Powinien akceptować poprawne formaty nazwisk")
    void validLastNameTest(String lastName) {
        StaffRegistrationRequest request = createValidStaffRequest();
        request.setLastName(lastName);
        Set<ConstraintViolation<StaffRegistrationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Nazwisko " + lastName + " powinno być uznane za poprawne");
    }

    @ParameterizedTest
    @ValueSource(strings = {"kowalski", "Nowak--Jeziorański", "Nowak-", "-Nowak", "123Nowak"})
    @DisplayName("Powinien odrzucać niepoprawne formaty nazwisk")
    void invalidLastNameTest(String lastName) {
        StaffRegistrationRequest request = createValidStaffRequest();
        request.setLastName(lastName);
        Set<ConstraintViolation<StaffRegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Nazwisko " + lastName + " powinno zostać odrzucone");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "+48123456789", "0123456789"})
    @DisplayName("Powinien akceptować poprawne polskie numery telefonów")
    void validPhoneNumberTest(String phone) {
        StaffRegistrationRequest request = createValidStaffRequest();
        request.setPhoneNumber(phone);
        Set<ConstraintViolation<StaffRegistrationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Numer " + phone + " powinien być poprawny");
    }

    @Test
    @DisplayName("Powinien odrzucić zbyt krótkie hasło")
    void shortPasswordTest() {
        StaffRegistrationRequest request = createValidStaffRequest();
        request.setPassword("1234");
        Set<ConstraintViolation<StaffRegistrationRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Hasło musi mieć minimum 5 znaków")));
    }

    @Test
    @DisplayName("Powinien odrzucić datę zatrudnienia z przyszłości")
    void futureHireDateTest() {
        StaffRegistrationRequest request = createValidStaffRequest();
        request.setHireDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<StaffRegistrationRequest>> violations = validator.validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nie może być z przyszłości")));
    }

    private StaffRegistrationRequest createValidStaffRequest() {
        StaffRegistrationRequest request = new StaffRegistrationRequest();
        request.setUsername("pracownik1");
        request.setPassword("TajneHaslo123");
        request.setFirstName("Jan");
        request.setLastName("Kowalski");
        request.setEmail("jan@biblioteka.pl");
        request.setPhoneNumber("123456789");
        request.setHireDate(LocalDate.now());
        request.setSalary(5000.0);
        request.setRole(UserRole.Bibliotekarz);
        return request;
    }
}