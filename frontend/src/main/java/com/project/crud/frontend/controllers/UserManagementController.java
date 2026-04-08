package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.stream.Stream;

public class UserManagementController {
    @FXML private TextField userSearchField;
    @FXML private TableView<UserDTO> userTable;
    @FXML private TableColumn<UserDTO, String> colUserName, colUserEmail;
    @FXML private TableView<LoanDTO> userLoansTable;
    @FXML private TableColumn<LoanDTO, String> colBookTitle, colStatus;
    @FXML private TableColumn<LoanDTO, LocalDate> colDueDate;
    @FXML private TableColumn<LoanDTO, String> payDue;
    @FXML private Button returnBookBtn, openPickerBtn;

    private final ObservableList<UserDTO> allUsers = FXCollections.observableArrayList();
    private final ObservableList<LoanDTO> allLoans = FXCollections.observableArrayList();
    private final ObservableList<BookDTO> masterInventory = FXCollections.observableArrayList();

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
        colUserName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colUserEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colBookTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookTitle()));
        colDueDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        payDue.setCellValueFactory(d -> new SimpleStringProperty());
        Stream.of(userTable, userLoansTable).forEach(t -> t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY));
    }

    @FXML
    private void handleOpenBookPicker() {
        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();
        Dialog<BookDTO> dialog = new Dialog<>();
        styleDialog(dialog, "Wybierz książkę dla: " + selectedUser.getFullName());
        TableView<BookDTO> table = createPickerTable();
        TextField search = createPickerSearchField(table);
        VBox box = new VBox(15, new Label("Wyszukaj dostępną pozycję:"), search, table);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().get(0))
                .disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        dialog.setResultConverter(btn -> btn.getButtonData().isDefaultButton() ? table.getSelectionModel().getSelectedItem() : null);
        dialog.showAndWait().ifPresent(book -> {
            book.setStatus(BookStatus.RENTED);
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
        tCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        aCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));
        iCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIsbn()));
        table.getColumns().addAll(tCol, aCol, iCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TextField createPickerSearchField(TableView<BookDTO> table) {
        TextField search = new TextField();
        search.setPromptText("Szukaj po tytule, autorze lub ISBN...");
        search.getStyleClass().add("text-field");
        search.setPrefHeight(35);
        FilteredList<BookDTO> available = new FilteredList<>(masterInventory, b -> BookStatus.AVAILABLE.equals(b.getStatus()));
        search.textProperty().addListener((obs, old, val) -> {
            String f = val.toLowerCase().trim();
            available.setPredicate(b -> BookStatus.AVAILABLE.equals(b.getStatus()) && (f.isEmpty() ||
                    Stream.of(b.getTitle(), b.getAuthor(), b.getIsbn()).anyMatch(s -> s.toLowerCase().contains(f))));
        });
        table.setItems(available);
        return search;
    }

    @FXML
    private void handleReturnAction() {
        LoanDTO loan = userLoansTable.getSelectionModel().getSelectedItem();
        if (loan == null) return;
        loan.setReturnDate(LocalDate.now());
        masterInventory.stream()
                .filter(b -> b.getId().equals(loan.getBookId()))
                .findFirst().ifPresent(b -> b.setStatus(BookStatus.AVAILABLE));

        showLoansForUser(loan.getUserId());
        showInfo("Zwrócono książkę.");
    }

    private void showLoansForUser(Long userId) {
        userLoansTable.setItems(new FilteredList<>(allLoans,
                loan -> loan.getUserId().equals(userId) && "AKTYWNE".equals(loan.getStatus())));
    }

    private void setupUserSearch() {
        FilteredList<UserDTO> filtered = new FilteredList<>(allUsers, p -> true);
        userSearchField.textProperty().addListener((obs, old, val) -> {
            String f = val.toLowerCase().trim();
            filtered.setPredicate(u -> f.isEmpty() || u.getFullName().toLowerCase().contains(f) || u.getEmail().toLowerCase().contains(f));
        });
        userTable.setItems(filtered);
    }

    private void styleDialog(Dialog<?> d, String header) {
        d.setTitle("Nowe wypożyczenie");
        d.setHeaderText(header);
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        p.setPrefSize(700, 500);
        p.getButtonTypes().setAll(new ButtonType("Wypożycz", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        (p.lookupButton(p.getButtonTypes().get(0))).getStyleClass().add("button-primary");
        Button can = (Button) p.lookupButton(ButtonType.CANCEL);
        can.getStyleClass().add("button-outline-danger");
        can.setText("Anuluj");
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle("Sukces");
        a.setHeaderText(null);
        DialogPane p = a.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        Button ok = (Button) p.lookupButton(ButtonType.OK);
        if (ok != null) { ok.getStyleClass().add("button-primary"); ok.setText("Rozumiem"); }
        a.showAndWait();
    }

    private void loadMockData() {
        allUsers.addAll(
                new UserDTO(1L, "Jan", "Jan", "Kowalski","jan@wp.pl", UserRole.USER),
                new UserDTO(2L, "Anna", "Anna", "Nowak","ania@gmail.com", UserRole.ADMIN)
        );
        masterInventory.add(new BookDTO(105L, "Czysty Kod", "Robert C. Martin", "9788328", "Edukacja", BookStatus.AVAILABLE, "Podręcznik programowania", 2008));
        allLoans.add(LoanDTO.builder().userId(1L).bookId(101L).bookTitle("Wiedźmin").dueDate(LocalDate.now().plusDays(5)).overduePay(0L).build());
    }
}