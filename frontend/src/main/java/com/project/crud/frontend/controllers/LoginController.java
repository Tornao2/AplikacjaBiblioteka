package com.project.crud.frontend.controllers;

import com.project.crud.frontend.ApiClient;
import com.project.crud.frontend.auth.AuthResponse;
import com.project.crud.frontend.auth.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Map;
import java.util.stream.Stream;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private HBox errorCont;
    @FXML private VBox globalLoadingOverlay;
    @FXML private Button btn;

    private ApiClient apiClient;

    @FXML
    private void initialize() {
        this.apiClient = new ApiClient(usernameField);
        setLoading(false);
        errorCont.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        setLoading(true);
        errorCont.setVisible(false);
        var request = Map.of(
                "username", usernameField.getText(),
                "password", passwordField.getText()
        );
        apiClient.send("/auth/login", "POST", request, AuthResponse.class)
                .thenAccept(res -> Platform.runLater(() -> {
                    setLoading(false);
                    UserSession.login(res);
                    loadMainView();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        String msg = ApiClient.getErrorMessage(ex);
                        if (msg != null && (msg.contains("401") || msg.contains("Unauthorized"))) {
                            showError("Błędny login lub hasło!");
                        } else {
                            showError("Błąd: " + (msg != null ? msg : "Serwer nie odpowiada"));
                        }
                    });
                    return null;
                });
    }

    private void setLoading(boolean active) {
        if (globalLoadingOverlay != null) {
            globalLoadingOverlay.setVisible(active);
            globalLoadingOverlay.setManaged(active);
            if (active) globalLoadingOverlay.toFront();
        }
        Stream.of(usernameField, passwordField, btn).forEach(c -> c.setDisable(active));
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorCont.setVisible(true);
    }

    private void loadMainView() {
        try {
            var loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/main-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Zarządzanie biblioteką");
            stage.setScene(new Scene(loader.load(), 1200, 900));
            stage.centerOnScreen();
        } catch (Exception e) { showError("Błąd krytyczny interfejsu!"); }
    }
}