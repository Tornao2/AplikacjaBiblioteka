package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.SystemSettingsDTO;
import com.project.crud.frontend.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.stream.Stream;

public class AdminSettingsController {
    @FXML private Spinner<Integer> loanDurationSpinner, userLimitSpinner;
    @FXML private TextField penaltyRateField;
    @FXML private Button saveBtn;

    private static SystemSettingsDTO cachedSettings = null;
    private ApiClient apiClient;

    @FXML
    public void initialize() {
        apiClient = new ApiClient(saveBtn);
        configureSpinner(loanDurationSpinner, 365, 14);
        configureSpinner(userLimitSpinner, 100, 5);
        penaltyRateField.textProperty().addListener((o, old, v) -> {
            if (!v.matches("\\d*(\\.\\d{0,2})?")) penaltyRateField.setText(old);
        });
        saveBtn.disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
                () -> penaltyRateField.getText().isEmpty() || penaltyRateField.getText().endsWith("."),
                penaltyRateField.textProperty()
        ));
        if (cachedSettings != null) load(cachedSettings);
        fetch();
    }

    private void fetch() {
        if (cachedSettings == null) MainController.setLoading(true);
        apiClient.send("/settings", "GET", null, SystemSettingsDTO.class)
                .thenAccept(s -> Platform.runLater(() -> {
                    MainController.setLoading(false);
                    if (s != null) {
                        cachedSettings = s;
                        load(s);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> MainController.setLoading(false));
                    return null;
                });
    }

    private void load(SystemSettingsDTO s) {
        if (s == null) return;
        loanDurationSpinner.getValueFactory().setValue(s.getMaxLoanDuration());
        userLimitSpinner.getValueFactory().setValue(s.getUserLoanLimit());
        penaltyRateField.setText(String.valueOf(s.getDailyPenaltyRate()));
    }

    @FXML
    private void handleSaveSettings() {
        Stream.of(loanDurationSpinner, userLimitSpinner).forEach(Spinner::commitValue);
        try {
            double rate = Double.parseDouble(penaltyRateField.getText());
            var dta = SystemSettingsDTO.builder()
                    .maxLoanDuration(loanDurationSpinner.getValue())
                    .userLoanLimit(userLimitSpinner.getValue())
                    .dailyPenaltyRate(rate).build();
            MainController.setLoading(true);
            apiClient.send("/settings", "PUT", dta, SystemSettingsDTO.class)
                    .thenAccept(res -> Platform.runLater(() -> {
                        MainController.setLoading(false);
                        cachedSettings = dta;
                        showAlert(Alert.AlertType.INFORMATION, "Ustawienia zostały zapisane.");
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            MainController.setLoading(false);
                            String cleanMsg = ApiClient.getErrorMessage(e);
                            showAlert(Alert.AlertType.ERROR, "Nie udało się zapisać ustawień: \n" + cleanMsg);
                        });
                        return null;
                    });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Nieprawidłowy format danych.");
        }
    }

    private void configureSpinner(Spinner<Integer> s, int max, int def) {
        s.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, def));
        s.setEditable(true);
        s.getEditor().textProperty().addListener((o, old, v) -> {
            if (!v.matches("\\d*")) s.getEditor().setText(old);
        });
    }

    private void showAlert(Alert.AlertType t, String msg) {
        Alert a = new Alert(t, msg);
        a.setHeaderText(null);
        DialogPane p = a.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        Button ok = (Button) p.lookupButton(ButtonType.OK);
        if (ok != null) { ok.getStyleClass().add("button-primary"); ok.setText("Rozumiem"); }
        a.showAndWait();
    }

    @FXML private void handleReset() { fetch(); }
}