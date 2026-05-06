package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.UserRegistrationRequest;
import com.biblioteka.backend.entity.Loan;
import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.repository.LoanRepository;
import com.biblioteka.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private LoanRepository loanRepository;

    @InjectMocks private UserService userService;

    @Test
    @DisplayName("Create: rzuca błąd gdy login zajęty")
    void shouldThrowExceptionWhenUsernameExists() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername("bob");
        when(userRepository.existsByUsername("bob")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> userService.createUser(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete: blokada gdy użytkownik ma wypożyczenia")
    void shouldPreventDeleteWithActiveLoans() {
        User user = User.builder().id(1L).build();
        Loan activeLoan = Loan.builder().userId(1L).returnDate(null).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(loanRepository.findAll()).thenReturn(List.of(activeLoan));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
        assertTrue(ex.getMessage().contains("aktywne wypożyczenia"));
    }

    @Test
    @DisplayName("Mapowanie: konwersja encji na DTO")
    void shouldMapEntityToDto() {
        User user = User.builder()
                .id(1L)
                .username("test")
                .firstName("Adam")
                .build();
        var dto = userService.mapToDTO(user);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getFirstName(), dto.getFirstName());
    }
}