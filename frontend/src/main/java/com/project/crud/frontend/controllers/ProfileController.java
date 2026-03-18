package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProfileController {
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;

    @FXML
    public void initialize() {
        emailField.setText(UserSession.getInstance().getUserEmail());
    }

    @FXML
    private void handleUpdateEmail() {
        String newEmail = emailField.getText();
        if (newEmail.contains("@") && newEmail.length() > 5) {
            UserSession.getInstance().setUserEmail(newEmail);
            showAlert("Sukces", "Adres email został zaktualizowany.");
        } else {
            showAlert("Błąd", "Podaj poprawny adres email.");
        }
    }

    @FXML
    private void handleUpdatePassword() {
        String current = currentPasswordField.getText();
        String next = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();
        if (current.isEmpty() || next.isEmpty()) {
            showAlert("Błąd", "Pola haseł nie mogą być puste.");
            return;
        }
        if (!next.equals(confirm)) {
            showAlert("Błąd", "Nowe hasła nie są identyczne.");
            return;
        }
        showAlert("Sukces", "Hasło zostało zmienione.");
        clearPasswordFields();
    }

    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}