package com.project.crud.frontend.auth;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserSession {
    @Getter
    private static UserSession instance;
    private AuthResponse token;

    private UserSession(AuthResponse token) {
        this.token = token;
    }

    public static void login(AuthResponse token) {
        instance = new UserSession(token);
    }

    public static void logout() {
        instance = null;
    }

    public AuthResponse getToken() {
        return (instance != null) ? instance.token : null;
    }
}