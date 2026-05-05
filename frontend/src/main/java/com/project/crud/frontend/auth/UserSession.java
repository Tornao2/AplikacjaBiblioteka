package com.project.crud.frontend.auth;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserSession {
    @Getter
    private static volatile UserSession instance;
    private AuthResponse token;

    private UserSession(AuthResponse token) {
        this.token = token;
    }

    public static void login(AuthResponse token) {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession(token);
                }
            }
        }
    }

    public static void logout() {
        synchronized (UserSession.class) {
            instance = null;
        }
    }

    public AuthResponse getToken() {
        return (instance != null) ? instance.token : null;
    }
}