package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.BookDTO;
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
    @FXML private ComboBox<BookDTO> availableBooksCombo;
    @FXML private Button addLoanBtn;

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
        setupUserSearch();
        setupAvailableBooksCombo();
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newUser) -> {
            if (newUser != null) {
                showLoansForUser(newUser.getId());
            } else {
                userLoansTable.setItems(FXCollections.emptyObservableList());
            }
        });
        returnBookBtn.disableProperty().bind(userLoansTable.getSelectionModel().selectedItemProperty().isNull());
        addLoanBtn.disableProperty().bind(
                userTable.getSelectionModel().selectedItemProperty().isNull()
                        .or(availableBooksCombo.getSelectionModel().selectedItemProperty().isNull())
        );
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userLoansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setSkin(new javafx.scene.control.skin.TableViewSkin<>(userTable) {
            @Override
            protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
                return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset) + 15;
            }
        });
        userTable.setPlaceholder(new Label("Brak użytkowników w systemie."));
        userLoansTable.setPlaceholder(new Label("Brak wypożyczeń w systemie."));
    }

    private void setupAvailableBooksCombo() {
        FilteredList<BookDTO> availableBooks = new FilteredList<>(InventoryController.masterInventory,
                book -> "AVAILABLE".equals(book.getStatus()));
        availableBooksCombo.setItems(availableBooks);
        availableBooksCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BookDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitle() + " [" + item.getAuthor() + "]");
            }
        });
        availableBooksCombo.setButtonCell(availableBooksCombo.getCellFactory().call(null));
    }

    private void showLoansForUser(Long userId) {
        FilteredList<LoanDTO> userLoans = new FilteredList<>(allLoans,
                loan -> loan.getUserId().equals(userId));
        userLoansTable.setItems(userLoans);
    }

    @FXML
    private void handleNewLoan() {
        UserDTO user = userTable.getSelectionModel().getSelectedItem();
        BookDTO book = availableBooksCombo.getSelectionModel().getSelectedItem();
        if (user != null && book != null) {
            book.setStatus("RENTED");
            LoanDTO newLoan = LoanDTO.builder()
                    .userId(user.getId())
                    .bookId(book.getId())
                    .bookTitle(book.getTitle())
                    .dueDate(LocalDate.now().plusDays(14))
                    .loanDate(LocalDate.now())
                    .extended(false)
                    .build();

            allLoans.add(newLoan);
            availableBooksCombo.getSelectionModel().clearSelection();
            refreshTables();
        }
    }

    @FXML
    private void handleReturnAction() {
        LoanDTO loan = userLoansTable.getSelectionModel().getSelectedItem();
        if (loan == null || loan.getReturnDate() != null) return;
        loan.setReturnDate(LocalDate.now());
        InventoryController.masterInventory.stream()
                .filter(b -> b.getId().equals(loan.getBookId()))
                .findFirst()
                .ifPresent(b -> b.setStatus("AVAILABLE"));
        refreshTables();
    }

    private void refreshTables() {
        userTable.refresh();
        userLoansTable.refresh();
    }

    private void setupUserSearch() {
        FilteredList<UserDTO> filteredUsers = new FilteredList<>(allUsers, p -> true);
        userSearchField.textProperty().addListener((obs, old, newVal) -> filteredUsers.setPredicate(user -> {
            if (newVal == null || newVal.isEmpty()) return true;
            String f = newVal.toLowerCase();
            return user.getFullName().toLowerCase().contains(f) || user.getEmail().toLowerCase().contains(f);
        }));
        userTable.setItems(filteredUsers);
    }

    private void loadMockData() {
        allUsers.add(new UserDTO(1L, "Jan", "Jan", "Kowalski","jan@wp.pl", UserRole.USER));
        allUsers.add(new UserDTO(2L, "Anna", "Anna", "Nowak","ania@gmail.com", UserRole.ADMIN));
        allLoans.add(LoanDTO.builder().userId(1L).bookId(101L).bookTitle("Wiedźmin").dueDate(LocalDate.now().plusDays(5)).build());
        if (InventoryController.masterInventory.isEmpty()) {
            InventoryController.masterInventory.add(new BookDTO(102L, "Hobbit", "Tolkien", "123", "Fantasy", "AVAILABLE", "Opis", 1937));
        }
    }
}