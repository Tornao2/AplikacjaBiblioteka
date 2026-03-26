package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddUserController {
    @FXML private TextField firstNameField, lastNameField, emailField, usernameField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Button registerBtn, clearBtn;

    public void initialize() {
        registerBtn.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        isAnyBlank(firstNameField, lastNameField, emailField, usernameField, passwordField, confirmPasswordField),
                firstNameField.textProperty(), lastNameField.textProperty(), emailField.textProperty(),
                usernameField.textProperty(), passwordField.textProperty(), confirmPasswordField.textProperty()
        ));
        clearBtn.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        isAllEmpty(firstNameField, lastNameField, emailField, usernameField, passwordField, confirmPasswordField),
                firstNameField.textProperty(), lastNameField.textProperty(), emailField.textProperty(),
                usernameField.textProperty(), passwordField.textProperty(), confirmPasswordField.textProperty()
        ));
    }

    @FXML
    private void handleRegister() {
        firstNameField.setText(firstNameField.getText().trim());
        lastNameField.setText(lastNameField.getText().trim());
        emailField.setText(emailField.getText().trim().toLowerCase());
        usernameField.setText(usernameField.getText().trim().replaceAll("\\s+", ""));
        if (isInputInvalid()) {
            return;
        }
        UserDTO newUser = UserDTO.builder()
                .firstName(firstNameField.getText())
                .lastName(lastNameField.getText())
                .email(emailField.getText())
                .username(usernameField.getText())
                .role(UserRole.USER)
                .build();
        showInfo("Czytelnik " + newUser.getFullName() + " został dodany.", Alert.AlertType.INFORMATION);
        handleClear();
    }

    @FXML
    private void handleClear() {
        for (TextInputControl f : new TextInputControl[]{firstNameField, lastNameField, emailField, usernameField, passwordField, confirmPasswordField})
            f.clear();
    }

    private boolean isInputInvalid() {
        String fName = firstNameField.getText(), lName = lastNameField.getText(),
                uName = usernameField.getText(), email = emailField.getText(),
                pass = passwordField.getText(), confirm = confirmPasswordField.getText();
        if (fName.isBlank()) showInfo("Wypełnij pole imienia.", Alert.AlertType.ERROR);
        else if (lName.isBlank()) showInfo("Wypełnij pole nazwiska.", Alert.AlertType.ERROR);
        else if (uName.isBlank()) showInfo("Wypełnij pole loginu.", Alert.AlertType.ERROR);
        else if (!email.contains("@")) showInfo("Email powinien posiadać znak małpy (@).", Alert.AlertType.ERROR);
        else if (pass.length() < 5) showInfo("Hasło musi być dłuższe niż 5 znaki", Alert.AlertType.ERROR);
        else if (!pass.equals(confirm)) showInfo("Hasła nie są identyczne.", Alert.AlertType.ERROR);
        else if (uName.length() < 3) showInfo("Login musi mieć co najmniej 3 znaki.", Alert.AlertType.ERROR);
        else return false;
        return true;
    }

    private void showInfo(String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message);
        alert.setHeaderText(null);
        alert.setTitle(type == Alert.AlertType.INFORMATION ? "Sukces" : "Błąd");
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        pane.getStyleClass().add("root-container");
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.getStyleClass().add("button-primary");
            okBtn.setText("Rozumiem");
        }
        alert.showAndWait();
    }

    private boolean isAnyBlank(TextInputControl... fields) {
        for (TextInputControl f : fields) if (f.getText().trim().isEmpty()) return true;
        return false;
    }

    private boolean isAllEmpty(TextInputControl... fields) {
        for (TextInputControl f : fields) if (!f.getText().isEmpty()) return false;
        return true;
    }
}