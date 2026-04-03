package com.project.crud.frontend.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class ConnectionErrorController {
    @FXML
    private Button retryBtn;

    @FXML
    private void handleRetry() {
        retryBtn.setDisable(true);
        retryBtn.setText("Próba połączenia...");
        new Thread(() -> {
            try {
                Thread.sleep(800);
                Platform.runLater(() -> {
                    try {
                        Stage stage = (Stage) retryBtn.getScene().getWindow();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
                        Parent root = loader.load();
                        stage.getScene().setRoot(root);
                    } catch (IOException e) {
                        System.err.println("Nie udało się wrócić do ekranu logowania: " + e.getMessage());
                        retryBtn.setDisable(false);
                        retryBtn.setText("Ponów próbę połączenia");
                    }
                });
            } catch (InterruptedException ignored) {}
        }).start();
    }
}