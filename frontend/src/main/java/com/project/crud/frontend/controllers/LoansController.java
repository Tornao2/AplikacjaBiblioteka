package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.LoanDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;

public class LoansController {

    @FXML private TableView<LoanDTO> loanTable;
    @FXML private TableColumn<LoanDTO, String> colLoanTitle;
    @FXML private TableColumn<LoanDTO, LocalDate> colDueDate;
    @FXML private Button prolongBtn;

    private final ObservableList<LoanDTO> loanData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colLoanTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        loadMockLoans();
        loanTable.setItems(loanData);
        setupProlongButtonLogic();
    }

    private void setupProlongButtonLogic() {
        prolongBtn.setDisable(true);
        loanTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
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
            System.out.println("Wysłano prośbę o przedłużenie dla: " + selected.getBookTitle());
            selected.setExtended(true);
            selected.setDueDate(selected.getDueDate().plusDays(7));
            loanTable.refresh();
            prolongBtn.setDisable(true);
            showAlert("Prośba wysłana", "Twoja prośba o przedłużenie terminu została zaakceptowana (symulacja).");
        }
    }
    private void loadMockLoans() {
        loanData.add(LoanDTO.builder()
                .id(1L)
                .bookId(2L)
                .userId(1L)
                .bookTitle("Rok 1984")
                .bookAuthor("George Orwell")
                .userFullName("Jan Kowalski")
                .userEmail("user@wp.pl")
                .loanDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .extended(false)
                .build());
        loanData.add(LoanDTO.builder()
                .id(2L)
                .bookId(5L)
                .userId(1L)
                .bookTitle("Hobbit")
                .bookAuthor("J.R.R. Tolkien")
                .userFullName("Jan Kowalski")
                .userEmail("user@wp.pl")
                .loanDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().plusDays(2))
                .extended(true)
                .build());
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}