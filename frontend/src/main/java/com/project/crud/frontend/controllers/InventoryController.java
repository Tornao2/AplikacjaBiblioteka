package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.BookDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import java.util.stream.Stream;

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
        categoryCombo.setItems(FXCollections.observableArrayList("Fantasy", "Kryminał", "Dystopia", "Nauka", "Biografia", "Inne"));
        inventoryTable.setItems(masterInventory);
        inventoryTable.setPlaceholder(new Label("Brak książek w systemie."));
        yearField.textProperty().addListener((obs, old, val) -> { if (!val.matches("\\d*")) yearField.setText(old); });
        addBtn.disableProperty().bind(titleField.textProperty().isEmpty()
                .or(authorField.textProperty().isEmpty()).or(yearField.textProperty().isEmpty())
                .or(categoryCombo.valueProperty().isNull()).or(isbnField.textProperty().isEmpty())
                .or(descriptionArea.textProperty().isEmpty()));
    }

    private void setupColumns() {
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
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
            @Override protected void updateItem(Void i, boolean e) { super.updateItem(i, e); setGraphic(e ? null : container); }
        });
    }

    @FXML
    private void handleAddBook() {
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            if (year < 1000 || year > 2026) { showAlert("Podaj realny rok (1000-2026)."); return; }
            masterInventory.add(BookDTO.builder().id((long) (masterInventory.size() + 1))
                    .title(titleField.getText().trim()).author(authorField.getText().trim())
                    .isbn(isbnField.getText().trim()).category(categoryCombo.getValue())
                    .description(descriptionArea.getText().trim()).releaseYear(year).status("AVAILABLE").build());
            clearFields();
        } catch (Exception e) { showAlert("Nieprawidłowe dane!"); }
    }

    private void showEditDialog(BookDTO book) {
        Dialog<BookDTO> dialog = new Dialog<>();
        styleControl(dialog, "Zapisz", "Anuluj");
        dialog.setTitle("Edycja książki");
        TextField eTitle = new TextField(book.getTitle()), eAuthor = new TextField(book.getAuthor()),
                eIsbn = new TextField(book.getIsbn()), eYear = new TextField(String.valueOf(book.getReleaseYear())),
                eDesc = new TextField(book.getDescription());
        ComboBox<String> eCat = new ComboBox<>(categoryCombo.getItems()); eCat.setValue(book.getCategory());
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        String[] labels = {"Tytuł:", "Autor:", "ISBN:", "Rok:", "Kategoria:", "Opis:"};
        Control[] fields = {eTitle, eAuthor, eIsbn, eYear, eCat, eDesc};
        for (int i = 0; i < labels.length; i++) grid.addRow(i, new Label(labels[i]), fields[i]);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn.getButtonData().isDefaultButton() ? updateBook(book, eTitle, eAuthor, eIsbn, eCat, eYear, eDesc) : null);
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
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> masterInventory.remove(book));
    }

    private void styleControl(Dialog<?> d, String okT, String canT) {
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        d.setHeaderText(null);
        p.getButtonTypes().setAll(new ButtonType(okT, ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        (p.lookupButton(p.getButtonTypes().get(0))).getStyleClass().add("button-primary");
        Button can = (Button) p.lookupButton(ButtonType.CANCEL);
        can.getStyleClass().add("button-outline-danger");
        if (canT != null) can.setText(canT);
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content);
        styleControl(alert, "Rozumiem", null);
        alert.setTitle("Błąd");
        alert.showAndWait();
    }

    private void clearFields() {
        Stream.of(titleField, authorField, isbnField, yearField, descriptionArea).forEach(TextInputControl::clear);
        categoryCombo.setValue(null);
    }
}