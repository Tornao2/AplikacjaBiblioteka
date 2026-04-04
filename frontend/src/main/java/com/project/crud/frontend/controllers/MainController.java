package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import com.project.crud.frontend.model.UserRole;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.stream.Stream;

public class MainController {
    @FXML private Button staffSearchBtn, inventoryBtn, staffBtn, managBtn, addBtn, delBtn, logsBtn, systemBtn;
    @FXML private StackPane contentArea;
    @FXML private Label welcomeLabel, adminSectionLabel, librarianSectionLabel, userSectionLabel;
    @FXML private VBox globalLoadingOverlay;
    private static MainController instance;

    @FXML
    public void initialize() {
        instance = this;
        UserSession session = UserSession.getInstance();
        if (session != null) {
            welcomeLabel.setText("Zalogowano jako: " + session.getToken().getUsername());
            UserRole role = session.getToken().getRole();
            boolean isStaff = role == UserRole.LIBRARIAN || role == UserRole.ADMIN;
            boolean isAdmin = role == UserRole.ADMIN;
            configureNodes(isStaff, librarianSectionLabel, userSectionLabel, staffSearchBtn, inventoryBtn, addBtn);
            configureNodes(isAdmin, adminSectionLabel, staffBtn, systemBtn, logsBtn, delBtn, managBtn);
        }
        showCatalog();
    }

    public static void setLoading(boolean isLoading) {
        if (instance != null && instance.globalLoadingOverlay != null) {
            Platform.runLater(() -> {
                instance.globalLoadingOverlay.setVisible(isLoading);
                instance.globalLoadingOverlay.setManaged(isLoading);
                if (isLoading) instance.globalLoadingOverlay.toFront();
            });
        }
    }

    private void configureNodes(boolean visible, Node... nodes) {
        Stream.of(nodes).forEach(n -> { n.setVisible(visible); n.setManaged(visible); });
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/" + fxml));
            contentArea.getChildren().setAll((Parent) loader.load());
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout() throws IOException {
        UserSession.logout();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        var loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
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