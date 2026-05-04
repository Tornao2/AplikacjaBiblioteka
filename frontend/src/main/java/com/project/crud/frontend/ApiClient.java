package com.project.crud.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.crud.frontend.auth.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.*;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Node viewNode;

    public ApiClient(Node viewNode) {
        this.viewNode = viewNode;
    }

    private HttpRequest.Builder createBuilder(String endpoint) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");
        UserSession session = UserSession.getInstance();
        if (session != null && session.getToken() != null) {
            builder.header("Authorization", "Bearer " + session.getToken().getToken());
        }
        return builder;
    }

    public <T> CompletableFuture<T> send(String endpoint, String method, Object body, Class<T> type) {
        try {
            HttpRequest.BodyPublisher publisher = (body == null)
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body));
            var req = createBuilder(endpoint)
                    .method(method, publisher)
                    .build();
            return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> {
                        if (res.statusCode() >= 500) throw new RuntimeException("500:Serwer leży");
                        if (res.statusCode() >= 400) throw new RuntimeException(res.body());
                        try {
                            if (res.body().isEmpty() || type == Void.class) return null;
                            return mapper.readValue(res.body(), type);
                        } catch (Exception e) {
                            throw new RuntimeException("Mapping error: " + e.getMessage());
                        }
                    }).exceptionally(ex -> {
                        handleException(ex);
                        throw new RuntimeException(ex);
                    });
        } catch (Exception e) {
            handleException(e);
            return CompletableFuture.completedFuture(null);
        }
    }

    private void handleException(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof ConnectException || cause.getMessage().contains("Connection refused")) {
            Platform.runLater(this::loadErrorScreen);
        }
    }

    private void loadErrorScreen() {
        try {
            if (viewNode.getScene() == null) return;
            Parent root = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/error-connection-view.fxml")).load();
            viewNode.getScene().setRoot(root);
        } catch (Exception ignored) {  }
    }

    public static String getErrorMessage(Throwable ex) {
        if (ex == null) return "Nieznany błąd";
        Throwable cause = ex;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}