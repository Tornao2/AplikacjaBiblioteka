package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.BookDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class InventoryController {
    @FXML private TextField titleField, authorField, isbnField, yearField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TableView<BookDTO> inventoryTable;
    @FXML private TableColumn<BookDTO, Long> colId;
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colStatus, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, Integer> colYear;
    @FXML private Button deleteBtn;
    @FXML private TableColumn<BookDTO, Void> colActions;

    static final ObservableList<BookDTO> masterInventory = FXCollections.observableArrayList();

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
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edytuj");
            {
                editBtn.getStyleClass().add("button-primary-table");
                editBtn.setOnAction(event -> {
                    BookDTO book = getTableView().getItems().get(getIndex());
                    showEditDialog(book);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editBtn);
                }
            }
        });
        inventoryTable.setItems(masterInventory);
        deleteBtn.disableProperty().bind(inventoryTable.getSelectionModel().selectedItemProperty().isNull());
        inventoryTable.setPlaceholder(new Label("Brak książek w systemie."));
    }

    private void showEditDialog(BookDTO book) {
        Dialog<BookDTO> dialog = new Dialog<>();
        dialog.setTitle("Edycja książki");
        dialog.setHeaderText("Edytujesz: " + book.getTitle());
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dialogPane.getStyleClass().add("root-container");
        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 50, 10, 10));
        TextField editTitle = new TextField(book.getTitle());
        TextField editAuthor = new TextField(book.getAuthor());
        TextField editIsbn = new TextField(book.getIsbn());
        TextField editYear = new TextField(String.valueOf(book.getReleaseYear()));
        ComboBox<String> editCategory = new ComboBox<>(categoryCombo.getItems());
        editCategory.setValue(book.getCategory());
        TextArea editDesc = new TextArea(book.getDescription());
        editDesc.setWrapText(true);
        editDesc.setPrefRowCount(3);
        Button saveBtn = (Button) dialogPane.lookupButton(saveButtonType);
        saveBtn.getStyleClass().add("button-primary");
        Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelBtn.getStyleClass().add("button-outline-danger");
        grid.add(new Label("Tytuł:"), 0, 0);   grid.add(editTitle, 1, 0);
        grid.add(new Label("Autor:"), 0, 1);   grid.add(editAuthor, 1, 1);
        grid.add(new Label("ISBN:"), 0, 2);    grid.add(editIsbn, 1, 2);
        grid.add(new Label("Rok:"), 0, 3);     grid.add(editYear, 1, 3);
        grid.add(new Label("Kategoria:"), 0, 4); grid.add(editCategory, 1, 4);
        grid.add(new Label("Opis:"), 0, 5);    grid.add(editDesc, 1, 5);
        dialogPane.setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                book.setTitle(editTitle.getText());
                book.setAuthor(editAuthor.getText());
                book.setIsbn(editIsbn.getText());
                book.setCategory(editCategory.getValue());
                book.setReleaseYear(Integer.parseInt(editYear.getText()));
                book.setDescription(editDesc.getText());
                return book;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(result -> inventoryTable.refresh());
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
        } catch (NumberFormatException e) {
            showAlert("Błąd formatu", "Rok wydania musi być liczbą!");
        } catch (IllegalArgumentException e) {
            showAlert("Błąd walidacji", e.getMessage());
        } catch (Exception e) {
            showAlert("Błąd", "Wystąpił nieoczekiwany błąd: " + e.getMessage());
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