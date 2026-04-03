package com.project.crud.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Node viewNode;

    public ApiClient(Node viewNode) {
        this.viewNode = viewNode;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    private void handleCriticalError(Throwable ex) {
        Platform.runLater(this::loadErrorScreen);
    }

    private void showSimpleErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Błędne dane");
        alert.setHeaderText(null);
        alert.setContentText("Serwer odrzucił wprowadzone dane. Sprawdź limity i spróbuj ponownie.");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dialogPane.getStyleClass().add("root-container");
        alert.showAndWait();
    }

    public <T> CompletableFuture<T> get(String endpoint, Class<T> responseType) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        validateResponse(response);
                        return mapper.readValue(response.body(), responseType);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = cause.getMessage();
                    if (msg == null || !msg.contains("400")) {
                        handleCriticalError(cause);
                    }
                    throw new RuntimeException(cause);
                });
    }

    public <T> CompletableFuture<Object> getList(String endpoint, Class<T> responseType) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        validateResponse(response);
                        return mapper.readValue(response.body(),
                                mapper.getTypeFactory().constructCollectionType(List.class, responseType));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = cause.getMessage();
                    if (msg == null || !msg.contains("400")) {
                        handleCriticalError(cause);
                    }
                    throw new RuntimeException(cause);
                });
    }

    public <T> CompletableFuture<T> send(String endpoint, String method, Object body, Class<T> responseType) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        try {
                            validateResponse(response);
                            return responseType != null ? mapper.readValue(response.body(), responseType) : null;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .exceptionally(ex -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        String msg = cause.getMessage();
                        if (msg == null || !msg.contains("400")) {
                            handleCriticalError(cause);
                        }
                        throw new RuntimeException(cause);
                    });
        } catch (Exception e) {
            handleCriticalError(e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private void loadErrorScreen() {
        try {
            if (viewNode.getScene() == null) return;
            Stage stage = (Stage) viewNode.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/error-connection-view.fxml"));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("Krytyczny błąd: " + e.getMessage());
        }
    }

    private void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            throw new RuntimeException(String.valueOf(response.statusCode()));
        }
    }
}