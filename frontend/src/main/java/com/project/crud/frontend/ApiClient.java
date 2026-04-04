package com.project.crud.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.crud.frontend.auth.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.*;
import java.util.List;
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

    public <T> CompletableFuture<T> get(String endpoint, Class<T> type) {
        return execute(createBuilder(endpoint).GET().build(), type);
    }

    public <T> CompletableFuture<List<T>> getList(String endpoint, Class<T> type) {
        var req = createBuilder(endpoint).GET().build();
        var listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    validateResponse(res);
                    try {
                        return mapper.<List<T>>readValue(res.body(), listType);
                    } catch (Exception e) { throw new RuntimeException(e); }
                })
                .exceptionally(this::handleEx);
    }

    public <T> CompletableFuture<T> send(String endpoint, String method, Object body, Class<T> type) {
        try {
            var req = createBuilder(endpoint)
                    .method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            return execute(req, type);
        } catch (Exception e) {
            loadErrorScreen();
            return CompletableFuture.failedFuture(e);
        }
    }

    private <T> CompletableFuture<T> execute(HttpRequest req, Class<T> type) {
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    validateResponse(res);
                    try {
                        return (res.body().isEmpty() || type == Void.class) ? null : mapper.readValue(res.body(), type);
                    } catch (Exception e) { throw new RuntimeException(e); }
                })
                .exceptionally(this::handleEx);
    }

    private void validateResponse(HttpResponse<String> res) {
        if (res.statusCode() >= 400) throw new RuntimeException(String.valueOf(res.statusCode()));
    }

    private <T> T handleEx(Throwable ex) {
        var cause = ex.getCause() != null ? ex.getCause() : ex;
        var msg = cause.getMessage();
        if (msg == null || !List.of("400", "401", "403").contains(msg)) {
            Platform.runLater(this::loadErrorScreen);
        }
        return null;
    }

    private void loadErrorScreen() {
        try {
            var scene = viewNode.getScene();
            if (scene == null) return;
            Parent root = new FXMLLoader(getClass().getResource("error-connection-view.fxml")).load();
            (scene.getWindow()).getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}