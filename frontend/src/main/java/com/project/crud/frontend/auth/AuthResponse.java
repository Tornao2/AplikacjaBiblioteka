package com.project.crud.frontend.auth;

import com.project.crud.frontend.model.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
private String token;
private String username;
private UserRole role;
private String email;
}