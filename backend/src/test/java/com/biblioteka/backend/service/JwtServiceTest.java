package com.biblioteka.backend.service;

import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        userDetails = User.builder()
                .username("testUser")
                .password("password")
                .authorities("USER")
                .build();
    }

    @Test
    @DisplayName("JWT: sukces - generowanie i walidacja")
    void generateAndValidateToken() {
        String token = jwtService.generateToken(Collections.emptyMap(), userDetails.getUsername());
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals("testUser", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("JWT: błąd gdy inny użytkownik")
    void invalidUserToken() {
        String token = jwtService.generateToken(Collections.emptyMap(), "otherUser");
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("JWT: błąd gdy token jest uszkodzony")
    void tamperedToken() {
        String token = jwtService.generateToken(Collections.emptyMap(), userDetails.getUsername());
        String tamperedToken = token + "modified";
        assertThrows(SignatureException.class, () -> jwtService.extractUsername(tamperedToken));
    }

    @Test
    @DisplayName("JWT: ekstrahowanie ról z extraClaims")
    void extractExtraClaims() {
        Map<String, Object> claims = Map.of("role", "ADMIN");
        String token = jwtService.generateToken(claims, userDetails.getUsername());
        String role = jwtService.extractClaim(token, c -> c.get("role", String.class));
        assertEquals("ADMIN", role);
    }

    @Test
    @DisplayName("JWT: błąd przy nieprawidłowym formacie tokena")
    void malformedToken() {
        assertThrows(Exception.class, () -> jwtService.extractUsername("not.a.jwt.token"));
    }
}