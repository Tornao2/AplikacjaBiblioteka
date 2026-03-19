package com.project.crud.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminSettingsController {

    @FXML private Spinner<Integer> loanDurationSpinner;
    @FXML private Spinner<Integer> userLimitSpinner;
    @FXML private TextField penaltyRateField;

    @FXML
    public void initialize() {
        loanDurationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 30));
        userLimitSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
        penaltyRateField.setText("0.50");
    }

    @FXML
    private void handleSaveSettings() {
        int duration = loanDurationSpinner.getValue();
        String penalty = penaltyRateField.getText();
        System.out.println("Zapisano: Czas=" + duration + " dni, Kara=" + penalty + " PLN");
        showAlert();
    }

    @FXML
    private void handleReset() {
        loanDurationSpinner.getValueFactory().setValue(30);
        penaltyRateField.setText("0.50");
        userLimitSpinner.getValueFactory().setValue(5);
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        alert.setContentText("Ustawienia systemowe zostały zaktualizowane.");
        alert.showAndWait();
    }
}