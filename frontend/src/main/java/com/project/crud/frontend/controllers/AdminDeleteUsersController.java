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
    @FXML private TableColumn<UserDTO, String> colUsername;
    @FXML private TableColumn<UserDTO, String> colEmail;
    @FXML private TableColumn<UserDTO, String> colRole;
    @FXML private TableColumn<UserDTO, Void> colActions;

    private final ObservableList<UserDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFiltering();
        loadInitialData();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colUsername.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        colRole.setCellValueFactory(cellData -> {
            UserRole role = cellData.getValue().getRole();
            return new SimpleStringProperty(role != null ? role.name() : "BRAK");
        });
        colId.setStyle("-fx-alignment: CENTER;");
        colRole.setStyle("-fx-alignment: CENTER;");
        setupActions();
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Usuń");
            private final HBox container = new HBox(deleteBtn);
            {
                deleteBtn.getStyleClass().add("button-outline-danger");
                deleteBtn.setPrefHeight(25);
                deleteBtn.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
                deleteBtn.setStyle("-fx-padding: 2 12 2 12;");
                container.setAlignment(Pos.CENTER);
                deleteBtn.setOnAction(event -> {
                    UserDTO user = getTableView().getItems().get(getIndex());
                    handleDeleteRequest(user);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void loadInitialData() {
        masterData.add(UserDTO.builder().id(1L).username("admin_super").email("admin@library.com").role(UserRole.ADMIN).build());
        masterData.add(UserDTO.builder().id(2L).username("jan_nowak").email("j.nowak@gmail.com").role(UserRole.USER).build());
        masterData.add(UserDTO.builder().id(3L).username("bibliotekarz1").email("staff@library.com").role(UserRole.LIBRARIAN).build());
    }

    private void handleDeleteRequest(UserDTO user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia");
        alert.setHeaderText("Usunąć użytkownika " + user.getUsername() + "?");
        alert.setContentText("Ta operacja jest nieodwracalna.");
        DialogPane dialogPane = alert.getDialogPane();
        String cssPath = getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm();
        dialogPane.getStylesheets().add(cssPath);
        dialogPane.getStyleClass().add("root-container");
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Tak, usuń");
        okButton.getStyleClass().add("button-outline-danger");
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Anuluj");
        cancelButton.getStyleClass().add("button-primary");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                masterData.remove(user);
                System.out.println("Usunięto użytkownika ID: " + user.getId());
            }
        });
    }

    private void setupFiltering() {
        FilteredList<UserDTO> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(user -> {
            if (newValue == null || newValue.isEmpty()) return true;
            String lowerCaseFilter = newValue.toLowerCase();
            return user.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                    user.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                    String.valueOf(user.getId()).contains(lowerCaseFilter);
        }));
        usersTable.setItems(filteredData);
    }
}