package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.AuthResponse;
import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("401:Nieprawidłowy login"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("401:Nieprawidłowe hasło");
        }
        String token = jwtService.generateToken(user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}