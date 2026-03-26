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

public class AdminDeleteUsersController {
    @FXML private TextField searchField;
    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, Long> colId;
    @FXML private TableColumn<UserDTO, String> colUsername, colEmail, colRole;
    @FXML private TableColumn<UserDTO, Void> colActions;

    private final ObservableList<UserDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFiltering();
        loadInitialData();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
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
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filteredData.setPredicate(user -> {
            if (newVal == null || newVal.isBlank()) return true;
            String filter = newVal.toLowerCase();
            return user.getUsername().toLowerCase().contains(filter) ||
                    user.getEmail().toLowerCase().contains(filter) ||
                    String.valueOf(user.getId()).contains(filter);
        }));
        usersTable.setItems(filteredData);
    }

    private void handleDeleteRequest(UserDTO user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie");
        alert.setHeaderText("Usunąć użytkownika " + user.getUsername() + "?");
        alert.setContentText("Ta operacja jest nieodwracalna.");
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        pane.getStyleClass().add("root-container");
        styleButton(pane, ButtonType.OK, "Tak, usuń", "button-primary");
        styleButton(pane, ButtonType.CANCEL, "Anuluj", "button-outline-danger");
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) masterData.remove(user);
        });
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
                UserDTO.builder().id(1L).username("admin_super").email("admin@library.com").role(UserRole.ADMIN).build(),
                UserDTO.builder().id(2L).username("jan_nowak").email("j.nowak@gmail.com").role(UserRole.USER).build(),
                UserDTO.builder().id(3L).username("bibliotekarz1").email("staff@library.com").role(UserRole.LIBRARIAN).build()
        );
    }
}