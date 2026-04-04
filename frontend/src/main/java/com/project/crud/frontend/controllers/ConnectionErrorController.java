package com.project.crud.frontend.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class ConnectionErrorController {
    @FXML private Button retryBtn;

    @FXML
    private void handleRetry() {
        retryBtn.setDisable(true);
        PauseTransition pause = new PauseTransition(Duration.millis(800));
        pause.setOnFinished(e -> {
            try {
                Stage stage = (Stage) retryBtn.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
                stage.getScene().setRoot(loader.load());
            } catch (IOException ex) {
                retryBtn.setDisable(false);
                retryBtn.setText("Błąd ładowania. Ponów próbę.");
            }
        });
        pause.play();
    }
}