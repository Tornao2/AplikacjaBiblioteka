package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ProfileController {
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;

    @FXML
    public void initialize() {
        if (UserSession.getInstance().getUserEmail() != null) {
            emailField.setText(UserSession.getInstance().getUserEmail());
        }
    }

    @FXML
    private void handleUpdateEmail() {
        String newEmail = emailField.getText();
        if (newEmail != null && newEmail.contains("@") && newEmail.length() > 5) {
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

    @FXML
    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Usuwanie konta");
        alert.setHeaderText("Czy na pewno chcesz usunąć konto?");
        alert.setContentText("Ta operacja jest nieodwracalna. Stracisz dostęp do systemu.");
        DialogPane dialogPane = alert.getDialogPane();
        if (emailField.getScene() != null) {
            dialogPane.getStylesheets().addAll(emailField.getScene().getStylesheets());
        }
        dialogPane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Usuń bezpowrotnie");
        okButton.getStyleClass().add("button-primary");
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Anuluj");
        cancelButton.getStyleClass().add("button-outline-danger");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Konto zostało usunięte.");
                UserSession.logout();
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene loginScene = new Scene(loginRoot, 1200, 900);
            stage.setScene(loginScene);
            stage.setTitle("Logowanie");
            stage.centerOnScreen();
            stage.show();
        } catch (java.io.IOException e) {
            showAlert("Błąd", "Nie udało się powrócić do ekranu logowania.");
            e.printStackTrace();
        }
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
        DialogPane dialogPane = alert.getDialogPane();
        if (emailField.getScene() != null) {
            dialogPane.getStylesheets().addAll(emailField.getScene().getStylesheets());
        }
        alert.showAndWait();
    }
}