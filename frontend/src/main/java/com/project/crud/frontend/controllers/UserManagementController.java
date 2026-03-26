package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.BookDTO;
import com.project.crud.frontend.model.LoanDTO;
import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.time.LocalDate;

public class UserManagementController {
    @FXML private TextField userSearchField;
    @FXML private TableView<UserDTO> userTable;
    @FXML private TableColumn<UserDTO, String> colUserName, colUserEmail;
    @FXML private TableView<LoanDTO> userLoansTable;
    @FXML private TableColumn<LoanDTO, String> colBookTitle, colStatus;
    @FXML private TableColumn<LoanDTO, LocalDate> colDueDate;
    @FXML private TableColumn<LoanDTO, Long> payDue;
    @FXML private Button returnBookBtn, openPickerBtn;

    private final ObservableList<UserDTO> allUsers = FXCollections.observableArrayList();
    private final ObservableList<LoanDTO> allLoans = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadMockData();
        setupUserSearch();
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newUser) -> {
            if (newUser != null) showLoansForUser(newUser.getId());
            else userLoansTable.setItems(FXCollections.emptyObservableList());
        });
        returnBookBtn.disableProperty().bind(userLoansTable.getSelectionModel().selectedItemProperty().isNull());
        openPickerBtn.disableProperty().bind(userTable.getSelectionModel().selectedItemProperty().isNull());
        userTable.setPlaceholder(new Label("Brak użytkowników w systemie."));
        userLoansTable.setPlaceholder(new Label("Brak aktywnych wypożyczeń."));
    }

    private void setupTableColumns() {
        colUserName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        payDue.setCellValueFactory(new PropertyValueFactory<>("overduePayFormatted"));
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userLoansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleOpenBookPicker() {
        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();
        Dialog<BookDTO> dialog = new Dialog<>();
        dialog.setTitle("Nowe wypożyczenie");
        dialog.setHeaderText("Wybierz książkę dla: " + selectedUser.getFullName());
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        pane.getStyleClass().add("root-container");
        pane.setPrefSize(700, 500);
        ButtonType loanBtnType = new ButtonType("Wypożycz", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(loanBtnType, ButtonType.CANCEL);
        (pane.lookupButton(loanBtnType)).getStyleClass().add("button-primary");
        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelBtn.getStyleClass().add("button-outline-danger");
        cancelBtn.setText("Anuluj");
        TableView<BookDTO> table = createPickerTable();
        TextField search = createPickerSearchField(table);
        VBox box = new VBox(15, new Label("Wyszukaj dostępną pozycję:"), search, table);
        box.setPadding(new Insets(10));
        pane.setContent(box);
        pane.lookupButton(loanBtnType).disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        dialog.setResultConverter(btn -> btn == loanBtnType ? table.getSelectionModel().getSelectedItem() : null);
        dialog.showAndWait().ifPresent(book -> {
            book.setStatus("RENTED");
            allLoans.add(LoanDTO.builder()
                    .userId(selectedUser.getId()).bookId(book.getId()).bookTitle(book.getTitle())
                    .dueDate(LocalDate.now().plusDays(14)).loanDate(LocalDate.now()).overduePay(0L).build());
            showLoansForUser(selectedUser.getId());
            showInfo("Pomyślnie wypożyczono książkę: " + book.getTitle());
        });
    }

    private TableView<BookDTO> createPickerTable() {
        TableView<BookDTO> table = new TableView<>();
        TableColumn<BookDTO, String> tCol = new TableColumn<>("Tytuł"), aCol = new TableColumn<>("Autor"), iCol = new TableColumn<>("ISBN");
        tCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        aCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        iCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        table.getColumns().addAll(tCol, aCol, iCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TextField createPickerSearchField(TableView<BookDTO> table) {
        TextField search = new TextField();
        search.setPromptText("Szukaj po tytule, autorze lub ISBN...");
        search.getStyleClass().add("text-field");
        search.setPrefHeight(35);
        FilteredList<BookDTO> available = new FilteredList<>(InventoryController.masterInventory, b -> "AVAILABLE".equals(b.getStatus()));
        search.textProperty().addListener((obs, old, val) -> available.setPredicate(book -> {
            if (val == null || val.isEmpty()) return "AVAILABLE".equals(book.getStatus());
            String f = val.toLowerCase();
            return "AVAILABLE".equals(book.getStatus()) && (book.getTitle().toLowerCase().contains(f) ||
                    book.getAuthor().toLowerCase().contains(f) || book.getIsbn().toLowerCase().contains(f));
        }));
        table.setItems(available);
        return search;
    }

    @FXML
    private void handleReturnAction() {
        LoanDTO loan = userLoansTable.getSelectionModel().getSelectedItem();
        if (loan == null) return;
        loan.setReturnDate(LocalDate.now());
        InventoryController.masterInventory.stream()
                .filter(b -> b.getId().equals(loan.getBookId()))
                .findFirst().ifPresent(b -> b.setStatus("AVAILABLE"));
        if (userLoansTable.getItems() instanceof FilteredList<LoanDTO> filtered) {
            var pred = filtered.getPredicate();
            filtered.setPredicate(null);
            filtered.setPredicate(pred);
        }
        showInfo("Zwrócono książkę.");
    }

    private void showLoansForUser(Long userId) {
        userLoansTable.setItems(new FilteredList<>(allLoans,
                loan -> loan.getUserId().equals(userId) && "AKTYWNE".equals(loan.getStatus())));
    }

    private void setupUserSearch() {
        FilteredList<UserDTO> filteredUsers = new FilteredList<>(allUsers, p -> true);
        userSearchField.textProperty().addListener((obs, old, val) -> filteredUsers.setPredicate(user -> {
            if (val == null || val.isEmpty()) return true;
            String f = val.toLowerCase();
            return user.getFullName().toLowerCase().contains(f) || user.getEmail().toLowerCase().contains(f);
        }));
        userTable.setItems(filteredUsers);
    }

    private void loadMockData() {
        allUsers.addAll(
                new UserDTO(1L, "Jan", "Jan", "Kowalski","jan@wp.pl", UserRole.USER),
                new UserDTO(2L, "Anna", "Anna", "Nowak","ania@gmail.com", UserRole.ADMIN)
        );
        InventoryController.masterInventory.add(new BookDTO(105L, "Czysty Kod", "Robert C. Martin", "9788328", "Edukacja", "AVAILABLE", "Podręcznik programowania", 2008));
        allLoans.add(LoanDTO.builder().userId(1L).bookId(101L).bookTitle("Wiedźmin").dueDate(LocalDate.now().plusDays(5)).overduePay(0L).build());
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        DialogPane dp = alert.getDialogPane();
        dp.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dp.getStyleClass().add("root-container");
        Button ok = (Button) dp.lookupButton(ButtonType.OK);
        if (ok != null) {
            ok.getStyleClass().add("button-primary");
            ok.setText("Rozumiem");
        }
        alert.showAndWait();
    }
}