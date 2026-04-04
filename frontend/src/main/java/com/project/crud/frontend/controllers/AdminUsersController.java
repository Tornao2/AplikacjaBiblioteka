package com.project.crud.frontend.controllers;

import com.project.crud.frontend.model.StaffDTO;
import com.project.crud.frontend.model.UserDTO;
import com.project.crud.frontend.model.UserRole;
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
import java.time.LocalDate;
import java.util.stream.Stream;

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
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getUser().getId()));
        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUser().getFullName()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUser().getEmail()));
        colRole.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getUser().getRole()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhoneNumber()));
        colSalary.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSalary()));
        colHireDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getHireDate()));
        setupActions();
    }

    private void setupFiltering() {
        FilteredList<StaffDTO> filtered = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((o, old, v) -> {
            String f = v.toLowerCase().trim();
            filtered.setPredicate(s -> f.isEmpty() || Stream.of(s.getUser().getFullName(), s.getUser().getEmail())
                    .anyMatch(field -> field.toLowerCase().contains(f)));
        });
        SortedList<StaffDTO> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(staffTable.comparatorProperty());
        staffTable.setItems(sorted);
    }

    private void setupActions() {
        colActions.setCellFactory(p -> new TableCell<>() {
            private final Button edit = new Button("Edytuj"), del = new Button("Zwolnij");
            private final HBox container = new HBox(8, edit, del);
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

    private void showStaffDialog(StaffDTO s) {
        boolean isEdit = s != null;
        Dialog<StaffDTO> dialog = new Dialog<>();
        styleControl(dialog, "Zapisz");
        dialog.setTitle(isEdit ? "Edycja" : "Nowy Pracownik");
        TextField user = new TextField(isEdit ? s.getUser().getUsername() : ""),
                fName = new TextField(isEdit ? s.getUser().getFirstName() : ""),
                lName = new TextField(isEdit ? s.getUser().getLastName() : ""),
                email = new TextField(isEdit ? s.getUser().getEmail() : ""),
                phone = new TextField(isEdit ? s.getPhoneNumber() : ""),
                salary = new TextField(isEdit ? String.valueOf(s.getSalary()) : "");
        user.setDisable(isEdit);
        ComboBox<UserRole> role = new ComboBox<>(FXCollections.observableArrayList(UserRole.LIBRARIAN, UserRole.ADMIN));
        role.setValue(isEdit ? s.getUser().getRole() : UserRole.LIBRARIAN);
        DatePicker hire = new DatePicker(isEdit ? s.getHireDate() : LocalDate.now());
        salary.textProperty().addListener((o, old, v) -> { if (!v.matches("\\d*(\\.\\d*)?")) salary.setText(old); });
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        String[] l = {"Login:", "Imię:", "Nazwisko:", "Email:", "Telefon:", "Pensja:", "Rola:", "Data:"};
        Control[] c = {user, fName, lName, email, phone, salary, role, hire};
        for (int i = 0; i < l.length; i++) grid.addRow(i, new Label(l[i]), c[i]);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn.getButtonData().isDefaultButton() ?
                update(s, user, fName, lName, email, role, phone, salary, hire) : null);
        dialog.showAndWait().ifPresent(res -> { if (!isEdit) masterData.add(res); staffTable.refresh(); });
    }

    private StaffDTO update(StaffDTO s, TextField u, TextField fn, TextField ln, TextField em, ComboBox<UserRole> r, TextField ph, TextField sal, DatePicker h) {
        UserDTO user = (s == null) ? UserDTO.builder().build() : s.getUser();
        user.setUsername(u.getText()); user.setFirstName(fn.getText()); user.setLastName(ln.getText());
        user.setEmail(em.getText().toLowerCase()); user.setRole(r.getValue());
        StaffDTO staff = (s == null) ? StaffDTO.builder().user(user).build() : s;
        staff.setPhoneNumber(ph.getText()); staff.setSalary(Double.parseDouble(sal.getText()));
        staff.setHireDate(h.getValue());
        return staff;
    }

    private void handleDemote(StaffDTO s) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Czy na pewno zwolnić " + s.getUser().getUsername() + "?");
        styleControl(a, "Tak, zwolnij");
        a.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            s.getUser().setRole(UserRole.USER); masterData.remove(s);
        });
    }

    private void styleControl(Dialog<?> d, String okT) {
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getButtonTypes().setAll(new ButtonType(okT, ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        p.getStyleClass().add("root-container");
        (p.lookupButton(p.getButtonTypes().get(0))).getStyleClass().add("button-primary");
        (p.lookupButton(ButtonType.CANCEL)).getStyleClass().add("button-outline-danger");
        ((Button) p.lookupButton(ButtonType.CANCEL)).setText("Anuluj");
    }

    @FXML private void handleAddNewStaff() { showStaffDialog(null); }

    private void loadMockData() {
        masterData.add(StaffDTO.builder().id(1L).user(new UserDTO(1L, "admin", "Jan", "Kowalski", "a@b.pl", UserRole.ADMIN))
                .phoneNumber("123-123-123").salary(5000.0).hireDate(LocalDate.now()).build());
    }
}