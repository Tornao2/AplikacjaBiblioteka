package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.LoanDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;

public class ManagementController {

    @FXML private TableView<LoanDTO> requestTable;
    @FXML private TableColumn<LoanDTO, String> colUser, colBook;
    @FXML private TableColumn<LoanDTO, LocalDate> colCurrentDue;
    @FXML private Button approveBtn;

    private final ObservableList<LoanDTO> allLoans = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colUser.setCellValueFactory(new PropertyValueFactory<>("userFullName"));
        colBook.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colCurrentDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        loadAllLoans();
        requestTable.setItems(allLoans);
        approveBtn.disableProperty().bind(requestTable.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void handleApprove() {
        LoanDTO selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setDueDate(selected.getDueDate().plusDays(7));
            selected.setExtended(true);
            allLoans.remove(requestTable.getSelectionModel().getSelectedItem());
            requestTable.refresh();
            showInfo("Przedłużono termin zwrotu dla: " + selected.getBookTitle());
        }
    }

    @FXML
    private void handleReject() {
        LoanDTO selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            allLoans.remove(selected);
            System.out.println("Odrzucono prośbę dla: " + selected.getBookTitle());
        }
    }

    private void loadAllLoans() {
        allLoans.add(LoanDTO.builder()
                .userFullName("Jan Kowalski").bookTitle("Wiedźmin")
                .dueDate(LocalDate.now().plusDays(2)).extended(false).build());
        allLoans.add(LoanDTO.builder()
                .userFullName("Anna Nowak").bookTitle("Rok 1984")
                .dueDate(LocalDate.now().minusDays(1)).extended(false).build());
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("Sukces");
        alert.show();
    }
}