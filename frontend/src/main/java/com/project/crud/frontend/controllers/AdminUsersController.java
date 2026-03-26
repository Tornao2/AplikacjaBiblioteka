package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.StaffDTO;
import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
        setupFiltering();
        loadMockData();
    }

    private void setupColumns() {
        colId.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getUser().getId()));
        colFullName.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getUser().getFullName()));
        colEmail.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getUser().getEmail()));
        colRole.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getUser().getRole()));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        setupActions();
    }

    private void setupFiltering() {
        FilteredList<StaffDTO> filtered = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((o, old, v) -> filtered.setPredicate(s -> {
            if (v == null || v.isBlank()) return true;
            String f = v.toLowerCase();
            return s.getUser().getFullName().toLowerCase().contains(f) || s.getUser().getEmail().toLowerCase().contains(f);
        }));
        SortedList<StaffDTO> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(staffTable.comparatorProperty());
        staffTable.setItems(sorted);
    }

    private void setupActions() {
        colActions.setCellFactory(p -> new TableCell<>() {
            private final Button edit = new Button("Edytuj"), del = new Button("Zwolnij");
            private final HBox container = new HBox(10, edit, del);
            {
                edit.getStyleClass().add("button-primary-table");
                del.getStyleClass().add("button-outline-danger-table");
                container.setAlignment(Pos.CENTER);
                edit.setOnAction(e -> showStaffDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> handleDemote(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void i, boolean e) {
                super.updateItem(i, e);
                setGraphic(e ? null : container);
            }
        });
    }

    @FXML private void handleAddNewStaff() { showStaffDialog(null); }

    private void showStaffDialog(StaffDTO staff) {
        boolean isEdit = staff != null;
        Dialog<StaffDTO> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edycja" : "Nowy Pracownik");
        ButtonType saveType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleControl(dialog, "Zapisz", "Anuluj");
        TextField user = new TextField(isEdit ? staff.getUser().getUsername() : "");
        user.setDisable(isEdit);
        TextField fName = new TextField(isEdit ? staff.getUser().getFirstName() : "");
        TextField lName = new TextField(isEdit ? staff.getUser().getLastName() : "");
        TextField email = new TextField(isEdit ? staff.getUser().getEmail() : "");
        TextField phone = new TextField(isEdit ? staff.getPhoneNumber() : "");
        TextField salary = new TextField(isEdit ? String.valueOf(staff.getSalary()) : "");
        ComboBox<UserRole> role = new ComboBox<>(FXCollections.observableArrayList(UserRole.LIBRARIAN, UserRole.ADMIN));
        role.setValue(isEdit ? staff.getUser().getRole() : UserRole.LIBRARIAN);
        DatePicker hire = new DatePicker(isEdit ? staff.getHireDate() : LocalDate.now());
        salary.textProperty().addListener((o, old, v) -> { if (!v.matches("\\d*(\\.\\d*)?")) salary.setText(old); });
        phone.textProperty().addListener((o, old, v) -> { if (!v.matches("[\\d-]*")) phone.setText(old); });
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        user.getText().isBlank() || fName.getText().isBlank() || lName.getText().isBlank() ||
                                !email.getText().contains("@") || salary.getText().isBlank(),
                user.textProperty(), fName.textProperty(), lName.textProperty(), email.textProperty(), salary.textProperty()
        ));
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        String[] labels = {"Login:", "Imię:", "Nazwisko:", "Email:", "Telefon:", "Pensja:", "Rola:", "Data:"};
        Control[] fields = {user, fName, lName, email, phone, salary, role, hire};
        for (int i = 0; i < labels.length; i++) { grid.add(new Label(labels[i]), 0, i); grid.add(fields[i], 1, i); }
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn == saveType ? updateStaff(staff, user, fName, lName, email, role, phone, salary, hire) : null);
        dialog.showAndWait().ifPresent(s -> { if (!isEdit) masterData.add(s); staffTable.refresh(); });
    }

    private StaffDTO updateStaff(StaffDTO s, TextField u, TextField fn, TextField ln, TextField em, ComboBox<UserRole> r, TextField ph, TextField sal, DatePicker h) {
        UserDTO user = (s == null) ? UserDTO.builder().build() : s.getUser();
        user.setUsername(u.getText().trim()); user.setFirstName(fn.getText().trim());
        user.setLastName(ln.getText().trim()); user.setEmail(em.getText().trim().toLowerCase());
        user.setRole(r.getValue());
        StaffDTO staff = (s == null) ? StaffDTO.builder().user(user).build() : s;
        staff.setPhoneNumber(ph.getText().trim()); staff.setSalary(Double.parseDouble(sal.getText()));
        staff.setHireDate(h.getValue());
        return staff;
    }

    private void handleDemote(StaffDTO s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Czy na pewno zwolnić " + s.getUser().getUsername() + "?");
        styleControl(alert, "Tak, zwolnij", "Nie, zostaw");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) { s.getUser().setRole(UserRole.USER); masterData.remove(s); }
        });
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

    private void loadMockData() {
        masterData.add(StaffDTO.builder().id(1L).user(new UserDTO(1L, "admin", "Jan", "Kowalski", "a@b.pl", UserRole.ADMIN))
                .phoneNumber("123-123-123").salary(5000.0).hireDate(LocalDate.now()).build());
    }
}