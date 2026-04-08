package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.stream.Stream;

public class AdminDeleteUsersController {
    @FXML private TextField searchField;
    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, Long> colId;
    @FXML private TableColumn<UserDTO, String> colUsername, colFullName, colEmail, colRole;
    @FXML private TableColumn<UserDTO, Void> colActions;

    private final ObservableList<UserDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFiltering();
        loadInitialData();
        usersTable.setPlaceholder(new Label("Nie znaleziono użytkowników."));
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName())); // Zakładam, że UserDTO ma getFullName()
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRole() != null ? d.getValue().getRole().name() : "BRAK"));

        setupActions();
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Usuń");
            private final HBox container = new HBox(deleteBtn);
            {
                deleteBtn.getStyleClass().add("button-outline-danger-table");
                deleteBtn.setPrefHeight(25);
                container.setAlignment(Pos.CENTER);
                deleteBtn.setOnAction(e -> handleDeleteRequest(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupFiltering() {
        FilteredList<UserDTO> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, newVal) -> {
            String f = newVal.toLowerCase();
            filteredData.setPredicate(u -> f.isBlank() ||
                    Stream.of(u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName(), String.valueOf(u.getId()))
                            .anyMatch(s -> s != null && s.toLowerCase().contains(f)));
        });
        usersTable.setItems(filteredData);
    }

    private void handleDeleteRequest(UserDTO user) {
        if (!canDeleteUser(user)) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie można usunąć użytkownika.", "Upewnij się, że nie ma on aktywnych wypożyczeń.");
            return;
        }
        showAlert(Alert.AlertType.CONFIRMATION, "Potwierdzenie", "Usunąć użytkownika " + user.getUsername() + "?", "Ta operacja jest nieodwracalna.")
                .filter(res -> res == ButtonType.OK)
                .ifPresent(res -> masterData.remove(user));
    }

    private boolean canDeleteUser(UserDTO user) { return true; }

    private java.util.Optional<ButtonType> showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        pane.getStyleClass().add("root-container");
        if (type == Alert.AlertType.CONFIRMATION) {
            styleButton(pane, ButtonType.OK, "Tak, usuń", "button-primary");
            styleButton(pane, ButtonType.CANCEL, "Anuluj", "button-outline-danger");
        } else {
            styleButton(pane, ButtonType.OK, "Rozumiem", "button-primary");
        }
        return alert.showAndWait();
    }

    private void styleButton(DialogPane pane, ButtonType type, String text, String styleClass) {
        Button btn = (Button) pane.lookupButton(type);
        if (btn != null) {
            btn.setText(text);
            btn.getStyleClass().add(styleClass);
        }
    }

    private void loadInitialData() {
        masterData.addAll(
                createUser(1L, "admin_super", "Super", "Admin", "admin@library.com", UserRole.Admin),
                createUser(2L, "jan_nowak", "Jan", "Nowak", "j.nowak@gmail.com", UserRole.Czytelnik),
                createUser(3L, "bibliotekarz1", "Marta", "Zielińska", "staff@library.com", UserRole.Bibliotekarz)
        );
    }

    private UserDTO createUser(Long id, String u, String f, String l, String e, UserRole r) {
        return UserDTO.builder().id(id).username(u).firstName(f).lastName(l).email(e).role(r).build();
    }
}