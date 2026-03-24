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
    @FXML private TableColumn<BookDTO, Long> colId;
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colStatus, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, Integer> colYear;

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
        colDescription.setCellFactory(tc -> new TableCell<>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
                label.maxWidthProperty().bind(tc.widthProperty().subtract(15));
                label.getStyleClass().add("text");
                setGraphic(label);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    label.setText(null);
                } else {
                    label.setText(item);
                    setGraphic(label);
                    label.textFillProperty().bind(textFillProperty());
                }
            }
        });
        loadMockData();
        setupSearch();
    }

    private void setupSearch() {
        FilteredList<BookDTO> filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<BookDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(bookTable.comparatorProperty());
        searchField.textProperty().addListener((obs, old, newVal) -> filteredData.setPredicate(book -> {
            if (newVal == null || newVal.isEmpty()) return true;
            String f = newVal.toLowerCase();
            return book.getTitle().toLowerCase().contains(f)
                    || book.getAuthor().toLowerCase().contains(f)
                    || book.getCategory().toLowerCase().contains(f);
        }));
        bookTable.setItems(sortedData);
    }

    private void loadMockData() {
        masterData.add(new BookDTO(1L, "Wiedźmin", "Andrzej Sapkowski", "9788375", "Fantasy", "AVAILABLE", "Opis", 1990));
        masterData.add(new BookDTO(2L, "Rok 1984", "George Orwell", "9780451", "Dystopia", "RENTED", "Opis", 1949));
        masterData.add(new BookDTO(3L, "Hobbit", "J.R.R. Tolkien", "9788324", "Fantasy", "AVAILABLE", "Opis", 1937));
    }
}