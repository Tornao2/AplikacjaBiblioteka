package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ProfileController {
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    @FXML private Button aktButton, chanButton;

    @FXML
    public void initialize() {
        String currentEmail = UserSession.getInstance().getUserEmail();
        if (currentEmail != null) emailField.setText(currentEmail);
        aktButton.disableProperty().bind(emailField.textProperty().isEmpty()
                .or(emailField.textProperty().isEqualTo(currentEmail)));
        chanButton.disableProperty().bind(currentPasswordField.textProperty().isEmpty()
                .or(newPasswordField.textProperty().isEmpty())
                .or(confirmPasswordField.textProperty().isEmpty()));
    }

    @FXML
    private void handleUpdateEmail() {
        String newEmail = emailField.getText().trim();
        if (newEmail.length() >= 5 && newEmail.contains("@")) {
            UserSession.getInstance().setUserEmail(newEmail.toLowerCase());
            showAlert("Sukces", "Adres email został zaktualizowany.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Błąd", "Podaj poprawny adres email (min. 5 znaków).", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdatePassword() {
        String next = newPasswordField.getText();
        if (!next.equals(confirmPasswordField.getText())) {
            showAlert("Błąd", "Nowe hasła nie są identyczne.", Alert.AlertType.ERROR);
            return;
        }
        if (next.length() < 5) {
            showAlert("Błąd", "Hasło musi mieć przynajmniej 5 znaków.", Alert.AlertType.ERROR);
            return;
        }
        showAlert("Sukces", "Hasło zostało zmienione.", Alert.AlertType.INFORMATION);
        clearPasswordFields();
    }

    @FXML
    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Ta operacja jest nieodwracalna. Czy na pewno usunąć konto?");
        styleAlert(alert, "Usuwanie konta");
        Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Usuń bezpowrotnie");
        ok.getStyleClass().add("button-primary");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                UserSession.logout();
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1200, 900));
            stage.setTitle("Logowanie");
            stage.centerOnScreen();
        } catch (Exception e) {
            showAlert("Błąd", "Nie udało się powrócić do logowania.", Alert.AlertType.ERROR);
        }
    }

    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type, content);
        styleAlert(alert, title);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert, String title) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        DialogPane dp = alert.getDialogPane();
        dp.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dp.getStyleClass().add("root-container");
        Button ok = (Button) dp.lookupButton(ButtonType.OK);
        if (ok != null) {
            ok.getStyleClass().add("button-primary");
            ok.setText("Rozumiem");
        }
        Button cancel = (Button) dp.lookupButton(ButtonType.CANCEL);
        if (cancel != null) {
            cancel.getStyleClass().add("button-outline-danger");
            cancel.setText("Anuluj");
        }
    }
}