package com.project.crud.frontend;

import com.project.crud.frontend.auth.AuthResponse;
import com.project.crud.frontend.auth.UserSession;
import com.project.crud.frontend.model.UserRole;
import org.junit.jupiter.api.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {
    private AuthResponse fullMockToken;

    @BeforeEach
    void setUp() {
        UserSession.logout();
        fullMockToken = new AuthResponse();
        fullMockToken.setToken("jwt-secret-123");
        fullMockToken.setUsername("testowy_user");
        fullMockToken.setRole(UserRole.Admin);
        fullMockToken.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Login: Powinien poprawnie zapisać wszystkie pola AuthResponse")
    void shouldStoreFullAuthResponseData() {
        UserSession.login(fullMockToken);
        UserSession session = UserSession.getInstance();
        assertNotNull(session);
        AuthResponse storedToken = session.getToken();
        assertAll("Weryfikacja pól sesji",
                () -> assertEquals("jwt-secret-123", storedToken.getToken()),
                () -> assertEquals("testowy_user", storedToken.getUsername()),
                () -> assertEquals(UserRole.Admin, storedToken.getRole()),
                () -> assertEquals("test@example.com", storedToken.getEmail())
        );
    }

    @Test
    @DisplayName("Logout: Powinien całkowicie wyczyścić instancję sesji")
    void shouldNullifyInstanceOnLogout() {
        UserSession.login(fullMockToken);
        UserSession.logout();
        assertNull(UserSession.getInstance());
    }

    @Test
    @DisplayName("Singleton: Nie powinien nadpisywać sesji bez wylogowania")
    void shouldNotOverwriteExistingSession() {
        UserSession.login(fullMockToken);
        AuthResponse anotherToken = new AuthResponse();
        anotherToken.setToken("other-token");
        UserSession.login(anotherToken);
        assertEquals("jwt-secret-123", UserSession.getInstance().getToken().getToken());
    }

    @Test
    @DisplayName("Concurrency: Powinien stworzyć TYLKO JEDNĄ instancję i jej nie nadpisywać")
    void threadSafetyTest() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        UserSession[] results = new UserSession[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    AuthResponse t = new AuthResponse();
                    t.setToken("token-" + index);
                    UserSession.login(t);
                    results[index] = UserSession.getInstance();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        UserSession finalInstance = UserSession.getInstance();
        assertNotNull(finalInstance);
        for (int i = 0; i < threadCount; i++) {
            assertSame(finalInstance, results[i], "Wątek " + i + " widzi inną instancję!");
        }
    }
}