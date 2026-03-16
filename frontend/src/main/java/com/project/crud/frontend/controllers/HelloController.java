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

public class HelloController {
    @FXML private Label welcomeLabel;
    @FXML private HBox adminPanel;
    @FXML private Button deleteBtn;
    @FXML private Button rentBtn;
    @FXML private TextField titleField;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session == null) return;
        welcomeLabel.setText("Witaj, " + session.getUsername() + "!");
        applyPermissions(session.getRole());
    }

    private void applyPermissions(UserRole role) {
        switch (role) {
            case USER:
                adminPanel.setVisible(false);
                adminPanel.setManaged(false);
                break;
            case LIBRARIAN:
                deleteBtn.setDisable(true);
                rentBtn.setVisible(false);
                rentBtn.setManaged(false);
                break;
            case ADMIN:
                break;
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        UserSession.logout();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 350);
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Logowanie");
    }

    @FXML
    private void addBook() {
        System.out.println("Dodawanie: " + titleField.getText());
    }

    @FXML
    private void deleteBook() {
        System.out.println("Usuwanie zaznaczonej książki");
    }

    @FXML
    private void rentBook() {
        System.out.println("Wypożyczanie książki");
    }
}