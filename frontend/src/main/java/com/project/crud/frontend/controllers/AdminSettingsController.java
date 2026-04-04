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
        apiClient.get("/settings", SystemSettingsDTO.class)
                .thenAccept(s -> Platform.runLater(() -> {
                    cachedSettings = s;
                    load(s);
                    MainController.setLoading(false);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        MainController.setLoading(false);
                        if (cachedSettings == null) show(Alert.AlertType.ERROR, "Błąd połączenia.");
                    });
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
        try {
            double rate = Double.parseDouble(penaltyRateField.getText());
            var toSave = SystemSettingsDTO.builder()
                    .maxLoanDuration(loanDurationSpinner.getValue())
                    .userLoanLimit(userLimitSpinner.getValue())
                    .dailyPenaltyRate(rate).build();
            apiClient.send("/settings", "PUT", toSave, SystemSettingsDTO.class)
                    .thenAccept(res -> Platform.runLater(() -> {
                        cachedSettings = toSave;
                        show(Alert.AlertType.INFORMATION, "Ustawienia zapisane.");
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> show(Alert.AlertType.WARNING, "Błąd walidacji serwera."));
                        return null;
                    });
        } catch (Exception e) { show(Alert.AlertType.ERROR, "Nieprawidłowy format stawki."); }
    }

    private void configureSpinner(Spinner<Integer> s, int max, int def) {
        s.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, def));
        s.setEditable(true);
        s.getEditor().textProperty().addListener((o, old, v) -> {
            if (!v.matches("\\d*")) s.getEditor().setText(old);
        });
    }

    private void show(Alert.AlertType t, String msg) {
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