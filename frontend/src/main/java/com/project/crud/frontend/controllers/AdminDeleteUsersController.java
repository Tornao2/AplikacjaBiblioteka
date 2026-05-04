package com.project.crud.frontend.controllers;

import com.project.crud.frontend.ApiClient;
import com.project.crud.frontend.model.UserDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class AdminDeleteUsersController {
    @FXML private TextField searchField;
    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, Long> colId;
    @FXML private TableColumn<UserDTO, String> colUsername, colFullName, colEmail, colRole;
    @FXML private TableColumn<UserDTO, Void> colActions;

    private final ObservableList<UserDTO> masterData = FXCollections.observableArrayList();
    private ApiClient apiClient;

    @FXML
    public void initialize() {
        this.apiClient = new ApiClient(usersTable);
        setupTableColumns();
        setupFiltering();
        loadUsersFromApi();
        usersTable.setPlaceholder(new Label("Nie znaleziono użytkowników."));
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
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

    private void loadUsersFromApi() {
        apiClient.send("/users", "GET", null, UserDTO[].class)
                .thenAccept(usersArray -> {
                    if (usersArray != null) {
                        Platform.runLater(() -> {
                            masterData.clear();
                            masterData.addAll(List.of(usersArray));
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać danych", ApiClient.getErrorMessage(ex)));
                    return null;
                });
    }

    private void handleDeleteRequest(UserDTO user) {
        showAlert(Alert.AlertType.CONFIRMATION, "Potwierdzenie", "Usunąć użytkownika " + user.getUsername() + "?", "Ta operacja jest nieodwracalna.")
                .filter(res -> res == ButtonType.OK)
                .ifPresent(res -> apiClient.send("/users/" + user.getId(), "DELETE", null, Void.class)
                        .thenAccept(v -> Platform.runLater(() -> {
                            masterData.remove(user);
                            usersTable.refresh();
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd usuwania", "Nie można usunąć użytkownika", ApiClient.getErrorMessage(ex)));
                            return null;
                        }));
    }

    private java.util.Optional<ButtonType> showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/project/crud/frontend/style.css")).toExternalForm());
        pane.getStyleClass().add("root-container");
        pane.setMinWidth(400);
        pane.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        pane.setMinHeight(Region.USE_PREF_SIZE);
        for (javafx.scene.Node node : pane.getChildrenUnmodifiable()) {
            if (node instanceof Label) {
                ((Label) node).setWrapText(true);
                ((Label) node).setMinWidth(Region.USE_PREF_SIZE);
                ((Label) node).setMaxWidth(Double.MAX_VALUE);
            }
        }
        if (type == Alert.AlertType.CONFIRMATION) {
            styleButton(pane, ButtonType.OK, "Tak, usuń", "button-primary");
            styleButton(pane, ButtonType.CANCEL, "Anuluj", "button-outline-danger");
        } else {
            styleButton(pane, ButtonType.OK, "Rozumiem", "button-primary");
        }
        if (pane.getScene() != null && pane.getScene().getWindow() != null) {
            Platform.runLater(() -> pane.getScene().getWindow().sizeToScene());
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
}