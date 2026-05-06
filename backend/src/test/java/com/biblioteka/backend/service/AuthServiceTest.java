package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.AuthResponse;
import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.entity.UserRole;
import com.biblioteka.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Logowanie: poprawne dane")
    void loginSuccess() {
        User user = User.builder()
                .username("test")
                .password("encoded_pass")
                .role(UserRole.Czytelnik)
                .build();
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw_pass", "encoded_pass")).thenReturn(true);
        when(jwtService.generateToken(any(), any())).thenReturn("fake_token");
        AuthResponse response = authService.login("test", "raw_pass");
        assertNotNull(response.getToken());
        assertEquals("test", response.getUsername());
    }

    @Test
    @DisplayName("Logowanie: błędne hasło")
    void loginWrongPassword() {
        User user = User.builder().username("test").password("encoded_pass").build();
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_pass", "encoded_pass")).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("test", "wrong_pass"));
        assertTrue(ex.getMessage().contains("401"));
    }
}