package com.project.crud.frontend.controllers;

import com.project.crud.frontend.auth.UserSession;
import com.project.crud.frontend.model.BookDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CatalogController {

    @FXML private TextField searchField;
    @FXML private TableView<BookDTO> bookTable;
    @FXML private TableColumn<BookDTO, Long> colId;
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colStatus, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, Integer> colYear;
    @FXML private Button rentBtn;

    private final ObservableList<BookDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        loadMockData();
        setupSearch();
        UserRole role = UserSession.getInstance().getRole();
        boolean isStaff = (role == UserRole.LIBRARIAN || role == UserRole.ADMIN);
        rentBtn.setVisible(isStaff);
        rentBtn.setManaged(isStaff);
        if (isStaff) {
            setupButtonBinding();
        }
    }

    private void setupButtonBinding() {
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                rentBtn.setDisable(true);
                rentBtn.setText("Wybierz książkę");
                rentBtn.setStyle("");
            } else {
                rentBtn.setDisable(false);
                if ("AVAILABLE".equals(newSelection.getStatus())) {
                    rentBtn.setDisable(false);
                    rentBtn.setText("Wypożycz książkę");
                    rentBtn.setStyle("-fx-background-color: #2d5a27; -fx-text-fill: white; -fx-cursor: hand;");
                } else {
                    rentBtn.setDisable(true);
                    rentBtn.setText("Książka niedostępna");
                    rentBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: #bdc3c7;");
                }
            }
        });
        rentBtn.setDisable(true);
    }

    private void setupSearch() {
        FilteredList<BookDTO> filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<BookDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(bookTable.comparatorProperty());
        searchField.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(book -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String f = newVal.toLowerCase();
                return book.getTitle().toLowerCase().contains(f)
                        || book.getAuthor().toLowerCase().contains(f)
                        || book.getCategory().toLowerCase().contains(f);
            });
        });
        bookTable.setItems(sortedData);
    }

    @FXML
    private void handleBookAction() {
        BookDTO selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if ("AVAILABLE".equals(selected.getStatus())) {
            handleRent(selected);
        }
        bookTable.refresh();
        bookTable.getSelectionModel().clearSelection();
    }

    private void handleRent(BookDTO book) {
        System.out.println("Wypożyczanie: " + book.getTitle());
        book.setStatus("RENTED");
        bookTable.refresh();
    }

    private void loadMockData() {
        masterData.add(new BookDTO(1L, "Wiedźmin", "Andrzej Sapkowski", "9788375", "Fantasy", "AVAILABLE", "Opis", 1990));
        masterData.add(new BookDTO(2L, "Rok 1984", "George Orwell", "9780451", "Dystopia", "RENTED", "Opis", 1949));
        masterData.add(new BookDTO(3L, "Hobbit", "J.R.R. Tolkien", "9788324", "Fantasy", "AVAILABLE", "Opis", 1937));
    }
}