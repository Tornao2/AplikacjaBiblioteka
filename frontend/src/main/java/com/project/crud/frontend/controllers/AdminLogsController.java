package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.SystemLogDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

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
        severityFilter.setItems(FXCollections.observableArrayList("WSZYSTKIE", "INFO", "WARNING", "CRITICAL"));
        severityFilter.setValue("WSZYSTKIE");
        loadMockLogs();
        setupLiveFiltering();
    }

    private void setupColumns() {
        colTimestamp.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getTimestamp()));
        colUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUser()));
        colAction.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAction()));
        colDetails.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDetails()));
        colSeverity.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSeverity()));
        colTimestamp.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime i, boolean e) {
                super.updateItem(i, e);
                setText((e || i == null) ? null : formatter.format(i));
            }
        });
        colDetails.setCellFactory(tc -> new TableCell<>() {
            private final Text t = new Text();
            { t.wrappingWidthProperty().bind(tc.widthProperty().subtract(20)); t.getStyleClass().add("text"); }
            @Override protected void updateItem(String i, boolean e) {
                super.updateItem(i, e);
                if (e || i == null) setGraphic(null);
                else { t.setText(i); t.fillProperty().bind(textFillProperty()); setGraphic(t); }
            }
        });
    }

    private void setupLiveFiltering() {
        FilteredList<SystemLogDTO> filtered = new FilteredList<>(masterData, p -> true);
        javafx.beans.value.ChangeListener<Object> l = (o, old, v) -> filtered.setPredicate(this::applyFilters);
        logSearchField.textProperty().addListener(l);
        severityFilter.valueProperty().addListener(l);
        dateFrom.valueProperty().addListener(l);
        dateTo.valueProperty().addListener(l);
        SortedList<SystemLogDTO> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(logTable.comparatorProperty());
        logTable.setItems(sorted);
    }

    private boolean applyFilters(SystemLogDTO log) {
        String s = logSearchField.getText() == null ? "" : logSearchField.getText().toLowerCase().trim();
        boolean txt = s.isEmpty() || Stream.of(log.getUser(), log.getAction(), log.getDetails())
                .anyMatch(f -> f != null && f.toLowerCase().contains(s));
        String sev = severityFilter.getValue();
        boolean mSev = "WSZYSTKIE".equals(sev) || log.getSeverity().equalsIgnoreCase(sev);
        LocalDate f = dateFrom.getValue(), t = dateTo.getValue(), ld = log.getTimestamp().toLocalDate();
        boolean mDate = (f == null || !ld.isBefore(f)) && (t == null || !ld.isAfter(t));
        return txt && mSev && mDate;
    }

    private void loadMockLogs() {
        masterData.addAll(
                new SystemLogDTO(LocalDateTime.now(), "admin", "LOGIN", "Pomyślne logowanie", "INFO"),
                new SystemLogDTO(LocalDateTime.now().minusMinutes(5), "marta_b", "BOOK_ADD", "Dodano: Solaris", "INFO"),
                new SystemLogDTO(LocalDateTime.now().minusHours(2), "system", "DB_ERROR", "Błąd połączenia", "CRITICAL"),
                new SystemLogDTO(LocalDateTime.now().minusDays(1), "jan_kowalski", "AUTH_FAIL", "Złe hasło", "WARNING")
        );
    }
}