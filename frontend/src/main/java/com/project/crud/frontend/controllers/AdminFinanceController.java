package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.FinanceDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;

public class AdminFinanceController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField amountField;
    @FXML private TextField descriptionArea;
    @FXML private Button saveButton;

    @FXML private TableView<FinanceDTO> financeTable;
    @FXML private TableColumn<FinanceDTO, LocalDate> colDate;
    @FXML private TableColumn<FinanceDTO, String> colType;
    @FXML private TableColumn<FinanceDTO, Double> colAmount;
    @FXML private TableColumn<FinanceDTO, String> colDesc;
    @FXML private TableColumn<FinanceDTO, Void> colActions;

    private final ObservableList<FinanceDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) amountField.setText(oldVal);
        });
    }

    private void setupTable() {
        colDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDate()));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        colAmount.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getAmount()));
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        setupActions();
        financeTable.setItems(masterData);
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edytuj");
            private final Button deleteBtn = new Button("Usuń");
            private final HBox container = new HBox(10, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("button-primary-table");
                deleteBtn.getStyleClass().add("button-outline-danger-table");
                container.setAlignment(Pos.CENTER);
                editBtn.setOnAction(event -> {
                    FinanceDTO finance = getTableView().getItems().get(getIndex());
                    showEditDialog(finance);
                });
                deleteBtn.setOnAction(event -> {
                    FinanceDTO finance = getTableView().getItems().get(getIndex());
                    handleDelete(finance);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @FXML
    private void handleSave() {
        if (typeComboBox.getValue() == null || amountField.getText().isEmpty()) {
            showSimpleAlert();
            return;
        }
        FinanceDTO newEntry = FinanceDTO.builder()
                .id((long) (masterData.size() + 1))
                .date(LocalDate.now())
                .type(typeComboBox.getValue())
                .amount(Double.parseDouble(amountField.getText()))
                .description(descriptionArea.getText())
                .build();
        masterData.add(newEntry);
        clearFields();
    }

    private void showEditDialog(FinanceDTO finance) {
        Dialog<FinanceDTO> dialog = new Dialog<>();
        dialog.setTitle("Edycja operacji finansowej");
        dialog.setHeaderText("Edytujesz wpis z dnia: " + finance.getDate());
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        pane.getStyleClass().add("root-container");
        ButtonType saveBtnType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);
        ((Button) pane.lookupButton(saveBtnType)).getStyleClass().add("button-primary");
        ((Button) pane.lookupButton(ButtonType.CANCEL)).getStyleClass().add("button-outline-danger");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));
        ComboBox<String> editType = new ComboBox<>(typeComboBox.getItems());
        editType.setValue(finance.getType());
        editType.setMaxWidth(Double.MAX_VALUE);
        TextField editAmount = new TextField(String.valueOf(finance.getAmount()));
        TextField editDesc = new TextField(finance.getDescription());
        grid.add(new Label("Typ:"), 0, 0);      grid.add(editType, 1, 0);
        grid.add(new Label("Kwota:"), 0, 1);    grid.add(editAmount, 1, 1);
        grid.add(new Label("Tytuł:"), 0, 2);     grid.add(editDesc, 1, 2);
        pane.setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                if (editType.getValue() == null || editAmount.getText().isEmpty()) return null;
                finance.setType(editType.getValue());
                finance.setAmount(Double.parseDouble(editAmount.getText()));
                finance.setDescription(editDesc.getText());
                return finance;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(res -> financeTable.refresh());
    }

    private void handleDelete(FinanceDTO finance) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie");
        alert.setHeaderText("Usunąć wpis finansowy?");
        styleAlert(alert);
        DialogPane pane = alert.getDialogPane();
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.getStyleClass().add("button-primary");
            okButton.setText("Tak, usuń");
        }
        Button cancelButton = (Button) pane.lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.getStyleClass().add("button-outline-danger");
            cancelButton.setText("Anuluj");
        }
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                masterData.remove(finance);
            }
        });
    }

    private void showSimpleAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Błąd walidacji");
        alert.setHeaderText(null);
        alert.setContentText("Proszę wypełnić typ transakcji oraz kwotę!");
        styleAlert(alert);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        pane.getStyleClass().add("root-container");
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        if (okBtn != null) okBtn.getStyleClass().add("button-primary");
    }

    @FXML private void handleCancel() { clearFields(); }
    private void clearFields() {
        typeComboBox.setValue(null);
        amountField.clear();
        descriptionArea.clear();
        financeTable.getSelectionModel().clearSelection();
    }
}