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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.stream.Stream;

public class InventoryController {
    @FXML private TextField titleField, authorField, isbnField, yearField, descriptionArea;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TableView<BookDTO> inventoryTable;
    @FXML private TableColumn<BookDTO, String> colTitle, colAuthor, colIsbn, colCategory, colDescription;
    @FXML private TableColumn<BookDTO, BookStatus> colStatus;
    @FXML private TableColumn<BookDTO, Integer> colYear;
    @FXML private TableColumn<BookDTO, Void> colActions;
    @FXML private Button addBtn;
    @FXML private TextField filterField;

    private final ObservableList<BookDTO> masterInventory = FXCollections.observableArrayList();
    private ApiClient apiClient;

    @FXML
    public void initialize() {
        categoryCombo.setPromptText("Wybierz kategorię");
        this.apiClient = new ApiClient(titleField);
        setupColumns();
        categoryCombo.setItems(FXCollections.observableArrayList("Fantasy", "Klasyka", "Sci-Fi", "Kryminał", "Dystopia", "Nauka", "Biografia", "Inne"));
        inventoryTable.setItems(masterInventory);
        inventoryTable.setPlaceholder(new Label("Brak książek w systemie."));
        yearField.textProperty().addListener((obs, old, val) -> { if (!val.matches("\\d*")) yearField.setText(old); });
        setupValidation();
        setupSearch();
        refreshData();
    }

    private void setupValidation() {
        addBtn.disableProperty().bind(titleField.textProperty().isEmpty()
                .or(authorField.textProperty().isEmpty()).or(yearField.textProperty().isEmpty())
                .or(categoryCombo.valueProperty().isNull()).or(isbnField.textProperty().isEmpty())
                .or(descriptionArea.textProperty().isEmpty()));
    }

    private void setupSearch() {
        FilteredList<BookDTO> filteredData = new FilteredList<>(masterInventory, p -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(book -> {
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = newValue.toLowerCase().trim();
            if (book.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (book.getAuthor().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (book.getIsbn().contains(lowerCaseFilter)) {
                return true;
            } else return book.getCategory().toLowerCase().contains(lowerCaseFilter);
        }));
        SortedList<BookDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(inventoryTable.comparatorProperty());
        inventoryTable.setItems(sortedData);
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
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    BookDTO book = getTableRow().getItem();
                    boolean isNotAvailable = !"Dostepna".equals(String.valueOf(book.getStatus()));
                    del.setDisable(isNotAvailable);
                    edit.setDisable(isNotAvailable);
                    setGraphic(container);
                }
            } });
    }

    private void refreshData() {
        MainController.setLoading(true);
        apiClient.send("/books", "GET", null, BookDTO[].class)
                .thenAccept(arr -> Platform.runLater(() -> {
                    MainController.setLoading(false);
                    if (arr != null) masterInventory.setAll(Arrays.asList(arr));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        MainController.setLoading(false);
                        showAlert("Błąd pobierania: " + ApiClient.getErrorMessage(ex));
                    });
                    return null;
                });
    }

    @FXML
    private void handleAddBook() {
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            BookDTO newBook = BookDTO.builder()
                    .title(titleField.getText().trim())
                    .author(authorField.getText().trim())
                    .isbn(isbnField.getText().trim())
                    .category(categoryCombo.getValue())
                    .description(descriptionArea.getText().trim())
                    .releaseYear(year)
                    .status(BookStatus.Dostepna)
                    .build();
            MainController.setLoading(true);
            apiClient.send("/books", "POST", newBook, BookDTO.class)
                    .thenAccept(res -> Platform.runLater(() -> {
                        refreshData();
                        clearFields();
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            MainController.setLoading(false);
                            showAlert("Błąd zapisu: " + ApiClient.getErrorMessage(ex));
                        });
                        return null;
                    });
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
        grid.addRow(0, new Label("Tytuł:"), eTitle);
        grid.addRow(1, new Label("Autor:"), eAuthor);
        grid.addRow(2, new Label("ISBN:"), eIsbn);
        grid.addRow(3, new Label("Rok:"), eYear);
        grid.addRow(4, new Label("Kategoria:"), eCat);
        grid.addRow(5, new Label("Opis:"), eDesc);
        dialog.getDialogPane().setContent(grid);
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().get(0));
        saveBtn.disableProperty().bind(
                eTitle.textProperty().isEmpty()
                        .or(eAuthor.textProperty().isEmpty())
                        .or(eIsbn.textProperty().isEmpty())
                        .or(eYear.textProperty().isEmpty())
                        .or(eCat.valueProperty().isNull())
                        .or(eDesc.textProperty().isEmpty())
        );
        dialog.setResultConverter(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                book.setTitle(eTitle.getText().trim());
                book.setAuthor(eAuthor.getText().trim());
                book.setIsbn(eIsbn.getText().trim());
                book.setCategory(eCat.getValue());
                book.setReleaseYear(Integer.parseInt(eYear.getText().trim()));
                book.setDescription(eDesc.getText().trim());
                return book;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(updatedBook -> {
            MainController.setLoading(true);
            apiClient.send("/books/" + updatedBook.getId(), "PUT", updatedBook, BookDTO.class)
                    .thenAccept(res -> Platform.runLater(this::refreshData))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            MainController.setLoading(false);
                            showAlert("Błąd edycji: " + ApiClient.getErrorMessage(ex));
                        });
                        return null;
                    });
        });
    }

    private void handleDelete(BookDTO book) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć książkę: " + book.getTitle() + "?");
        styleControl(alert, "Tak, usuń", "Anuluj");
        alert.showAndWait().filter(r -> r.getButtonData().isDefaultButton()).ifPresent(r -> {
            MainController.setLoading(true);
            apiClient.send("/books/" + book.getId(), "DELETE", null, Void.class)
                    .thenRun(() -> Platform.runLater(this::refreshData))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            MainController.setLoading(false);
                            showAlert("Błąd usuwania: " + ApiClient.getErrorMessage(ex));
                        });
                        return null;
                    });
        });
    }

    private void styleControl(Dialog<?> d, String okT, String canT) {
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        d.setHeaderText(null);
        if (canT != null) {
            p.getButtonTypes().setAll(new ButtonType(okT, ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
            Button can = (Button) p.lookupButton(ButtonType.CANCEL);
            can.getStyleClass().add("button-outline-danger");
            can.setText(canT);
        } else {
            p.getButtonTypes().setAll(new ButtonType(okT, ButtonBar.ButtonData.OK_DONE));
        }
        p.lookupButton(p.getButtonTypes().get(0)).getStyleClass().add("button-primary");
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content);
        styleControl(alert, "Rozumiem", null);
        alert.setTitle("Błąd");
        alert.showAndWait();
    }

    private void clearFields() {
        Stream.of(titleField, authorField, isbnField, yearField, descriptionArea).forEach(TextInputControl::clear);
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