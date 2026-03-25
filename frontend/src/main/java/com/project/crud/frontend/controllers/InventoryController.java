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
    @FXML private TextField descriptionArea;
    @FXML private TableView<BookDTO> inventoryTable;
    @FXML private TableColumn<BookDTO, Long> colId;
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colStatus, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, Integer> colYear;
    @FXML private Button deleteBtn;
    @FXML private Button addBtn;
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
        addBtn.disableProperty().bind(
                titleField.textProperty().isEmpty()
                        .or(authorField.textProperty().isEmpty())
                        .or(yearField.textProperty().isEmpty())
                        .or(categoryCombo.valueProperty().isNull())
                        .or(isbnField.textProperty().isNull())
                        .or(descriptionArea.textProperty().isNull())
        );
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
        TextField editDesc = new TextField(book.getDescription());
        Button saveBtn = (Button) dialogPane.lookupButton(saveButtonType);
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.disableProperty().bind(
                editTitle.textProperty().isEmpty()
                        .or(editAuthor.textProperty().isEmpty())
                        .or(editYear.textProperty().isEmpty())
                        .or(editCategory.valueProperty().isNull())
                        .or(editIsbn.textProperty().isNull())
                        .or(editDesc.textProperty().isNull())
        );
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
            showAlert("Rok wydania musi być liczbą!");
        } catch (IllegalArgumentException e) {
            showAlert("Zły typ danych");
        } catch (Exception e) {
            showAlert("Wystąpił nieoczekiwany błąd: " + e.getMessage());
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
                showAlert("Nie można usunąć książki, która jest obecnie wypożyczona!");
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

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dialogPane.getStyleClass().add("root-container");
        alert.setHeaderText(null);
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.getStyleClass().add("button-primary");
            okButton.applyCss();
            okButton.setText("Rozumiem");
        }
        alert.showAndWait();
    }
}