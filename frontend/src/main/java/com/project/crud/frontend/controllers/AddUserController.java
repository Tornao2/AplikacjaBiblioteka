package com.project.crud.frontend.controllers;

import com.project.crud.frontend.ApiClient;
import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRegistrationRequest;
import com.project.crud.frontend.model.UserRole;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Arrays;
import java.util.List;

public class AddUserController {
    @FXML private TextField firstNameField, lastNameField, emailField, usernameField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Button registerBtn, clearBtn;

    private List<TextInputControl> allFields;
    private ApiClient apiClient;

    public void initialize() {
        this.apiClient = new ApiClient(registerBtn);
        allFields = Arrays.asList(firstNameField, lastNameField, emailField, usernameField, passwordField, confirmPasswordField);
        var properties = allFields.stream().map(TextInputControl::textProperty).toArray(javafx.beans.value.ObservableValue[]::new);
        registerBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> allFields.stream().anyMatch(f -> f.getText().trim().isEmpty()), properties));
        clearBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> allFields.stream().allMatch(f -> f.getText().isEmpty()), properties));
    }

    @FXML
    private void handleRegister() {
        allFields.forEach(f -> f.setText(f.getText().trim()));
        emailField.setText(emailField.getText().toLowerCase());
        usernameField.setText(usernameField.getText().replaceAll("\\s+", ""));
        if (validate()) {
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .firstName(firstNameField.getText())
                    .lastName(lastNameField.getText())
                    .email(emailField.getText())
                    .username(usernameField.getText())
                    .password(passwordField.getText())
                    .role(UserRole.Czytelnik)
                    .build();
            apiClient.send("/users", "POST", request, UserDTO.class)
                    .thenAccept(registeredUser -> {
                        if (registeredUser != null) {
                            Platform.runLater(() -> {
                                showInfo("Czytelnik " + registeredUser.getFirstName() + " " + registeredUser.getLastName() + " dodany pomyślnie.", Alert.AlertType.INFORMATION);
                                handleClear();
                            });
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> showInfo(ApiClient.getErrorMessage(ex), Alert.AlertType.ERROR));
                        return null;
                    });
        }
    }

    @FXML
    private void handleClear() {
        allFields.forEach(TextInputControl::clear);
    }

    private boolean validate() {
        String pass = passwordField.getText();
        String uName = usernameField.getText();
        if (!emailField.getText().contains("@")) return err("Email musi zawierać @.");
        if (pass.length() < 5) return err("Hasło: min. 5 znaków.");
        if (!pass.equals(confirmPasswordField.getText())) return err("Hasła nie są identyczne.");
        if (uName.length() < 3) return err("Login: min. 3 znaki.");
        return true;
    }

    private boolean err(String msg) {
        showInfo(msg, Alert.AlertType.ERROR);
        return false;
    }

    private void showInfo(String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message);
        alert.setHeaderText(null);
        alert.setTitle(type == Alert.AlertType.INFORMATION ? "Sukces" : "Błąd");
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.getStyleClass().add("button-primary");
            okBtn.setText("Rozumiem");
        }
        alert.showAndWait();
    }
}