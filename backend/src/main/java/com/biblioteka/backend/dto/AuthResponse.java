package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.UserRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private UserRole role;
}