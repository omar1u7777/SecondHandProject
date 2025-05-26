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

public class CustomerController implements Initializable {

    private static final Logger logger = Logger.getLogger(CustomerController.class.getName());

    @FXML private TextField idField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField addressField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField cityField;
    @FXML private TextField postalCodeField;
    @FXML private TextField filterField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Long> idColumn;
    @FXML private TableColumn<Customer, String> firstNameColumn;
    @FXML private TableColumn<Customer, String> lastNameColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private TableColumn<Customer, LocalDate> birthDateColumn;
    @FXML private TableColumn<Customer, String> cityColumn;
    @FXML private TableColumn<Customer, String> postalCodeColumn;
    @FXML private Label feedbackLabel;

    private final CustomerDao customerDao = new CustomerDao();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredCustomerList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup table columns
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        firstNameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().firstName()));
        lastNameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().lastName()));
        addressColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().address()));
        birthDateColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().birthDate()));
        cityColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().city()));
        postalCodeColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().postalCode()));

        // Setup filtered list for search
        filteredCustomerList = new FilteredList<>(customerList, p -> true);
        customerTable.setItems(filteredCustomerList);

        // Load initial data
        reloadTable();

        // Table row click handler
        customerTable.setOnMouseClicked((MouseEvent me) -> {
            Customer sel = customerTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                idField.setText(String.valueOf(sel.id()));
                firstNameField.setText(sel.firstName());
                lastNameField.setText(sel.lastName());
                addressField.setText(sel.address());
                birthDatePicker.setValue(sel.birthDate());
                cityField.setText(sel.city());
                postalCodeField.setText(sel.postalCode());
                feedbackLabel.setText("");
            }
        });

        // Real-time validation
        firstNameField.textProperty().addListener((o, old, nw) -> validate(firstNameField, "[A-Za-zÅÄÖåäö ]+"));
        lastNameField.textProperty().addListener((o, old, nw) -> validate(lastNameField, "[A-Za-zÅÄÖåäö ]+"));
        addressField.textProperty().addListener((o, old, nw) -> validate(addressField, ".+"));
        cityField.textProperty().addListener((o, old, nw) -> validate(cityField, "[A-Za-zÅÄÖåäö ]+"));
        postalCodeField.textProperty().addListener((o, old, nw) -> validate(postalCodeField, "\\d{5}"));

        // Filter field listener
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCustomerList.setPredicate(customer -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return customer.firstName().toLowerCase().contains(lowerCaseFilter) ||
                        customer.lastName().toLowerCase().contains(lowerCaseFilter) ||
                        customer.city().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    @FXML
    private void addCustomer() {
        if (!collectAndValidateInput()) return;
        try {
            Customer c = new Customer(
                    0L,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    addressField.getText().trim(),
                    birthDatePicker.getValue(),
                    cityField.getText().trim(),
                    postalCodeField.getText().trim()
            );
            customerDao.addCustomer(c);
            feedbackLabel.setText("Kund tillagd!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;"); // feedback-success
            reloadTable();
            clearForm();
        } catch (Exception e) {
            feedbackLabel.setText("Fel: " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;"); // feedback-error
            logger.severe(e.toString());
        }
    }

    @FXML
    private void showAllCustomers() {
        reloadTable();
        filterField.clear();
        feedbackLabel.setText("Alla kunder visas.");
        feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
    }

    @FXML
    private void updateCustomer() {
        if (!collectAndValidateInput()) return;
        try {
            long id = Long.parseLong(idField.getText().trim());
            Customer c = new Customer(
                    id,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    addressField.getText().trim(),
                    birthDatePicker.getValue(),
                    cityField.getText().trim(),
                    postalCodeField.getText().trim()
            );
            customerDao.updateCustomer(c);
            feedbackLabel.setText("Kund uppdaterad!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            reloadTable();
            clearForm();
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("Ogiltigt ID.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        } catch (Exception e) {
            feedbackLabel.setText("Fel: " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            logger.severe(e.toString());
        }
    }

    @FXML
    private void deleteCustomer() {
        try {
            long id = Long.parseLong(idField.getText().trim());
            customerDao.deleteCustomer(id);
            feedbackLabel.setText("Kund borttagen!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            reloadTable();
            clearForm();
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("Ogiltigt ID.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        } catch (Exception e) {
            feedbackLabel.setText("Fel: " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            logger.severe(e.toString());
        }
    }

    @FXML
    private void showCustomerById() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sök kund efter ID");
        dialog.setHeaderText("Ange kund-ID");
        dialog.setContentText("Kund-ID:");
        dialog.showAndWait().ifPresent(customerId -> {
            try {
                long id = Long.parseLong(customerId);
                Customer c = customerDao.getCustomerById(id);
                if (c != null) {
                    customerList.setAll(c);
                    feedbackLabel.setText("Visar kund: " + c.firstName() + " " + c.lastName());
                    feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
                    idField.setText(String.valueOf(c.id()));
                    firstNameField.setText(c.firstName());
                    lastNameField.setText(c.lastName());
                    addressField.setText(c.address());
                    birthDatePicker.setValue(c.birthDate());
                    cityField.setText(c.city());
                    postalCodeField.setText(c.postalCode());
                } else {
                    feedbackLabel.setText("Ingen kund med ID " + id + " hittades.");
                    feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                }
            } catch (NumberFormatException ex) {
                feedbackLabel.setText("Ogiltigt ID. Ange ett numeriskt värde.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            } catch (Exception e) {
                feedbackLabel.setText("Fel vid sökning: " + e.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                logger.severe(e.toString());
            }
        });
    }

    private boolean collectAndValidateInput() {
        if (firstNameField.getText().isBlank() ||
                lastNameField.getText().isBlank() ||
                addressField.getText().isBlank() ||
                birthDatePicker.getValue() == null ||
                cityField.getText().isBlank() ||
                postalCodeField.getText().isBlank()) {
            feedbackLabel.setText("Alla fält måste fyllas i.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        if (!Pattern.matches("[A-Za-zÅÄÖåäö ]+", firstNameField.getText().trim()) ||
                !Pattern.matches("[A-Za-zÅÄÖåäö ]+", lastNameField.getText().trim()) ||
                !Pattern.matches("[A-Za-zÅÄÖåäö ]+", cityField.getText().trim()) ||
                !Pattern.matches("\\d{5}", postalCodeField.getText().trim())) {
            feedbackLabel.setText("Ogiltigt format i ett eller flera fält.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        return true;
    }

    private void reloadTable() {
        try {
            customerList.setAll(customerDao.getAllCustomers());
        } catch (Exception e) {
            feedbackLabel.setText("Kunde inte ladda kunder: " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            logger.severe(e.toString());
        }
    }

    private void clearForm() {
        idField.clear();
        firstNameField.clear();
        lastNameField.clear();
        addressField.clear();
        birthDatePicker.setValue(null);
        cityField.clear();
        postalCodeField.clear();
        feedbackLabel.setText("");
    }

    private void validate(TextField tf, String regex) {
        if (tf.getText().trim().isEmpty() || Pattern.matches(regex, tf.getText().trim())) {
            tf.setStyle("");
        } else {
            tf.setStyle("-fx-border-color: red;");
        }
    }
}