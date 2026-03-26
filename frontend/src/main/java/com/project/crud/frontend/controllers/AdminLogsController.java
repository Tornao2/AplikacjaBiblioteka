package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.SystemLogDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminLogsController {

    @FXML private TableView<SystemLogDTO> logTable;
    @FXML private TableColumn<SystemLogDTO, LocalDateTime> colTimestamp;
    @FXML private TableColumn<SystemLogDTO, String> colUser, colAction, colDetails, colSeverity;
    @FXML private TextField logSearchField;
    @FXML private ComboBox<String> severityFilter;
    @FXML private DatePicker dateFrom, dateTo;

    private final ObservableList<SystemLogDTO> masterData = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        setupColumns();
        severityFilter.getItems().addAll("WSZYSTKIE", "INFO", "WARNING", "CRITICAL");
        severityFilter.setValue("WSZYSTKIE");
        loadMockLogs();
        setupLiveFiltering();
    }

    private void setupColumns() {
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
        colSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        colTimestamp.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatter.format(item));
            }
        });
    }

    private void setupLiveFiltering() {
        FilteredList<SystemLogDTO> filteredData = new FilteredList<>(masterData, p -> true);
        Runnable updatePredicate = () -> filteredData.setPredicate(log -> {
            String search = logSearchField.getText() == null ? "" : logSearchField.getText().toLowerCase();
            String sev = severityFilter.getValue();
            LocalDate dFrom = dateFrom.getValue(), dTo = dateTo.getValue(), logD = log.getTimestamp().toLocalDate();
            boolean matchesText = log.getUser().toLowerCase().contains(search) ||
                    log.getAction().toLowerCase().contains(search) ||
                    log.getDetails().toLowerCase().contains(search);
            boolean matchesSev = "WSZYSTKIE".equals(sev) || log.getSeverity().equals(sev);
            boolean matchesDate = (dFrom == null || !logD.isBefore(dFrom)) && (dTo == null || !logD.isAfter(dTo));
            return matchesText && matchesSev && matchesDate;
        });
        logSearchField.textProperty().addListener(o -> updatePredicate.run());
        severityFilter.valueProperty().addListener(o -> updatePredicate.run());
        dateFrom.valueProperty().addListener(o -> updatePredicate.run());
        dateTo.valueProperty().addListener(o -> updatePredicate.run());
        SortedList<SystemLogDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(logTable.comparatorProperty());
        logTable.setItems(sortedData);
    }

    @FXML private void handleClearLogs() { masterData.clear(); }

    @FXML
    private void handleExport() {
        if (logTable.getItems().isEmpty()) {
            showAlert("Błąd", "Brak danych do eksportu.");
            return;
        }
        File file = new File("logi_systemowe.csv");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Data;Uzytkownik;Akcja;Szczegoly;Poziom");
            for (SystemLogDTO log : logTable.getItems()) {
                writer.printf("%s;%s;%s;%s;%s%n", log.getTimestamp().format(formatter), log.getUser(),
                        log.getAction(), log.getDetails().replace(";", ","), log.getSeverity());
            }
            showAlert("Sukces", "Zapisano: " + file.getAbsolutePath());
        } catch (Exception e) {
            showAlert("Błąd", "Nie można utworzyć pliku CSV.");
        }
    }

    private void loadMockLogs() {
        masterData.addAll(
                new SystemLogDTO(LocalDateTime.now(), "admin", "LOGIN", "Pomyślne logowanie", "INFO"),
                new SystemLogDTO(LocalDateTime.now().minusMinutes(5), "marta_b", "BOOK_ADD", "Dodano: Solaris", "INFO"),
                new SystemLogDTO(LocalDateTime.now().minusHours(2), "system", "DB_ERROR", "Błąd połączenia", "CRITICAL"),
                new SystemLogDTO(LocalDateTime.now().minusDays(1), "jan_kowalski", "AUTH_FAIL", "Złe hasło", "WARNING")
        );
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        pane.getStyleClass().add("root-container");
        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        if (ok != null) {
            ok.getStyleClass().add("button-primary");
            ok.setText("Rozumiem");
        }
        alert.showAndWait();
    }
}