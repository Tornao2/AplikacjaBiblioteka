package com.project.crud.frontend.controllers;

import com.project.crud.frontend.ApiClient;
import com.project.crud.frontend.model.BookDTO;
import com.project.crud.frontend.model.BookStatus;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.stream.Stream;

public class CatalogController {
    @FXML private TextField searchField;
    @FXML private TableView<BookDTO> bookTable;
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, Integer> colYear;
    @FXML private TableColumn<BookDTO, BookStatus> colStatus;

    private final ObservableList<BookDTO> masterData = FXCollections.observableArrayList();
    private ApiClient apiClient;

    @FXML
    public void initialize() {
        this.apiClient = new ApiClient(searchField);
        setupColumns();
        setupSearch();
        refreshCatalog();
        bookTable.setPlaceholder(new Label("Brak dostępnych książek w katalogu."));
    }

    private void setupColumns() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colAuthor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));
        colIsbn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIsbn()));
        colCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colYear.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getReleaseYear()));
        colStatus.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));
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

    public void refreshCatalog() {
        MainController.setLoading(true);
        apiClient.send("/books", "GET", null, BookDTO[].class)
                .thenAccept(arr -> Platform.runLater(() -> {
                    MainController.setLoading(false);
                    if (arr != null) {
                        masterData.setAll(Arrays.asList(arr));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> MainController.setLoading(false));
                    return null;
                });
    }
}