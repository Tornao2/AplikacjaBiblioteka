package com.project.crud.frontend.controllers;

import com.project.crud.frontend.ApiClient;
import com.project.crud.frontend.model.LoanDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.List;

public class LoansController {
    @FXML private TableView<LoanDTO> loanTable;
    @FXML private TableColumn<LoanDTO, String> colLoanTitle, colStatus;
    @FXML private TableColumn<LoanDTO, LocalDate> colDueDate;
    @FXML private TableColumn<LoanDTO, String> payDue;
    @FXML private Button prolongBtn;
    @FXML private Label totalLoansLabel, overdueCountLabel, booksReturnedLabel;

    private final ObservableList<LoanDTO> loanData = FXCollections.observableArrayList();
    private ApiClient apiClient;

    @FXML
    public void initialize() {
        this.apiClient = new ApiClient(loanTable);
        colLoanTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookTitle()));
        colDueDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        payDue.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f zł", d.getValue().getOverduePayFormatted())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        loanTable.setItems(loanData);
        loanTable.setPlaceholder(new Label("Brak wypożyczeń."));
        loadLoansFromApi();
        setupRowFactory();
        prolongBtn.disableProperty().bind(loanTable.getSelectionModel().selectedItemProperty().isNull()
                .or(javafx.beans.binding.Bindings.createBooleanBinding(
                        () -> {
                            LoanDTO s = loanTable.getSelectionModel().getSelectedItem();
                            return s != null && (s.getReturnDate() != null || s.isExtended() || s.getDueDate().isBefore(LocalDate.now()));
                        },
                        loanTable.getSelectionModel().selectedItemProperty()
                )));
    }

    private void loadLoansFromApi() {
        apiClient.send("/loans", "GET", null, LoanDTO[].class)
                .thenAccept(loansArray -> {
                    if (loansArray != null) {
                        Platform.runLater(() -> {
                            loanData.clear();
                            loanData.addAll(List.of(loansArray));
                            updateStatistics();
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Nie udało się pobrać wypożyczeń: " + ApiClient.getErrorMessage(ex)));
                    return null;
                });
    }

    private void setupRowFactory() {
        loanTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LoanDTO i, boolean e) {
                super.updateItem(i, e);
                getStyleClass().removeAll("table-row-overdue", "table-row-returned");
                if (i != null && !e) {
                    if (i.getReturnDate() == null && i.getDueDate() != null && LocalDate.now().isAfter(i.getDueDate())) {
                        getStyleClass().add("table-row-overdue");
                    } else if (i.getReturnDate() != null) {
                        getStyleClass().add("table-row-returned");
                    }
                }
            }
        });
    }

    private void updateStatistics() {
        totalLoansLabel.setText("Wszystkie: " + loanData.size());
        overdueCountLabel.setText("Po terminie: " + loanData.stream().filter(LoanDTO::isOverdue).count());
        booksReturnedLabel.setText("Oddane: " + loanData.stream().filter(l -> l.getReturnDate() != null).count());
    }

    @FXML
    private void requestProlongation() {
        LoanDTO selected = loanTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            apiClient.send("/loans/" + selected.getId() + "/prolong", "POST", null, LoanDTO.class)
                    .thenAccept(updatedLoan -> {
                        if (updatedLoan != null) {
                            Platform.runLater(() -> {
                                int index = loanData.indexOf(selected);
                                if (index >= 0) {
                                    loanData.set(index, updatedLoan);
                                }
                                loanTable.refresh();
                                updateStatistics();
                                loanTable.getSelectionModel().clearSelection();
                            });
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> showError(ApiClient.getErrorMessage(ex)));
                        return null;
                    });
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        applyDialogStyles(alert);
        alert.showAndWait();
    }

    private void applyDialogStyles(Alert alert) {
        DialogPane p = alert.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        Button ok = (Button) p.lookupButton(ButtonType.OK);
        if (ok != null) {
            ok.getStyleClass().add("button-primary");
            ok.setText("Rozumiem");
        }
    }
}