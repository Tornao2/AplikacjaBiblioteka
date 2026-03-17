package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.BookDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class InventoryController {
    @FXML private TextField titleField, authorField, isbnField, yearField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TableView<BookDTO> inventoryTable;
    @FXML private TableColumn<BookDTO, Long> colId;
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colStatus, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, Integer> colYear;
    @FXML private Button deleteBtn;

    private static final ObservableList<BookDTO> masterInventory = FXCollections.observableArrayList();

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
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Fantasy", "Kryminał", "Dystopia", "Nauka", "Biografia", "Inne"
        ));
        inventoryTable.setItems(masterInventory);
        deleteBtn.disableProperty().bind(inventoryTable.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void handleAddBook() {
        try {
            validateInputs();
            BookDTO newBook = BookDTO.builder()
                    .id((long) (masterInventory.size() + 1))
                    .title(titleField.getText())
                    .author(authorField.getText())
                    .isbn(isbnField.getText())
                    .category(categoryCombo.getValue() != null ? categoryCombo.getValue() : "Inne")
                    .description(descriptionArea.getText())
                    .releaseYear(Integer.parseInt(yearField.getText()))
                    .status("AVAILABLE")
                    .build();
            masterInventory.add(newBook);
            clearFields();
        } catch (Exception e) {
            System.err.println("Błąd" + e.getMessage());
        }
    }

    private void validateInputs() {
        if (titleField.getText().isEmpty()) throw new IllegalArgumentException("Tytuł jest wymagany!");
        if (authorField.getText().isEmpty()) throw new IllegalArgumentException("Autor jest wymagany!");
        if (yearField.getText().isEmpty()) throw new IllegalArgumentException("Rok wydania jest wymagany!");
    }

    @FXML
    private void handleDeleteBook() {
        BookDTO selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if ("RENTED".equals(selected.getStatus())) {
                showAlert("Błąd", "Nie można usunąć książki, która jest obecnie wypożyczona!");
            } else {
                masterInventory.remove(selected);
            }
        }
    }

    private void clearFields() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        yearField.clear();
        descriptionArea.clear();
        categoryCombo.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}