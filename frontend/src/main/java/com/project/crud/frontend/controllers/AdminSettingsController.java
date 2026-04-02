package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.SystemSettingsDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminSettingsController {
    @FXML private Spinner<Integer> loanDurationSpinner, userLimitSpinner;
    @FXML private TextField penaltyRateField;
    @FXML private Button saveBtn;

    private SystemSettingsDTO currentSettings;

    @FXML
    public void initialize() {
        setupFactories();
        setupListeners();
        loadSettings(SystemSettingsDTO.builder()
                .maxLoanDuration(30)
                .userLoanLimit(5)
                .dailyPenaltyRate(0.50)
                .build());
    }

    private void setupFactories() {
        loanDurationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 30));
        userLimitSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
    }

    private void setupListeners() {
        penaltyRateField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*(\\.\\d*)?")) penaltyRateField.setText(old);
            updateSaveButtonState();
        });
        setupIntegerOnly(loanDurationSpinner);
        setupIntegerOnly(userLimitSpinner);
    }

    private void loadSettings(SystemSettingsDTO settings) {
        this.currentSettings = settings;
        loanDurationSpinner.getValueFactory().setValue(settings.getMaxLoanDuration());
        userLimitSpinner.getValueFactory().setValue(settings.getUserLoanLimit());
        penaltyRateField.setText(String.valueOf(settings.getDailyPenaltyRate()));
    }

    private void setupIntegerOnly(Spinner<Integer> s) {
        s.getEditor().textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) s.getEditor().setText(old);
            updateSaveButtonState();
        });
    }

    private void updateSaveButtonState() {
        String p = penaltyRateField.getText();
        boolean pInv = p.isEmpty() || p.endsWith(".") || p.equals("0") || p.equals("0.0");
        boolean sEmpty = loanDurationSpinner.getEditor().getText().isEmpty() ||
                userLimitSpinner.getEditor().getText().isEmpty();
        saveBtn.setDisable(pInv || sEmpty);
    }

    @FXML
    private void handleSaveSettings() {
        try {
            currentSettings.setMaxLoanDuration(Integer.parseInt(loanDurationSpinner.getEditor().getText()));
            currentSettings.setUserLoanLimit(Integer.parseInt(userLimitSpinner.getEditor().getText()));
            currentSettings.setDailyPenaltyRate(Double.parseDouble(penaltyRateField.getText()));
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Ustawienia zapisane w modelu DTO.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nieprawidłowe dane!");
        }
    }

    @FXML
    private void handleReset() {
        loadSettings(SystemSettingsDTO.builder()
                .maxLoanDuration(30)
                .userLoanLimit(5)
                .dailyPenaltyRate(0.50)
                .build());
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type, msg);
        alert.setTitle(title);
        alert.setHeaderText(null);
        DialogPane dp = alert.getDialogPane();
        dp.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dp.getStyleClass().add("root-container");
        Button ok = (Button) dp.lookupButton(ButtonType.OK);
        if (ok != null) ok.getStyleClass().add("button-primary");
        alert.showAndWait();
    }
}