package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.LoanDTO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;

public class LoansController {

    @FXML private TableView<LoanDTO> loanTable;
    @FXML private TableColumn<LoanDTO, String> colLoanTitle, colStatus;
    @FXML private TableColumn<LoanDTO, LocalDate> colDueDate;
    @FXML private TableColumn<LoanDTO, Long> payDue;
    @FXML private Button prolongBtn;

    @FXML private Label totalLoansLabel, overdueCountLabel, booksReturnedLabel;

    private final ObservableList<LoanDTO> loanData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colLoanTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        payDue.setCellValueFactory(new PropertyValueFactory<>("overduePayFormatted"));
        colStatus.setCellValueFactory(cellData -> {
            LoanDTO loan = cellData.getValue();
            return new SimpleStringProperty(loan.getStatus());
        });
        loadMockLoans();
        loanTable.setItems(loanData);
        setupRowFactory();
        setupProlongButtonLogic();
        updateStatistics();
    }

    private void setupRowFactory() {
        loanTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LoanDTO item, boolean empty) {
                getStyleClass().removeAll("table-row-overdue", "table-row-returned");
                if (item != null && !empty) {
                    if (item.getReturnDate() == null && LocalDate.now().isAfter(item.getDueDate())) {
                        getStyleClass().add("table-row-overdue");
                    } else if (item.getReturnDate() != null) {
                        getStyleClass().add("table-row-returned");
                    }
                }
            }
        });
    }

    private void updateStatistics() {
        long total = loanData.size();
        long overdue = loanData.stream().filter(LoanDTO::isOverdue).count();
        long returned = loanData.stream().filter(l -> l.getReturnDate() != null).count();
        totalLoansLabel.setText("Wszystkie: " + total);
        overdueCountLabel.setText("Po terminie: " + overdue);
        booksReturnedLabel.setText("Oddane: " + returned);
    }

    private void setupProlongButtonLogic() {
        prolongBtn.setDisable(true);
        loanTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getReturnDate() == null) {
                prolongBtn.setDisable(newSelection.isExtended());
            } else {
                prolongBtn.setDisable(true);
            }
        });
    }

    @FXML
    private void requestProlongation() {
        LoanDTO selected = loanTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setExtended(true);
            selected.setDueDate(selected.getDueDate().plusDays(7));
            loanTable.refresh();
            updateStatistics();
            showAlert();
        }
    }

    private void loadMockLoans() {
        loanData.add(LoanDTO.builder().bookTitle("Rok 1984").dueDate(LocalDate.now().plusDays(5)).extended(false).overduePay(50L).build());
        loanData.add(LoanDTO.builder().bookTitle("Hobbit").dueDate(LocalDate.now().minusDays(2)).extended(true).overduePay(50L).build());
        loanData.add(LoanDTO.builder().bookTitle("Wiedźmin").dueDate(LocalDate.now().minusDays(10)).returnDate(LocalDate.now().minusDays(5)).extended(false).overduePay(50L).build());
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Przedłużono termin o 7 dni.");
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}