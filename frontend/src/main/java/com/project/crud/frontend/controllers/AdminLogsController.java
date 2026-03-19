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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminLogsController {

    @FXML private TableView<SystemLogDTO> logTable;
    @FXML private TableColumn<SystemLogDTO, LocalDateTime> colTimestamp;
    @FXML private TableColumn<SystemLogDTO, String> colUser, colAction, colDetails, colSeverity;
    @FXML private TextField logSearchField;
    @FXML private ComboBox<String> severityFilter;

    private final ObservableList<SystemLogDTO> masterData = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        setupColumns();
        setupSeverityFilter();
        loadMockLogs();
        setupLiveFiltering();
    }

    private void setupColumns() {
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
        colSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        colTimestamp.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
    }

    private void setupSeverityFilter() {
        severityFilter.getItems().addAll("WSZYSTKIE", "INFO", "WARNING", "CRITICAL");
        severityFilter.setValue("WSZYSTKIE");
    }

    private void setupLiveFiltering() {
        FilteredList<SystemLogDTO> filteredData = new FilteredList<>(masterData, p -> true);
        logSearchField.textProperty().addListener((obs, old, val) -> applyPredicate(filteredData));
        severityFilter.valueProperty().addListener((obs, old, val) -> applyPredicate(filteredData));
        SortedList<SystemLogDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(logTable.comparatorProperty());
        logTable.setItems(sortedData);
    }

    private void applyPredicate(FilteredList<SystemLogDTO> filteredList) {
        filteredList.setPredicate(log -> {
            String searchText = logSearchField.getText() == null ? "" : logSearchField.getText().toLowerCase();
            String severity = severityFilter.getValue() == null ? "WSZYSTKIE" : severityFilter.getValue();
            boolean matchesSeverity = severity.equals("WSZYSTKIE") || log.getSeverity().equals(severity);
            boolean matchesText = log.getUser().toLowerCase().contains(searchText) ||
                    log.getAction().toLowerCase().contains(searchText) ||
                    log.getDetails().toLowerCase().contains(searchText);
            return matchesSeverity && matchesText;
        });
    }

    @FXML
    private void handleClearLogs() {
        if (masterData.isEmpty()) return;
        masterData.clear();
        showAlert("Sukces", "Dziennik zdarzeń został wyczyszczony.");
    }

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
                writer.printf("%s;%s;%s;%s;%s%n",
                        log.getTimestamp().format(formatter),
                        log.getUser(),
                        log.getAction(),
                        log.getDetails().replace(";", ","), // Zabezpieczenie formatu CSV
                        log.getSeverity()
                );
            }
            showAlert("Eksport zakończony", "Zapisano do pliku: " + file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            showAlert("Błąd krytyczny", "Nie można utworzyć pliku CSV.");
            e.printStackTrace();
        }
    }

    private void loadMockLogs() {
        masterData.add(new SystemLogDTO(LocalDateTime.now(), "admin", "LOGIN", "Pomyślne logowanie", "INFO"));
        masterData.add(new SystemLogDTO(LocalDateTime.now().minusMinutes(5), "marta_b", "BOOK_ADD", "Dodano: Solaris", "INFO"));
        masterData.add(new SystemLogDTO(LocalDateTime.now().minusHours(2), "system", "DB_ERROR", "Błąd połączenia z MySQL", "CRITICAL"));
        masterData.add(new SystemLogDTO(LocalDateTime.now().minusDays(1), "jan_kowalski", "AUTH_FAIL", "Niepoprawne hasło", "WARNING"));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}