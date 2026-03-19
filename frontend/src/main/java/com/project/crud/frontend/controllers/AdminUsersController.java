package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.StaffDTO;
import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.time.LocalDate;

public class AdminUsersController {
    @FXML private TableView<StaffDTO> staffTable;
    @FXML private TableColumn<StaffDTO, Long> colId;
    @FXML private TableColumn<StaffDTO, String> colFullName, colEmail, colPhone;
    @FXML private TableColumn<StaffDTO, UserRole> colRole;
    @FXML private TableColumn<StaffDTO, Double> colSalary;
    @FXML private TableColumn<StaffDTO, LocalDate> colHireDate;
    @FXML private TableColumn<StaffDTO, Void> colActions;
    @FXML private TextField searchField;

    private final ObservableList<StaffDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadMockData();
        setupActions();
        setupFiltering();
    }

    private void setupColumns() {
        colId.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getUser().getId()));
        colFullName.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getUser().getFullName()));
        colEmail.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getUser().getEmail()));
        colRole.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getUser().getRole()));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
    }

    private void setupFiltering() {
        FilteredList<StaffDTO> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(staff -> {
            if (newValue == null || newValue.isEmpty()) return true;
            String filter = newValue.toLowerCase();
            UserDTO u = staff.getUser();

            return u.getFirstName().toLowerCase().contains(filter) ||
                    u.getLastName().toLowerCase().contains(filter) ||
                    u.getEmail().toLowerCase().contains(filter) ||
                    u.getUsername().toLowerCase().contains(filter);
        }));
        SortedList<StaffDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(staffTable.comparatorProperty());
        staffTable.setItems(sortedData);
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button demoteBtn = new Button("Zwolnij");
            private final HBox container = new HBox(10, demoteBtn);
            {
                demoteBtn.getStyleClass().add("button-outline-danger");
                demoteBtn.setPrefHeight(25);
                demoteBtn.setMaxHeight(Region.USE_PREF_SIZE);
                demoteBtn.setStyle("-fx-padding: 2 10 2 10;");
                demoteBtn.setOnAction(event -> {
                    StaffDTO staff = getTableView().getItems().get(getIndex());
                    handleDemote(staff);
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

    private void handleDemote(StaffDTO staff) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Zwolnienie pracownika");
        alert.setHeaderText("Czy na pewno chcesz usunąć uprawnienia?");
        alert.setContentText("Użytkownik " + staff.getUser().getUsername() + " stanie się zwykłym czytelnikiem.");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        dialogPane.getStyleClass().add("root-container");
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Zdejmij funkcję");
        okButton.getStyleClass().add("button-outline-danger");
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Anuluj");
        cancelButton.getStyleClass().add("button-primary");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                staff.getUser().setRole(UserRole.USER);
                masterData.remove(staff);
                System.out.println("Rola użytkownika zmieniona na USER");
            }
        });
    }

    @FXML
    private void handleAddNewStaff() {
        Dialog<StaffDTO> dialog = new Dialog<>();
        dialog.setTitle("Nowy Pracownik");
        dialog.setHeaderText("Dodaj dane nowego pracownika");
        DialogPane dialogPane = dialog.getDialogPane();
        String cssPath = getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm();
        dialogPane.getStylesheets().add(cssPath);
        dialogPane.getStyleClass().add("root-container");
        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        Button saveBtn = (Button) dialogPane.lookupButton(saveButtonType);
        saveBtn.getStyleClass().add("button-primary");
        Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) {
            cancelBtn.setText("Anuluj");
            cancelBtn.getStyleClass().add("button-outline-danger");
        }
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 20, 30));
        grid.getStyleClass().add("form-container");
        TextField username = new TextField(); username.getStyleClass().add("text-field");
        TextField fName = new TextField();    fName.getStyleClass().add("text-field");
        TextField lName = new TextField();    lName.getStyleClass().add("text-field");
        TextField email = new TextField();    email.getStyleClass().add("text-field");
        TextField phone = new TextField();    phone.getStyleClass().add("text-field");
        TextField salary = new TextField();   salary.getStyleClass().add("text-field");
        ComboBox<UserRole> roleCombo = new ComboBox<>(FXCollections.observableArrayList(UserRole.LIBRARIAN, UserRole.ADMIN));
        roleCombo.setValue(UserRole.LIBRARIAN);
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.getStyleClass().add("text-field");
        DatePicker hireDate = new DatePicker(LocalDate.now());
        hireDate.setMaxWidth(Double.MAX_VALUE);
        hireDate.getStyleClass().add("text-field");
        grid.add(new Label("Login:"), 0, 0);    grid.add(username, 1, 0);
        grid.add(new Label("Imię:"), 0, 1);     grid.add(fName, 1, 1);
        grid.add(new Label("Nazwisko:"), 0, 2);  grid.add(lName, 1, 2);
        grid.add(new Label("Email:"), 0, 3);    grid.add(email, 1, 3);
        grid.add(new Label("Rola:"), 0, 4);     grid.add(roleCombo, 1, 4);
        grid.add(new Label("Telefon:"), 0, 5);  grid.add(phone, 1, 5);
        grid.add(new Label("Pensja:"), 0, 6);   grid.add(salary, 1, 6);
        grid.add(new Label("Data:"), 0, 7);     grid.add(hireDate, 1, 7);
        dialogPane.setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    UserDTO u = UserDTO.builder()
                            .username(username.getText())
                            .firstName(fName.getText())
                            .lastName(lName.getText())
                            .email(email.getText())
                            .role(roleCombo.getValue())
                            .build();

                    return StaffDTO.builder()
                            .user(u)
                            .phoneNumber(phone.getText())
                            .salary(Double.parseDouble(salary.getText().replace(",", ".")))
                            .hireDate(hireDate.getValue())
                            .build();
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(staff -> {
            masterData.add(staff);
            staffTable.refresh();
        });
    }

    private void loadMockData() {
        UserDTO u1 = new UserDTO(1L, "marta_b", "Marta", "Bibliotekarz", "marta@biblo.pl", UserRole.LIBRARIAN);
        StaffDTO s1 = StaffDTO.builder()
                .id(101L)
                .user(u1)
                .phoneNumber("123-456-789")
                .salary(4500.0)
                .hireDate(LocalDate.now().minusYears(1))
                .build();
        masterData.add(s1);
    }
}