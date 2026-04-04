package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.stream.Stream;

public class ProfileController {
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    @FXML private Button aktButton, chanButton;

    @FXML
    public void initialize() {
        String currentEmail = UserSession.getInstance().getToken().getEmail();
        if (currentEmail != null) emailField.setText(currentEmail);
        aktButton.disableProperty().bind(emailField.textProperty().isEmpty()
                .or(emailField.textProperty().isEqualTo(currentEmail != null ? currentEmail : "")));
        chanButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> Stream.of(currentPasswordField, newPasswordField, confirmPasswordField)
                        .anyMatch(f -> f.getText().isEmpty()),
                currentPasswordField.textProperty(), newPasswordField.textProperty(), confirmPasswordField.textProperty()
        ));
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.logout();
            Stage stage = (Stage) emailField.getScene().getWindow();
            var loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
            stage.setScene(new Scene(loader.load(), 1200, 900));
            stage.setTitle("Logowanie");
        } catch (Exception e) {
            showAlert("Błąd", "Nie udało się powrócić do logowania.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdateEmail() {
        String mail = emailField.getText().trim();
        if (mail.length() >= 5 && mail.contains("@")) {
            UserSession.getInstance().getToken().setEmail(mail.toLowerCase());
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
        } else if (next.length() < 5) {
            showAlert("Błąd", "Hasło musi mieć przynajmniej 5 znaków.", Alert.AlertType.ERROR);
        } else {
            showAlert("Sukces", "Hasło zostało zmienione.", Alert.AlertType.INFORMATION);
            handleLogout();
        }
    }

    @FXML
    private void handleDeleteAccount() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Ta operacja jest nieodwracalna. Czy na pewno usunąć konto?");
        styleAlert(a, "Usuwanie konta");
        Button ok = (Button) a.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Usuń bezpowrotnie");
        ok.getStyleClass().add("button-primary");
        a.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> handleLogout());
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert a = new Alert(type, content);
        styleAlert(a, title);
        a.showAndWait();
    }

    private void styleAlert(Alert a, String title) {
        a.setTitle(title);
        a.setHeaderText(null);
        DialogPane p = a.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        Button ok = (Button) p.lookupButton(ButtonType.OK);
        if (ok != null) { ok.getStyleClass().add("button-primary"); ok.setText("Rozumiem"); }
        Button can = (Button) p.lookupButton(ButtonType.CANCEL);
        if (can != null) { can.getStyleClass().add("button-outline-danger"); can.setText("Anuluj"); }
    }
}