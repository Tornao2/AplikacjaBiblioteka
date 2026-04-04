package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.LoanDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class LoansController {

    @FXML private TableView<LoanDTO> loanTable;
    @FXML private TableColumn<LoanDTO, String> colLoanTitle, colStatus;
    @FXML private TableColumn<LoanDTO, LocalDate> colDueDate;
    @FXML private TableColumn<LoanDTO, String> payDue;
    @FXML private Button prolongBtn;
    @FXML private Label totalLoansLabel, overdueCountLabel, booksReturnedLabel;

    private final ObservableList<LoanDTO> loanData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colLoanTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookTitle()));
        colDueDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        payDue.setCellValueFactory(d -> new SimpleStringProperty());
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        loanTable.setItems(loanData);
        loanTable.setPlaceholder(new Label("Brak wypożyczeń."));
        loadMockLoans();
        setupRowFactory();
        prolongBtn.disableProperty().bind(loanTable.getSelectionModel().selectedItemProperty().isNull()
                .or(javafx.beans.binding.Bindings.createBooleanBinding(
                        () -> {
                            LoanDTO s = loanTable.getSelectionModel().getSelectedItem();
                            return s != null && (s.getReturnDate() != null || s.isExtended());
                        },
                        loanTable.getSelectionModel().selectedItemProperty()
                )));
        updateStatistics();
    }

    private void setupRowFactory() {
        loanTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LoanDTO i, boolean e) {
                super.updateItem(i, e);
                getStyleClass().removeAll("table-row-overdue", "table-row-returned");
                if (i != null && !e) {
                    if (i.getReturnDate() == null && LocalDate.now().isAfter(i.getDueDate())) getStyleClass().add("table-row-overdue");
                    else if (i.getReturnDate() != null) getStyleClass().add("table-row-returned");
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
        LoanDTO s = loanTable.getSelectionModel().getSelectedItem();
        if (s != null) {
            s.setExtended(true);
            s.setDueDate(s.getDueDate().plusDays(7));
            loanTable.refresh();
            updateStatistics();
            showAlert();
            loanTable.getSelectionModel().clearSelection();
        }
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Przedłużono termin.");
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        DialogPane p = alert.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        Button ok = (Button) p.lookupButton(ButtonType.OK);
        if (ok != null) {
            ok.getStyleClass().add("button-primary");
            ok.setText("Rozumiem");
        }
        alert.showAndWait();
    }

    private void loadMockLoans() {
        loanData.addAll(
                LoanDTO.builder().bookTitle("Rok 1984").dueDate(LocalDate.now().plusDays(5)).extended(false).overduePay(50L).build(),
                LoanDTO.builder().bookTitle("Hobbit").dueDate(LocalDate.now().minusDays(2)).extended(true).overduePay(50L).build(),
                LoanDTO.builder().bookTitle("Wiedźmin").dueDate(LocalDate.now().minusDays(10)).returnDate(LocalDate.now().minusDays(5)).extended(false).overduePay(50L).build()
        );
    }
}