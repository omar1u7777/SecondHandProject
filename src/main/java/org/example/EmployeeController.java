package org.example;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class EmployeeController implements Initializable {
    private static final Logger logger = Logger.getLogger(EmployeeController.class.getName());
    private final EmployeeDao employeeDao = new EmployeeDao();
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredEmployeeList;

    @FXML private TextField idField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField filterField;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Long> idColumn;
    @FXML private TableColumn<Employee, String> firstNameColumn;
    @FXML private TableColumn<Employee, String> lastNameColumn;
    @FXML private TableColumn<Employee, String> emailColumn;
    @FXML private TableColumn<Employee, String> phoneColumn;
    @FXML private TableColumn<Employee, LocalDate> hireDateColumn;
    @FXML private Label feedbackLabel;

    @FXML private Button addButton;
    @FXML private Button showAllButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup table columns
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        firstNameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().firstName()));
        lastNameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().lastName()));
        emailColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().email()));
        phoneColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().phone()));
        hireDateColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().hireDate()));

        // Setup filtered list for search
        filteredEmployeeList = new FilteredList<>(employeeList, p -> true);
        employeeTable.setItems(filteredEmployeeList);

        // Load initial data
        loadEmployeeData();

        // Table row click handler
        employeeTable.setOnMouseClicked((MouseEvent me) -> {
            Employee sel = employeeTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                populateFields(sel);
                feedbackLabel.setText("");
            }
        });

        // Real-time validation
        firstNameField.textProperty().addListener((o, old, nw) -> validateField(firstNameField, "[A-Za-zÅÄÖåäö ]+"));
        lastNameField.textProperty().addListener((o, old, nw) -> validateField(lastNameField, "[A-Za-zÅÄÖåäö ]+"));
        emailField.textProperty().addListener((o, old, nw) -> validateField(emailField, "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"));
        phoneField.textProperty().addListener((o, old, nw) -> validateField(phoneField, "\\d{10}"));
        hireDatePicker.valueProperty().addListener((o, old, nw) -> validateDatePicker(hireDatePicker));

        // Filter field listener
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredEmployeeList.setPredicate(employee -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return employee.firstName().toLowerCase().contains(lowerCaseFilter) ||
                        employee.lastName().toLowerCase().contains(lowerCaseFilter) ||
                        employee.email().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Button event handlers
        addButton.setOnAction(e -> handleAddEmployee());
        showAllButton.setOnAction(e -> handleShowAllEmployees());
        updateButton.setOnAction(e -> handleUpdateEmployee());
        deleteButton.setOnAction(e -> handleDeleteEmployee());
        searchButton.setOnAction(e -> handleShowEmployeeById());
    }

    private void loadEmployeeData() {
        try {
            employeeList.setAll(employeeDao.getAllEmployees());
        } catch (Exception e) {
            logger.severe("Kunde inte ladda anställda: " + e.getMessage());
            feedbackLabel.setText("Fel vid inläsning: " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    private void populateFields(Employee emp) {
        idField.setText(String.valueOf(emp.id()));
        firstNameField.setText(emp.firstName());
        lastNameField.setText(emp.lastName());
        emailField.setText(emp.email());
        phoneField.setText(emp.phone());
        hireDatePicker.setValue(emp.hireDate());
    }

    @FXML
    private void handleAddEmployee() {
        if (!collectAndValidateInput()) return;
        try {
            Employee e = new Employee(
                    0L,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    hireDatePicker.getValue()
            );
            employeeDao.addEmployee(e);
            feedbackLabel.setText("Anställd tillagd!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            loadEmployeeData();
            clearFields();
        } catch (Exception e) {
            logger.severe("Fel vid tillägg: " + e.getMessage());
            feedbackLabel.setText("Fel: " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    @FXML
    private void handleShowAllEmployees() {
        loadEmployeeData();
        filterField.clear();
        feedbackLabel.setText("Alla anställda visas.");
        feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
    }

    @FXML
    private void handleUpdateEmployee() {
        Employee sel = employeeTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            feedbackLabel.setText("Välj en anställd först.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return;
        }
        if (!collectAndValidateInput()) return;
        try {
            Employee e = new Employee(
                    sel.id(),
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    hireDatePicker.getValue()
            );
            employeeDao.updateEmployee(e);
            feedbackLabel.setText("Anställd uppdaterad!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            loadEmployeeData();
            clearFields();
        } catch (Exception e) {
            logger.severe("Fel vid uppdatering: " + e.getMessage());
            feedbackLabel.setText("Fel: " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    @FXML
    private void handleDeleteEmployee() {
        Employee sel = employeeTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            feedbackLabel.setText("Välj en anställd att ta bort.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return;
        }
        try {
            employeeDao.deleteEmployee(sel.id());
            feedbackLabel.setText("Anställd borttagen!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            loadEmployeeData();
            clearFields();
        } catch (Exception e) {
            logger.severe("Fel vid borttagning: " + e.getMessage());
            feedbackLabel.setText("Fel: " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    @FXML
    private void handleShowEmployeeById() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sök anställd efter ID");
        dialog.setHeaderText("Ange anställd-ID");
        dialog.setContentText("Anställd-ID:");
        dialog.showAndWait().ifPresent(employeeId -> {
            try {
                long id = Long.parseLong(employeeId);
                Employee e = employeeDao.getEmployeeById(id);
                if (e != null) {
                    employeeList.setAll(e);
                    feedbackLabel.setText("Visar anställd: " + e.firstName() + " " + e.lastName());
                    feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
                    populateFields(e);
                } else {
                    feedbackLabel.setText("Ingen anställd med ID " + id + " hittades.");
                    feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                }
            } catch (NumberFormatException ex) {
                feedbackLabel.setText("Ogiltigt ID. Ange ett numeriskt värde.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            } catch (Exception ex) {
                logger.severe("Fel vid sökning: " + ex.getMessage());
                feedbackLabel.setText("Fel: " + ex.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            }
        });
    }

    private boolean collectAndValidateInput() {
        if (firstNameField.getText().isBlank() ||
                lastNameField.getText().isBlank() ||
                emailField.getText().isBlank() ||
                phoneField.getText().isBlank() ||
                hireDatePicker.getValue() == null) {
            feedbackLabel.setText("Alla fält måste fyllas i.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        if (!Pattern.matches("[A-Za-zÅÄÖåäö ]+", firstNameField.getText().trim()) ||
                !Pattern.matches("[A-Za-zÅÄÖåäö ]+", lastNameField.getText().trim()) ||
                !Pattern.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", emailField.getText().trim()) ||
                !Pattern.matches("\\d{10}", phoneField.getText().trim())) {
            feedbackLabel.setText("Ogiltigt format i ett eller flera fält.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        if (hireDatePicker.getValue().isAfter(LocalDate.now())) {
            feedbackLabel.setText("Anställningsdatum kan inte vara i framtiden.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        return true;
    }

    private void clearFields() {
        idField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        hireDatePicker.setValue(null);
        employeeTable.getSelectionModel().clearSelection();
        feedbackLabel.setText("");
    }

    private void validateField(TextField f, String regex) {
        if (f.getText().trim().isEmpty() || Pattern.matches(regex, f.getText().trim())) {
            f.setStyle("");
        } else {
            f.setStyle("-fx-border-color: red;");
        }
    }

    private void validateDatePicker(DatePicker dp) {
        if (dp.getValue() == null || !dp.getValue().isAfter(LocalDate.now())) {
            dp.setStyle("");
        } else {
            dp.setStyle("-fx-border-color: red;");
        }
    }
}