package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.BookDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import java.util.stream.Stream;

public class CatalogController {
    @FXML private TextField searchField;
    @FXML private TableView<BookDTO> bookTable;
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colStatus, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, Integer> colYear;

    private final ObservableList<BookDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadMockData();
        bookTable.setPlaceholder(new Label("Brak dostępnych książek w katalogu."));
    }

    private void setupColumns() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colAuthor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));
        colIsbn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIsbn()));
        colCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colYear.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getReleaseYear()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colDescription.setCellFactory(tc -> new TableCell<>() {
            private final Text t = new Text();
            { t.wrappingWidthProperty().bind(tc.widthProperty().subtract(20)); t.getStyleClass().add("text"); }
            @Override protected void updateItem(String i, boolean e) {
                super.updateItem(i, e);
                if (e || i == null) setGraphic(null);
                else { t.setText(i); t.fillProperty().bind(textFillProperty()); setGraphic(t); }
            }
        });
    }

    private void setupSearch() {
        FilteredList<BookDTO> filtered = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> {
            String f = val.toLowerCase().trim();
            filtered.setPredicate(b -> f.isEmpty() || Stream.of(b.getTitle(), b.getAuthor(), b.getCategory())
                    .anyMatch(s -> s != null && s.toLowerCase().contains(f)));
        });
        SortedList<BookDTO> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(bookTable.comparatorProperty());
        bookTable.setItems(sorted);
    }

    private void loadMockData() {
        masterData.addAll(
                new BookDTO(1L, "Wiedźmin", "Andrzej Sapkowski", "9788375", "Fantasy", "AVAILABLE", "Opis", 1990),
                new BookDTO(2L, "Rok 1984", "George Orwell", "9780451", "Dystopia", "RENTED", "DłuższyDłuższyDłuższyDłuższyDłuższyDłuższyDłuższyDłuższyDłuższyDłuższyDłuższyDłuższy opis klasyki literatury.", 1949),
                new BookDTO(3L, "Hobbit", "J.R.R. Tolkien", "9788324", "Fantasy", "AVAILABLE", "Opis", 1937)
        );
    }
}