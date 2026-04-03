package com.biblioteka.backend.dto;

import lombok.Data;
import com.biblioteka.backend.entity.UserRole;

@Data
public class UserRegistrationRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
}