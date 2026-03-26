package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import com.project.crud.frontend.model.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private HBox errorCont;
    @FXML private Button btn;

    @FXML
    private void initialize() {
        errorCont.setVisible(false);
        btn.disableProperty().bind(usernameField.textProperty().isEmpty()
                .or(passwordField.textProperty().isEmpty()));
    }

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        if ("admin".equals(user) && "admin".equals(pass)) {
            performLogin(user, "admin@admin.pl", UserRole.ADMIN);
        } else if ("biblio".equals(user) && "biblio".equals(pass)) {
            performLogin(user, "biblio@admin.pl", UserRole.LIBRARIAN);
        } else if ("user".equals(user) && "user".equals(pass)) {
            performLogin(user, "user@admin.pl", UserRole.USER);
        } else {
            showError("Błędny login lub hasło!");
        }
    }

    private void performLogin(String user, String email, UserRole role) {
        UserSession.login(user, email, role);
        loadMainView();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorCont.setVisible(true);
    }

    private void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/main-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1200, 900));
            stage.setTitle("System Biblioteczny");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Błąd ładowania widoku głównego!");
            e.printStackTrace();
        }
    }
}