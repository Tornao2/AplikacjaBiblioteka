package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import com.project.crud.frontend.model.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        if ("admin".equals(user) && "admin".equals(pass)) {
            UserSession.login(user, UserRole.ADMIN);
            loadMainView();
        } else if ("biblio".equals(user) && "biblio".equals(pass)) {
            UserSession.login(user, UserRole.LIBRARIAN);
            loadMainView();
        } else if ("user".equals(user) && "user".equals(pass)) {
            UserSession.login(user, UserRole.USER);
            loadMainView();
        } else {
            errorLabel.setText("Błędne dane!");
        }
    }

    private void loadMainView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1200, 900);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("System Biblioteczny");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}