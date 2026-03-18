package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import com.project.crud.frontend.model.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {
    @FXML private Button enlegthenMenuBtn;
    @FXML private Button staffSearchBtn;
    @FXML private Button inventoryBtn;
    @FXML private StackPane contentArea;
    @FXML private Label welcomeLabel;
    @FXML private Label adminSectionLabel;
    @FXML private Label librarianSectionLabel;
    @FXML private Label userSectionLabel;
    @FXML
    public void initialize() {
        if (UserSession.getInstance() != null) {
            welcomeLabel.setText("Zalogowano jako: " + UserSession.getInstance().getUsername());
        }
        showCatalog();
        UserRole role = UserSession.getInstance().getRole();
        boolean isStaff = (role == UserRole.LIBRARIAN || role == UserRole.ADMIN);
        boolean isAdmin = role == UserRole.ADMIN;
        adminSectionLabel.setVisible(isAdmin);
        adminSectionLabel.setManaged(isAdmin);
        librarianSectionLabel.setVisible(isStaff);
        librarianSectionLabel.setManaged(isStaff);
        userSectionLabel.setVisible(isStaff);
        userSectionLabel.setManaged(isStaff);
        enlegthenMenuBtn.setVisible(isStaff);
        enlegthenMenuBtn.setManaged(isStaff);
        staffSearchBtn.setVisible(isStaff);
        staffSearchBtn.setManaged(isStaff);
        inventoryBtn.setVisible(isAdmin);
        inventoryBtn.setManaged(isAdmin);
    }

    @FXML
    public void showCatalog() {
        loadView("catalog-view.fxml");
    }

    @FXML
    public void showMyLoans() {
        loadView("loans-view.fxml");
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/" + fxml));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        UserSession.logout();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
        stage.setScene(new Scene(loader.load(), 1200, 900));
        stage.setTitle("Logowanie");
    }

    @FXML
    public void showManagement() {
        loadView("management-view.fxml");
    }

    @FXML
    public void showUserManagement() {
        loadView("user-management-view.fxml");
    }

    @FXML
    public void showInventory() {
        loadView("inventory-view.fxml");
    }

    @FXML
    private void showProfileSettings() {
        loadView("profile-settings-view.fxml");
    }

    @FXML
    private void showAddUser() {
        loadView("add-user-view.fxml");
    }
}