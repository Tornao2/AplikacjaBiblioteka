package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.SystemSettingsDTO;
import com.project.crud.frontend.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminSettingsController {
    @FXML private Spinner<Integer> loanDurationSpinner, userLimitSpinner;
    @FXML private TextField penaltyRateField;
    @FXML private Button saveBtn;
    private static SystemSettingsDTO cachedSettings = null;
    private ApiClient apiClient;

    @FXML
    public void initialize() {
        loanDurationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 14)
        );
        userLimitSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 5)
        );
        this.apiClient = new ApiClient(saveBtn);
        setupListeners();
        fetchSettings();
    }

    private void fetchSettings() {
        if (cachedSettings != null) {
            loadSettings(cachedSettings);
        } else {
            MainController.setLoading(true);
        }
        apiClient.get("/settings", SystemSettingsDTO.class)
                .thenAccept(settings -> Platform.runLater(() -> {
                    cachedSettings = settings;
                    loadSettings(settings);
                    MainController.setLoading(false);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        MainController.setLoading(false);
                        if (cachedSettings == null) {
                            showAlert(Alert.AlertType.ERROR, "Błąd", "Błąd połączenia.");
                        }
                    });
                    return null;
                });
    }

    private void loadSettings(SystemSettingsDTO settings) {
        if (settings == null) return;
        loanDurationSpinner.getValueFactory().setValue(settings.getMaxLoanDuration());
        userLimitSpinner.getValueFactory().setValue(settings.getUserLoanLimit());
        penaltyRateField.setText(String.valueOf(settings.getDailyPenaltyRate()));
    }

    @FXML
    private void handleSaveSettings() {
        try {
            double penaltyRate = Double.parseDouble(penaltyRateField.getText());
            if (penaltyRate < 0) {
                showAlert(Alert.AlertType.WARNING, "Błąd danych", "Stawka kary nie może być ujemna.");
                return;
            }
            SystemSettingsDTO settingsToSave = SystemSettingsDTO.builder()
                    .maxLoanDuration(loanDurationSpinner.getValue())
                    .userLoanLimit(userLimitSpinner.getValue())
                    .dailyPenaltyRate(penaltyRate)
                    .build();
            apiClient.send("/settings", "PUT", settingsToSave, SystemSettingsDTO.class)
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res != null) {
                            Platform.runLater(() -> {
                                cachedSettings = settingsToSave;
                                showAlert(Alert.AlertType.INFORMATION, "Sukces", "Ustawienia zostały zapisane.");
                            });
                        }
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            if (e.getMessage().contains("400")) {
                                showAlert(Alert.AlertType.WARNING, "Błąd walidacji",
                                        "Dane są poza zakresem.");
                            }
                        });
                        return null;
                    });
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd formatu", "Wprowadzona stawka kary jest nieprawidłowa.");
        }
    }

    @FXML
    private void handleReset() {
        fetchSettings();
    }

    private void setupListeners() {
        penaltyRateField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*(\\.\\d{0,2})?")) {
                penaltyRateField.setText(old);
            }
            updateSaveButtonState();
        });
        configureSpinner(loanDurationSpinner, 365);
        configureSpinner(userLimitSpinner, 100);
    }
    private void configureSpinner(Spinner<Integer> spinner, int max) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, spinner.getValue()));
        spinner.getEditor().textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) {
                spinner.getEditor().setText(old);
                return;
            }
            if (!val.isEmpty()) {
                int num = Integer.parseInt(val);
                if (num > max) spinner.getEditor().setText(String.valueOf(max));
                if (num < 1) spinner.getEditor().setText(String.valueOf(1));
            }
            updateSaveButtonState();
        });
        spinner.valueProperty().addListener((obs, old, val) -> updateSaveButtonState());
    }

    private void updateSaveButtonState() {
        try {
            String pStr = penaltyRateField.getText();
            String ldStr = loanDurationSpinner.getEditor().getText();
            String ulStr = userLimitSpinner.getEditor().getText();
            if (pStr.isEmpty() || ldStr.isEmpty() || ulStr.isEmpty() || pStr.endsWith(".")) {
                saveBtn.setDisable(true);
                return;
            }
            double penalty = Double.parseDouble(pStr);
            int duration = Integer.parseInt(ldStr);
            int limit = Integer.parseInt(ulStr);
            boolean isInvalid = penalty <= 0 || penalty > 1000
                    || duration < 1 || duration > 365
                    || limit < 1 || limit > 100;
            saveBtn.setDisable(isInvalid);
        } catch (NumberFormatException e) {
            saveBtn.setDisable(true);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type, msg);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dialogPane.getStyleClass().add("root-container");
        alert.setTitle(title);
        alert.setHeaderText(null);
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.getStyleClass().add("button-primary");
            okButton.applyCss();
            okButton.setText("Rozumiem");
        }
        alert.showAndWait();
    }
}