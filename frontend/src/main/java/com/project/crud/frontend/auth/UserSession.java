package com.project.crud.frontend.auth;

import com.project.crud.frontend.model.UserRole;
import lombok.Getter;

@Getter
public class UserSession {
    @Getter
    private static UserSession instance;
    private final String username;
    private final UserRole role;

    private UserSession(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

    public static void login(String username, UserRole role) {
        instance = new UserSession(username, role);
    }

    public static void logout() {
        instance = null;
    }

}