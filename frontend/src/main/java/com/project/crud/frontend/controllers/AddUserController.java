package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddUserController {
    @FXML private TextField firstNameField, lastNameField, emailField, usernameField;
    @FXML private PasswordField passwordField, confirmPasswordField;

    @FXML
    private void handleRegister() {
        if (isInputInvalid()) {
            showAlert("Błąd", "Wypełnij poprawnie wszystkie pola (Imię, Nazwisko, Email, Login).");
            return;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Błąd", "Hasła nie są identyczne.");
            return;
        }
        UserDTO newUser = UserDTO.builder()
                .firstName(firstNameField.getText().trim())
                .lastName(lastNameField.getText().trim())
                .email(emailField.getText().trim())
                .username(usernameField.getText().trim())
                .role(UserRole.USER)
                .build();
        showAlert("Sukces", "Czytelnik " + newUser.getFullName() + " został dodany.");
        handleClear();
    }

    @FXML
    private void handleClear() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private boolean isInputInvalid() {
        return firstNameField.getText().isBlank() ||
                lastNameField.getText().isBlank() ||
                usernameField.getText().isBlank() ||
                !emailField.getText().contains("@") ||
                passwordField.getText().length() < 4;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }
}