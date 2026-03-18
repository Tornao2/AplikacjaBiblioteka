package com.project.crud.frontend.auth;

import com.project.crud.frontend.model.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserSession {
    @Getter
    private static UserSession instance;
    private String username;
    private String userEmail;
    private UserRole role;
    private UserSession(String username, String email, UserRole role) {
        this.username = username;
        this.userEmail = email;
        this.role = role;
    }
    public static void login(String user, String email, UserRole role) {
        instance = new UserSession(user, email, role);
    }

    public static void logout() {
        instance = null;
    }
}