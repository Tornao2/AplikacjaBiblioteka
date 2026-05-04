package com.project.crud.frontend.controllers;

import com.project.crud.frontend.ApiClient;
import com.project.crud.frontend.model.*;
import javafx.application.Platform;
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
import java.util.List;
import java.util.Map;
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
    private ApiClient apiClient;

    @FXML
    public void initialize() {
        this.apiClient = new ApiClient(userTable);
        setupTableColumns();
        setupUserSearch();
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newUser) -> {
            if (newUser != null) showLoansForUser(newUser.getId());
            else userLoansTable.setItems(FXCollections.emptyObservableList());
        });
        returnBookBtn.disableProperty().bind(userLoansTable.getSelectionModel().selectedItemProperty().isNull());
        openPickerBtn.disableProperty().bind(userTable.getSelectionModel().selectedItemProperty().isNull());
        userTable.setPlaceholder(new Label("Brak użytkowników w systemie."));
        userLoansTable.setPlaceholder(new Label("Brak aktywnych wypożyczeń."));
        loadInitialDataFromApi();
    }

    private void setupTableColumns() {
        colUserName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colUserEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colBookTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookTitle()));
        colDueDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        payDue.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getOverduePay() != null ? d.getValue().getOverduePay() + " PLN" : "0 PLN"));
        Stream.of(userTable, userLoansTable).forEach(t -> t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY));
    }

    private void loadInitialDataFromApi() {
        apiClient.send("/users", "GET", null, UserDTO[].class)
                .thenAccept(users -> Platform.runLater(() -> {
                    if (users != null) {
                        allUsers.clear();
                        allUsers.addAll(List.of(users));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Nie udało się pobrać użytkowników: " + ApiClient.getErrorMessage(ex)));
                    return null;
                });
        apiClient.send("/books", "GET", null, BookDTO[].class)
                .thenAccept(books -> Platform.runLater(() -> {
                    if (books != null) {
                        masterInventory.clear();
                        masterInventory.addAll(List.of(books));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Nie udało się pobrać książek: " + ApiClient.getErrorMessage(ex)));
                    return null;
                });
        loadLoansFromApi();
    }

    private void loadLoansFromApi() {
        apiClient.send("/loans/admin", "GET", null, LoanDTO[].class)
                .thenAccept(loans -> Platform.runLater(() -> {
                    if (loans != null) {
                        allLoans.clear();
                        allLoans.addAll(List.of(loans));
                        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();
                        if (selectedUser != null) {
                            showLoansForUser(selectedUser.getId());
                        }
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Nie udało się pobrać wypożyczeń: " + ApiClient.getErrorMessage(ex)));
                    return null;
                });
    }

    @FXML
    private void handleOpenBookPicker() {
        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;
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
            Map<String, Long> payload = Map.of("userId", selectedUser.getId(), "bookId", book.getId());
            apiClient.send("/loans", "POST", payload, LoanDTO.class)
                    .thenAccept(newLoan -> Platform.runLater(() -> {
                        book.setStatus(BookStatus.Wypozyczona);
                        allLoans.add(newLoan);
                        showLoansForUser(selectedUser.getId());
                        showInfo("Pomyślnie wypożyczono książkę: " + book.getTitle());
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> showError("Nie można wypożyczyć: " + ApiClient.getErrorMessage(ex)));
                        return null;
                    });
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
        FilteredList<BookDTO> available = new FilteredList<>(masterInventory, b -> BookStatus.Dostepna.equals(b.getStatus()));
        search.textProperty().addListener((obs, old, val) -> {
            String f = val.toLowerCase().trim();
            available.setPredicate(b -> BookStatus.Dostepna.equals(b.getStatus()) && (f.isEmpty() ||
                    Stream.of(b.getTitle(), b.getAuthor(), b.getIsbn()).anyMatch(s -> s.toLowerCase().contains(f))));
        });
        table.setItems(available);
        return search;
    }

    @FXML
    private void handleReturnAction() {
        LoanDTO loan = userLoansTable.getSelectionModel().getSelectedItem();
        if (loan == null) return;
        apiClient.send("/loans/" + loan.getId() + "/return", "POST", null, Void.class)
                .thenAccept(v -> Platform.runLater(() -> {
                    loan.setReturnDate(LocalDate.now());
                    masterInventory.stream()
                            .filter(b -> b.getId().equals(loan.getBookId()))
                            .findFirst().ifPresent(b -> b.setStatus(BookStatus.Dostepna));
                    loadLoansFromApi();
                    showInfo("Zwrócono książkę.");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Nie udało się zwrócić książki: " + ApiClient.getErrorMessage(ex)));
                    return null;
                });
    }

    private void showLoansForUser(Long userId) {
        userLoansTable.setItems(new FilteredList<>(allLoans,
                loan -> loan.getUserId().equals(userId) && loan.getReturnDate() == null));
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
        Button can = (Button) p.lookupButton(p.getButtonTypes().get(1));
        can.getStyleClass().add("button-outline-danger");
        can.setText("Anuluj");
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        styleAlert(a, "Sukces");
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        styleAlert(a, "Błąd");
        a.showAndWait();
    }

    private void styleAlert(Alert a, String title) {
        a.setTitle(title);
        a.setHeaderText(null);
        DialogPane p = a.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        p.setMinWidth(400);
        for (javafx.scene.Node node : p.getChildrenUnmodifiable()) {
            if (node instanceof Label label) {
                label.setWrapText(true);
                label.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
            }
        }
        Button ok = (Button) p.lookupButton(ButtonType.OK);
        if (ok != null) { ok.getStyleClass().add("button-primary"); ok.setText("Rozumiem"); }
    }
}