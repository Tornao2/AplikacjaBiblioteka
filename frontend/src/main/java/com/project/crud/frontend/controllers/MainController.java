package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import com.project.crud.frontend.model.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {
    @FXML private Button staffSearchBtn, inventoryBtn, staffBtn, managBtn, addBtn, delBtn, logsBtn, systemBtn;
    @FXML private StackPane contentArea;
    @FXML private Label welcomeLabel, adminSectionLabel, librarianSectionLabel, userSectionLabel;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session != null) {
            welcomeLabel.setText("Zalogowano jako: " + session.getUsername());
            applySecurityPolicy(session.getRole());
        }
        showCatalog();
    }

    private void applySecurityPolicy(UserRole role) {
        boolean isStaff = (role == UserRole.LIBRARIAN || role == UserRole.ADMIN);
        boolean isAdmin = (role == UserRole.ADMIN);
        configureNodes(isStaff, librarianSectionLabel, userSectionLabel, staffSearchBtn, inventoryBtn, addBtn);
        configureNodes(isAdmin, adminSectionLabel, staffBtn, systemBtn, logsBtn, delBtn, managBtn);
    }

    private void configureNodes(boolean visible, javafx.scene.Node... nodes) {
        for (javafx.scene.Node node : nodes) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/" + fxml));
            contentArea.getChildren().setAll(loader.<javafx.scene.Parent>load());
        } catch (IOException e) {
            System.err.println("Błąd ładowania widoku: " + fxml);
            e.printStackTrace();
        }
    }

    @FXML private void handleLogout() throws IOException {
        UserSession.logout();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
        stage.setScene(new Scene(loader.load(), 1200, 900));
        stage.setTitle("Logowanie");
    }

    @FXML public void showCatalog() { loadView("catalog-view.fxml"); }
    @FXML public void showMyLoans() { loadView("loans-view.fxml"); }
    @FXML public void showInventory() { loadView("inventory-view.fxml"); }
    @FXML public void showUserManagement() { loadView("user-management-view.fxml"); }
    @FXML private void showProfileSettings() { loadView("profile-settings-view.fxml"); }
    @FXML private void showAddUser() { loadView("add-user-view.fxml"); }
    @FXML private void showAdminUsers() { loadView("admin-users-view.fxml"); }
    @FXML private void showSettings() { loadView("admin-settings-view.fxml"); }
    @FXML private void showLogs() { loadView("admin-logs-view.fxml"); }
    @FXML private void showDel() { loadView("admin-delete-users-view.fxml"); }
    @FXML private void showMang() { loadView("admin-manag-view.fxml"); }
}