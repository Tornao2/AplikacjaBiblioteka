package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import com.project.crud.frontend.model.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private HBox errorCont;

    @FXML
    private void initialize() {
        errorCont.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        if ("admin".equals(user) && "admin".equals(pass)) {
            UserSession.login(user, "admin@admin.pl", UserRole.ADMIN);
            loadMainView();
        } else if ("biblio".equals(user) && "biblio".equals(pass)) {
            UserSession.login(user, "biblio@admin.pl", UserRole.LIBRARIAN);
            loadMainView();
        } else if ("user".equals(user) && "user".equals(pass)) {
            UserSession.login(user,"user@admin.pl", UserRole.USER);
            loadMainView();
        } else {
            errorLabel.setText("Błędne dane!");
            errorCont.setVisible(true);
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