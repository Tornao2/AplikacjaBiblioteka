package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.SystemLogDTO;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
        colDetails.setCellFactory(tc -> new TableCell<>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();
            {
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(20));
                text.getStyleClass().add("text");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    text.fillProperty().bind(textFillProperty());
                    setGraphic(text);
                }
            }
        });
    }

    private void setupLiveFiltering() {
        FilteredList<SystemLogDTO> filteredData = new FilteredList<>(masterData, p -> true);
        ChangeListener<Object> filterListener = (obs, old, val) -> filteredData.setPredicate(log -> {
            String searchText = logSearchField.getText() == null ? "" : logSearchField.getText().toLowerCase().trim();
            boolean matchesText = searchText.isEmpty() ||
                    log.getUser().toLowerCase().contains(searchText) ||
                    log.getAction().toLowerCase().contains(searchText) ||
                    log.getDetails().toLowerCase().contains(searchText);
            String selectedSev = severityFilter.getValue();
            boolean matchesSev = selectedSev == null || "WSZYSTKIE".equals(selectedSev) ||
                    log.getSeverity().equalsIgnoreCase(selectedSev);
            LocalDate dFrom = dateFrom.getValue();
            LocalDate dTo = dateTo.getValue();
            LocalDate logDate = log.getTimestamp().toLocalDate();
            boolean matchesDate = (dFrom == null || !logDate.isBefore(dFrom)) &&
                    (dTo == null || !logDate.isAfter(dTo));
            return matchesText && matchesSev && matchesDate;
        });
        logSearchField.textProperty().addListener(filterListener);
        severityFilter.valueProperty().addListener(filterListener);
        dateFrom.valueProperty().addListener(filterListener);
        dateTo.valueProperty().addListener(filterListener);
        SortedList<SystemLogDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(logTable.comparatorProperty());
        logTable.setItems(sortedData);
    }

    private void loadMockLogs() {
        masterData.addAll(
                new SystemLogDTO(LocalDateTime.now(), "admin", "LOGIN", "Pomyślne logowanie", "INFO"),
                new SystemLogDTO(LocalDateTime.now().minusMinutes(5), "marta_b", "BOOK_ADD", "Dodano: Solaris", "INFO"),
                new SystemLogDTO(LocalDateTime.now().minusHours(2), "system", "DB_ERROR", "Błąd połączenia połączenia połączenia połączenia", "CRITICAL"),
                new SystemLogDTO(LocalDateTime.now().minusDays(1), "jan_kowalski", "AUTH_FAIL", "Złe hasło", "WARNING")
        );
    }
}