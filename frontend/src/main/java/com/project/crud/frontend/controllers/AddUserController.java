package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddUserController {
    @FXML private TextField firstNameField, lastNameField, emailField, usernameField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Button registerBtn, clearBtn;

    @FXML
    public void initialize() {
        BooleanBinding isAnyFieldEmpty = Bindings.createBooleanBinding(() ->
                        firstNameField.getText().trim().isEmpty() ||
                                lastNameField.getText().trim().isEmpty() ||
                                emailField.getText().trim().isEmpty() ||
                                usernameField.getText().trim().isEmpty() ||
                                passwordField.getText().isEmpty() ||
                                confirmPasswordField.getText().isEmpty(),
                firstNameField.textProperty(), lastNameField.textProperty(),
                emailField.textProperty(), usernameField.textProperty(),
                passwordField.textProperty(), confirmPasswordField.textProperty()
        );
        registerBtn.disableProperty().bind(isAnyFieldEmpty);
        BooleanBinding areAllFieldsEmpty = Bindings.createBooleanBinding(() ->
                        firstNameField.getText().isEmpty() &&
                                lastNameField.getText().isEmpty() &&
                                emailField.getText().isEmpty() &&
                                usernameField.getText().isEmpty() &&
                                passwordField.getText().isEmpty() &&
                                confirmPasswordField.getText().isEmpty(),
                firstNameField.textProperty(), lastNameField.textProperty(),
                emailField.textProperty(), usernameField.textProperty(),
                passwordField.textProperty(), confirmPasswordField.textProperty()
        );
        clearBtn.disableProperty().bind(areAllFieldsEmpty);
    }

    @FXML
    private void handleRegister() {
        if (isInputInvalid()) {
            return;
        }
        UserDTO newUser = UserDTO.builder()
                .firstName(firstNameField.getText().trim())
                .lastName(lastNameField.getText().trim())
                .email(emailField.getText().trim())
                .username(usernameField.getText().trim())
                .role(UserRole.USER)
                .build();
        showInfo("Czytelnik " + newUser.getFullName() + " został dodany.", Alert.AlertType.INFORMATION);
        handleClear();
    }

    @FXML
    private void handleClear() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private boolean isInputInvalid() {
        if (firstNameField.getText().isBlank()){
            showInfo("Wypełnij pole imienia.", Alert.AlertType.ERROR);
        } else if (lastNameField.getText().isBlank()) {
            showInfo("Wypełnij pole nazwiska.", Alert.AlertType.ERROR);
        } else if (usernameField.getText().isBlank()) {
            showInfo("Wypełnij pole loginu.", Alert.AlertType.ERROR);
        } else if (!emailField.getText().contains("@")) {
            showInfo("Email powinien posiadać znak małpy (@).", Alert.AlertType.ERROR);
        } else if (passwordField.getText().length() < 4) {
            showInfo("Hasło musi być dłuższe niż 3 znaki", Alert.AlertType.ERROR);
        } else if (!passwordField.getText().equals(confirmPasswordField.getText())){
            showInfo("Hasła nie są identyczne.", Alert.AlertType.ERROR);
        }
        else {
            return false;
        }
        return true;
    }

    private void showInfo(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dialogPane.getStyleClass().add("root-container");
        alert.setHeaderText(null);
        if (alertType == Alert.AlertType.INFORMATION){
            alert.setTitle("Sukces");
        }
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.getStyleClass().add("button-primary");
            okButton.applyCss();
            okButton.setText("Rozumiem");
        }
        alert.showAndWait();
    }
}