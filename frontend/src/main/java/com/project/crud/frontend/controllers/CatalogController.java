package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.BookDTO;
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
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colStatus, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, Integer> colYear;

    private final ObservableList<BookDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadMockData();
        setupSearch();
    }

    private void setupColumns() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setCellFactory(tc -> new TableCell<>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();
            {
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(20));
                text.getStyleClass().add("text");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    text.fillProperty().bind(textFillProperty());
                    setGraphic(text);
                }
            }
        });
        bookTable.setPlaceholder(new Label("Brak dostępnych książek w katalogu."));
    }

    private void setupSearch() {
        FilteredList<BookDTO> filtered = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> filtered.setPredicate(book -> {
            if (val == null || val.isBlank()) return true;
            String f = val.toLowerCase();
            return book.getTitle().toLowerCase().contains(f) ||
                    book.getAuthor().toLowerCase().contains(f) ||
                    book.getCategory().toLowerCase().contains(f);
        }));
        SortedList<BookDTO> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(bookTable.comparatorProperty());
        bookTable.setItems(sorted);
    }

    private void loadMockData() {
        masterData.addAll(
                new BookDTO(1L, "Wiedźmin", "Andrzej Sapkowski", "9788375", "Fantasy", "AVAILABLE", "Opis", 1990),
                new BookDTO(2L, "Rok 1984", "George Orwell", "9780451", "Dystopia", "RENTED", "OpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpisOpis", 1949),
                new BookDTO(3L, "Hobbit", "J.R.R. Tolkien", "9788324", "Fantasy", "AVAILABLE", "Opis", 1937)
        );
    }
}