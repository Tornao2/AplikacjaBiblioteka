package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.LoanDTO;
import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;

public class UserManagementController {
    @FXML private TextField userSearchField;
    @FXML private TableView<UserDTO> userTable;
    @FXML private TableColumn<UserDTO, String> colUserName, colUserEmail;
    @FXML private TableView<LoanDTO> userLoansTable;
    @FXML private TableColumn<LoanDTO, String> colBookTitle, colStatus;
    @FXML private TableColumn<LoanDTO, LocalDate> colDueDate;
    @FXML private Button returnBookBtn;
    private final ObservableList<UserDTO> allUsers = FXCollections.observableArrayList();
    private final ObservableList<LoanDTO> allLoans = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colUserName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadMockData();
        FilteredList<UserDTO> filteredUsers = new FilteredList<>(allUsers, p -> true);
        userSearchField.textProperty().addListener((obs, old, newVal) -> {
            filteredUsers.setPredicate(user -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String f = newVal.toLowerCase();
                return user.getFullName().toLowerCase().contains(f) || user.getEmail().toLowerCase().contains(f);
            });
        });
        userTable.setItems(filteredUsers);
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showLoansForUser(newSelection.getId());
            } else {
                userLoansTable.setItems(FXCollections.emptyObservableList());
            }
        });
        returnBookBtn.disableProperty().bind(
                userLoansTable.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    private void showLoansForUser(Long userId) {
        FilteredList<LoanDTO> userLoans = new FilteredList<>(allLoans,
                loan -> loan.getUserId().equals(userId));
        userLoansTable.setItems(userLoans);
    }

    private void loadMockData() {
        allUsers.add(new UserDTO(1L, "Jan", "Jan", "Kowalski","jan@wp.pl", UserRole.USER));
        allUsers.add(new UserDTO(2L, "Anna", "Anna", "Nowak","ania@gmail.com", UserRole.ADMIN));
        allLoans.add(LoanDTO.builder().userId(1L).bookTitle("Wiedźmin").dueDate(LocalDate.now().plusDays(5)).build());
        allLoans.add(LoanDTO.builder().userId(2L).bookTitle("Rok 1984").dueDate(LocalDate.now().minusDays(2)).build());
    }

    @FXML
    private void handleReturnAction() {
        LoanDTO selectedLoan = userLoansTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null || selectedLoan.getReturnDate() != null) return;
        selectedLoan.setReturnDate(LocalDate.now());
        System.out.println("Książka zwrócona: " + selectedLoan.getBookTitle());
        userLoansTable.refresh();
    }
}