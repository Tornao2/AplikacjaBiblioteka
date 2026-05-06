package com.biblioteka.backend.service;

import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.entity.UserRole;
import com.biblioteka.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private SecurityConfig securityConfig;
    @Test
    @DisplayName("UserDetails: Sukces i poprawne mapowanie roli")
    void shouldMapUserToUserDetails() {
        User user = User.builder()
                .username("jan")
                .password("encoded_pass")
                .role(UserRole.Admin)
                .build();
        when(userRepository.findByUsername("jan")).thenReturn(Optional.of(user));
        UserDetails result = securityConfig.userDetailsService().loadUserByUsername("jan");
        assertEquals("jan", result.getUsername());
        assertEquals("encoded_pass", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Admin")));
    }

    @Test
    @DisplayName("UserDetails: Rzuca błąd gdy użytkownik nie istnieje")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("brak")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> securityConfig.userDetailsService().loadUserByUsername("brak"));
    }

    @Test
    @DisplayName("PasswordEncoder: BCrypt jest używany")
    void passwordEncoderShouldBeBCrypt() {
        assertNotNull(securityConfig.passwordEncoder());
        String raw = "test";
        String encoded = securityConfig.passwordEncoder().encode(raw);
        assertTrue(securityConfig.passwordEncoder().matches(raw, encoded));
    }
}