package com.project.crud.frontend.controllers;

import com.project.crud.frontend.ApiClient;
import com.project.crud.frontend.auth.UserSession;
import com.project.crud.frontend.model.StaffDTO;
import com.project.crud.frontend.model.UserRole;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class AdminUsersController {
    @FXML private TableView<StaffDTO> staffTable;
    @FXML private TableColumn<StaffDTO, String> colUsername, colFullName, colEmail, colPhone;
    @FXML private TableColumn<StaffDTO, UserRole> colRole;
    @FXML private TableColumn<StaffDTO, Double> colSalary;
    @FXML private TableColumn<StaffDTO, LocalDate> colHireDate;
    @FXML private TableColumn<StaffDTO, Void> colActions;
    @FXML private TextField searchField;

    private final ObservableList<StaffDTO> masterData = FXCollections.observableArrayList();
    private ApiClient apiClient;

    @FXML
    public void initialize() {
        this.apiClient = new ApiClient(searchField);
        setupColumns();
        setupFiltering();
        refreshData();
    }

    private void setupColumns() {
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUser().getUsername()));
        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUser().getFullName()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUser().getEmail()));
        colRole.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getUser().getRole()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhoneNumber()));
        colSalary.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSalary()));
        colHireDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getHireDate()));
        setupActions();
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
        boolean isEdit = (s != null);
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        styleControl(dialog, isEdit ? "Zapisz zmiany" : "Zatrudnij", "Anuluj");
        dialog.setTitle(isEdit ? "Edycja danych pracownika" : "Nowy pracownik");
        TextField user = new TextField(isEdit ? s.getUser().getUsername() : ""),
                fName = new TextField(isEdit ? s.getUser().getFirstName() : ""),
                lName = new TextField(isEdit ? s.getUser().getLastName() : ""),
                email = new TextField(isEdit ? s.getUser().getEmail() : ""),
                phone = new TextField(isEdit ? s.getPhoneNumber() : ""),
                salary = new TextField(isEdit ? String.valueOf(s.getSalary()) : "");
        ComboBox<UserRole> roleCombo = new ComboBox<>(FXCollections.observableArrayList(UserRole.Admin, UserRole.Bibliotekarz));
        roleCombo.setValue(isEdit ? s.getUser().getRole() : UserRole.Bibliotekarz);
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        user.setDisable(isEdit);
        DatePicker hire = new DatePicker(isEdit ? s.getHireDate() : LocalDate.now());
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Login:"), user);
        int rowOffset = 1;
        PasswordField pass = null;
        if (!isEdit) {
            pass = new PasswordField();
            grid.addRow(rowOffset++, new Label("Hasło:"), pass);
        }
        grid.addRow(rowOffset++, new Label("Imię:"), fName);
        grid.addRow(rowOffset++, new Label("Nazwisko:"), lName);
        grid.addRow(rowOffset++, new Label("Rola:"), roleCombo);
        grid.addRow(rowOffset++, new Label("Email:"), email);
        grid.addRow(rowOffset++, new Label("Telefon:"), phone);
        grid.addRow(rowOffset++, new Label("Pensja:"), salary);
        grid.addRow(rowOffset, new Label("Data zatrudnienia:"), hire);
        dialog.getDialogPane().setContent(grid);
        final PasswordField finalPass = pass;
        Button confirmBtn = (Button) dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().get(0));
        if (!isEdit && finalPass.getText() != null) {
            confirmBtn.disableProperty().bind(
                    pass.textProperty().isEmpty()
            );
        }
        dialog.setResultConverter(btn -> btn.getButtonData().isDefaultButton() ?
                buildMap(user, finalPass, fName, lName, email, phone, salary, hire, roleCombo.getValue()) : null);
        dialog.showAndWait().ifPresent(request -> {
            String endpoint = isEdit ? "/staff/" + s.getId() : "/staff";
            String method = isEdit ? "PUT" : "POST";
            MainController.setLoading(true);
            apiClient.send(endpoint, method, request, StaffDTO.class)
                    .thenAccept(res -> Platform.runLater(() -> {
                        String currentUser = UserSession.getInstance().getToken().getUsername();
                        if (isEdit && s.getUser().getUsername().equals(currentUser)) {
                            String roleStr = (String) request.get("role");
                            UserRole newRole = UserRole.valueOf(roleStr);
                            if (newRole != UserSession.getInstance().getToken().getRole()) {
                                showAlert("Zmiana uprawnień", "Twoja rola została zmieniona. Wymagane ponowne zalogowanie.", Alert.AlertType.INFORMATION);
                                forceLogout();
                                return;
                            }
                        }
                        refreshData();
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            MainController.setLoading(false);
                            String error = ApiClient.getErrorMessage(ex);
                            showAlert("Błąd zapisu", "Serwer odrzucił dane:\n" + error, Alert.AlertType.ERROR);
                        });
                        return null;
                    });
        });
    }

    private Map<String, Object> buildMap(TextField u, PasswordField p, TextField fn, TextField ln, TextField em, TextField ph, TextField s, DatePicker d, UserRole role) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", u.getText());
        if (p != null && !p.getText().isEmpty()) {
            map.put("password", p.getText());
        }
        map.put("firstName", fn.getText());
        map.put("lastName", ln.getText());
        map.put("email", em.getText());
        map.put("role", role.name());
        map.put("phoneNumber", ph.getText());
        map.put("salary", Double.parseDouble(s.getText().isEmpty() ? "0" : s.getText()));
        map.put("hireDate", d.getValue().toString());
        return map;
    }

    private void refreshData() {
        MainController.setLoading(true);
        apiClient.send("/staff", "GET", null, StaffDTO[].class)
                .thenAccept(arr -> Platform.runLater(() -> {
                    MainController.setLoading(false);
                    if (arr != null) masterData.setAll(arr);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        MainController.setLoading(false);
                        String error = ApiClient.getErrorMessage(ex);
                        if (error.contains("401") || error.contains("Unauthorized")) {
                            showAlert("Sesja wygasła", "Twoja sesja dobiegła końca. Zaloguj się ponownie.", Alert.AlertType.WARNING);
                            forceLogout();
                        } else {
                            showAlert("Błąd", "Błąd podczas pobierania danych: " + error, Alert.AlertType.ERROR);
                        }
                    });
                    return null;
                });
    }

    private void setupFiltering() {
        FilteredList<StaffDTO> filtered = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((o, old, v) -> {
            String f = v.toLowerCase().trim();
            filtered.setPredicate(s -> f.isEmpty() || Stream.of(
                    s.getUser().getFullName(),
                    s.getUser().getEmail(),
                    s.getUser().getUsername()
            ).anyMatch(field -> field != null && field.toLowerCase().contains(f)));
        });
        SortedList<StaffDTO> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(staffTable.comparatorProperty());
        staffTable.setItems(sorted);
    }

    private void handleDemote(StaffDTO s) {
        String currentUser = UserSession.getInstance().getToken().getUsername();
        boolean isSelfDemote = s.getUser().getUsername().equals(currentUser);
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Potwierdzenie zwolnienia");
        a.setHeaderText(null);
        a.setContentText("Czy na pewno chcesz zwolnić pracownika " + s.getUser().getUsername() + "?");
        styleControl(a, "Tak, zwolnij", "Anuluj");
        a.showAndWait().filter(r -> r.getButtonData().isDefaultButton()).ifPresent(r -> {
            MainController.setLoading(true);
            apiClient.send("/staff/" + s.getId(), "DELETE", null, Void.class)
                    .thenRun(() -> {
                        if (isSelfDemote) {
                            forceLogout();
                        } else {
                            Platform.runLater(this::refreshData);
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            MainController.setLoading(false);
                            String error = ApiClient.getErrorMessage(ex);
                            showAlert("Błąd", "Nie udało się zwolnić pracownika: " + error, Alert.AlertType.ERROR);
                        });
                        return null;
                    });
        });
    }

    @FXML private void handleAddNewStaff() { showStaffDialog(null); }

    private void styleControl(Dialog<?> d, String okT, String canT) {
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(getClass().getResource("/com/project/crud/frontend/style.css").toExternalForm());
        p.getStyleClass().add("root-container");
        if (canT == null) {
            p.getButtonTypes().setAll(ButtonType.OK);
        } else {
            p.getButtonTypes().setAll(new ButtonType(okT, ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        }
        Button ok = (Button) p.lookupButton(p.getButtonTypes().get(0));
        if (ok != null) {
            ok.getStyleClass().add("button-primary");
            ok.setText(okT);
        }
        if (canT != null && p.getButtonTypes().size() > 1) {
            Button can = (Button) p.lookupButton(ButtonType.CANCEL);
            can.getStyleClass().add("button-outline-danger");
            can.setText(canT);
        }
    }

    private void showAlert(String t, String c, Alert.AlertType type) {
        Alert a = new Alert(type, c);
        a.setTitle(t);
        a.setHeaderText(null);
        styleControl(a, "Rozumiem", null);
        a.showAndWait();
    }

    private void forceLogout() {
        Platform.runLater(() -> {
            try {
                UserSession.logout();
                Stage stage = (Stage) staffTable.getScene().getWindow();
                var loader = new FXMLLoader(getClass().getResource("/com/project/crud/frontend/login-view.fxml"));
                stage.setScene(new Scene(loader.load(), 1200, 900));
                stage.setTitle("Logowanie");
            } catch (Exception ignored) {

            }
        });
    }
}