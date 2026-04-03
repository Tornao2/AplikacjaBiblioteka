package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.FinanceDTO;
import com.project.crud.frontend.model.FinanceType;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java.time.LocalDate;

public class AdminFinanceController {
    @FXML private ComboBox<FinanceType> typeComboBox;
    @FXML private TextField amountField, descriptionArea;
    @FXML private Button saveButton;
    @FXML private DatePicker filterDateFrom, filterDateTo;
    @FXML private Label countLabel, incomeLabel, expenseLabel;
    @FXML private TableView<FinanceDTO> financeTable;
    @FXML private TableColumn<FinanceDTO, LocalDate> colDate;
    @FXML private TableColumn<FinanceDTO, FinanceType> colType;
    @FXML private TableColumn<FinanceDTO, String> colDesc;
    @FXML private TableColumn<FinanceDTO, Double> colAmount;
    @FXML private TableColumn<FinanceDTO, Void> colActions;

    private final ObservableList<FinanceDTO> masterData = FXCollections.observableArrayList();
    private FilteredList<FinanceDTO> filteredData;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(FinanceType.values()));
        setupTable();
        setupFiltering();
        amountField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*(\\.\\d*)?")) amountField.setText(old);
        });
        saveButton.disableProperty().bind(
                typeComboBox.valueProperty().isNull()
                        .or(amountField.textProperty().isEmpty())
                        .or(descriptionArea.textProperty().isEmpty())
        );
    }

    private void setupTable() {
        colDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDate()));
        colType.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getType()));
        colAmount.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getAmount()));
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        setupActions();
        financeTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(FinanceDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (FinanceType.INCOME.equals(item.getType())) {
                    setStyle("-fx-background-color: rgba(39, 174, 96, 0.15);");
                } else if (FinanceType.EXPENSE.equals(item.getType())) {
                    setStyle("-fx-background-color: rgba(231, 76, 60, 0.15);");
                }
            }
        });
        financeTable.setPlaceholder(new Label("Brak transakcji w systemie."));
    }

    private void setupFiltering() {
        filteredData = new FilteredList<>(masterData, p -> true);
        filterDateFrom.valueProperty().addListener(o -> updateFilterAndStats());
        filterDateTo.valueProperty().addListener(o -> updateFilterAndStats());
        SortedList<FinanceDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(financeTable.comparatorProperty());
        financeTable.setItems(sortedData);
        updateFilterAndStats();
    }

    private void updateFilterAndStats() {
        filteredData.setPredicate(item -> {
            LocalDate from = filterDateFrom.getValue(), to = filterDateTo.getValue(), date = item.getDate();
            return (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
        });
        double income = filteredData.stream()
                .filter(i -> FinanceType.INCOME.equals(i.getType()))
                .mapToDouble(FinanceDTO::getAmount).sum();
        double expense = filteredData.stream()
                .filter(i -> FinanceType.EXPENSE.equals(i.getType()))
                .mapToDouble(FinanceDTO::getAmount).sum();
        countLabel.setText("Operacji: " + filteredData.size());
        incomeLabel.setText(String.format("Dochody: %.2f PLN", income));
        expenseLabel.setText(String.format("Straty: %.2f PLN", expense));
    }

    private void setupActions() {
        colActions.setCellFactory(p -> new TableCell<>() {
            private final Button edit = new Button("Edytuj"), del = new Button("Usuń");
            private final HBox container = new HBox(10, edit, del);
            {
                edit.getStyleClass().add("button-primary-table");
                del.getStyleClass().add("button-outline-danger-table");
                container.setAlignment(Pos.CENTER);
                edit.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void i, boolean e) {
                super.updateItem(i, e);
                setGraphic(e ? null : container);
            }
        });
    }

    @FXML
    private void handleSave() {
        masterData.add(FinanceDTO.builder()
                .id(System.currentTimeMillis())
                .date(LocalDate.now())
                .type(typeComboBox.getValue())
                .amount(Double.parseDouble(amountField.getText()))
                .description(descriptionArea.getText())
                .build());
        updateFilterAndStats();
        clearFields();
    }

    private void showEditDialog(FinanceDTO f) {
        Dialog<FinanceDTO> dialog = new Dialog<>();
        dialog.setTitle("Edycja wpisu");
        ButtonType saveType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleControl(dialog, "Zapisz");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        ComboBox<FinanceType> eType = new ComboBox<>(FXCollections.observableArrayList(FinanceType.values()));
        eType.setValue(f.getType());
        TextField eAmount = new TextField(String.valueOf(f.getAmount()));
        TextField eDesc = new TextField(f.getDescription());
        eAmount.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*(\\.\\d*)?")) eAmount.setText(old);
        });
        grid.add(new Label("Typ:"), 0, 0); grid.add(eType, 1, 0);
        grid.add(new Label("Kwota:"), 0, 1); grid.add(eAmount, 1, 1);
        grid.add(new Label("Tytuł:"), 0, 2); grid.add(eDesc, 1, 2);
        dialog.getDialogPane().setContent(grid);
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.disableProperty().bind(
                eType.valueProperty().isNull()
                        .or(eAmount.textProperty().isEmpty())
                        .or(eDesc.textProperty().isEmpty())
        );
        dialog.setResultConverter(b -> b == saveType ? updateFinance(f, eType, eAmount, eDesc) : null);
        dialog.showAndWait().ifPresent(r -> {
            financeTable.refresh();
            updateFilterAndStats();
        });
    }

    private FinanceDTO updateFinance(FinanceDTO f, ComboBox<FinanceType> t, TextField a, TextField d) {
        f.setType(t.getValue());
        f.setAmount(Double.parseDouble(a.getText()));
        f.setDescription(d.getText());
        return f;
    }

    private void handleDelete(FinanceDTO f) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć wpis: " + f.getDescription() + "?");
        styleControl(alert, "Tak, usuń");
        alert.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { masterData.remove(f); updateFilterAndStats(); }});
    }

    private void styleControl(Dialog<?> d, String okT) {
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        d.setHeaderText(null);
        Button ok = (Button) p.lookupButton(p.getButtonTypes().get(0));
        if (ok != null) { ok.getStyleClass().add("button-primary"); ok.setText(okT); }
        Button can = (Button) p.lookupButton(ButtonType.CANCEL);
        if (can != null) { can.getStyleClass().add("button-outline-danger"); can.setText("Anuluj"); }
    }

    @FXML private void handleCancel() { clearFields(); }

    private void clearFields() {
        typeComboBox.getSelectionModel().select(-1);
        typeComboBox.setValue(null);
        typeComboBox.setButtonCell(new ListCell<FinanceType>() {
            @Override
            protected void updateItem(FinanceType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(typeComboBox.getPromptText());
                } else {
                    setText(item.toString());
                }
            }
        });
        amountField.clear();
        descriptionArea.clear();
        financeTable.getSelectionModel().clearSelection();
    }
}