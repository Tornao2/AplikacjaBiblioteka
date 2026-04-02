package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.BookDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class InventoryController {
    @FXML private TextField titleField, authorField, isbnField, yearField, descriptionArea;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TableView<BookDTO> inventoryTable;
    @FXML private TableColumn<BookDTO, Long> colId;
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colStatus, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, Integer> colYear;
    @FXML private TableColumn<BookDTO, Void> colActions;
    @FXML private Button addBtn;

    static final ObservableList<BookDTO> masterInventory = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        setupValidation();
        categoryCombo.setItems(FXCollections.observableArrayList("Fantasy", "Kryminał", "Dystopia", "Nauka", "Biografia", "Inne"));
        inventoryTable.setItems(masterInventory);
        inventoryTable.setPlaceholder(new Label("Brak książek w systemie."));
        addBtn.disableProperty().bind(titleField.textProperty().isEmpty()
                .or(authorField.textProperty().isEmpty())
                .or(yearField.textProperty().isEmpty())
                .or(categoryCombo.valueProperty().isNull())
                .or(isbnField.textProperty().isEmpty())
                .or(descriptionArea.textProperty().isEmpty()));
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
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
        setupActions();
    }

    private void setupActions() {
        colActions.setCellFactory(p -> new TableCell<>() {
            private final Button edit = new Button("Edytuj"), del = new Button("Usuń");
            private final HBox container = new HBox(10, edit, del);
            {
                edit.getStyleClass().add("button-primary-table");
                del.getStyleClass().add("button-outline-danger-table");
                container.setAlignment(Pos.CENTER);
                edit.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void i, boolean e) {
                super.updateItem(i, e);
                setGraphic(e ? null : container);
            }
        });
    }

    private void setupValidation() {
        yearField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) yearField.setText(old);
        });
    }

    @FXML
    private void handleAddBook() {
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            if (year < 1000 || year > 2026) { showAlert("Podaj realny rok (1000-2026)."); return; }
            masterInventory.add(BookDTO.builder()
                    .id((long) (masterInventory.size() + 1)).title(titleField.getText().trim())
                    .author(authorField.getText().trim()).isbn(isbnField.getText().trim())
                    .category(categoryCombo.getValue()).description(descriptionArea.getText().trim())
                    .releaseYear(year).status("AVAILABLE").build());
            clearFields();
        } catch (Exception e) { showAlert("Nieprawidłowe dane!"); }
    }

    private void showEditDialog(BookDTO book) {
        Dialog<BookDTO> dialog = new Dialog<>();
        dialog.setTitle("Edycja książki");
        ButtonType saveType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleControl(dialog, "Zapisz", "Anuluj");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        TextField eTitle = new TextField(book.getTitle()), eAuthor = new TextField(book.getAuthor()),
                eIsbn = new TextField(book.getIsbn()), eYear = new TextField(String.valueOf(book.getReleaseYear())),
                eDesc = new TextField(book.getDescription());
        ComboBox<String> eCat = new ComboBox<>(categoryCombo.getItems()); eCat.setValue(book.getCategory());
        String[] labels = {"Tytuł:", "Autor:", "ISBN:", "Rok:", "Kategoria:", "Opis:"};
        Control[] fields = {eTitle, eAuthor, eIsbn, eYear, eCat, eDesc};
        for (int i = 0; i < labels.length; i++) { grid.add(new Label(labels[i]), 0, i); grid.add(fields[i], 1, i); }
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn == saveType ? updateBook(book, eTitle, eAuthor, eIsbn, eCat, eYear, eDesc) : null);
        dialog.showAndWait().ifPresent(r -> inventoryTable.refresh());
    }

    private BookDTO updateBook(BookDTO b, TextField t, TextField a, TextField i, ComboBox<String> c, TextField y, TextField d) {
        b.setTitle(t.getText().trim()); b.setAuthor(a.getText().trim()); b.setIsbn(i.getText().trim());
        b.setCategory(c.getValue()); b.setReleaseYear(Integer.parseInt(y.getText().trim())); b.setDescription(d.getText().trim());
        return b;
    }

    private void handleDelete(BookDTO book) {
        if ("RENTED".equals(book.getStatus())) { showAlert("Książka jest wypożyczona!"); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć książkę: " + book.getTitle() + "?");
        styleControl(alert, "Tak, usuń", "Anuluj");
        alert.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) masterInventory.remove(book); });
    }

    private void styleControl(Dialog<?> d, String okT, String canT) {
        DialogPane dp = d.getDialogPane();
        dp.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dp.getStyleClass().add("root-container");
        d.setHeaderText(null);
        Button ok = (Button) dp.lookupButton(dp.getButtonTypes().get(0));
        if (ok != null) { ok.getStyleClass().add("button-primary"); ok.setText(okT); }
        Button can = (Button) dp.lookupButton(ButtonType.CANCEL);
        if (can != null) { can.getStyleClass().add("button-outline-danger"); can.setText(canT); }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content);
        styleControl(alert, "Rozumiem", null);
        alert.setTitle("Błąd");
        alert.showAndWait();
    }

    private void clearFields() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        yearField.clear();
        descriptionArea.clear();
        categoryCombo.getSelectionModel().select(-1);
        categoryCombo.setValue(null);
        categoryCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(categoryCombo.getPromptText());
                } else {
                    setText(item);
                }
            }
        });
    }
}