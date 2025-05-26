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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class OrderController implements Initializable {

    private static final Logger logger = Logger.getLogger(OrderController.class.getName());
    private final OrderDao orderDao = new OrderDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final EmployeeDao employeeDao = new EmployeeDao();
    private final FurnitureDao furnitureDao = new FurnitureDao();

    @FXML private TextField orderIdField;
    @FXML private DatePicker orderDatePicker;
    @FXML private TextField customerIdField;
    @FXML private TextField employeeIdField;
    @FXML private TextField filterField;

    @FXML private Button createOrderButton;
    @FXML private Button showOrdersButton;
    @FXML private Button showOrdersWithNamesButton;
    @FXML private Button showOrderDetailsButton;
    @FXML private Button deleteOrderButton;
    @FXML private Button showOrderValueButton;

    @FXML private TableView<OrderHead> orderTable;
    @FXML private TableColumn<OrderHead, Long> idColumn;
    @FXML private TableColumn<OrderHead, LocalDate> orderDateColumn;
    @FXML private TableColumn<OrderHead, Long> customerIdColumn;
    @FXML private TableColumn<OrderHead, Long> employeeIdColumn;

    @FXML private TableView<OrderDetail> detailTable;
    @FXML private TableColumn<OrderDetail, Long> detailIdColumn;
    @FXML private TableColumn<OrderDetail, LocalDate> detailOrderDateColumn;
    @FXML private TableColumn<OrderDetail, String> customerNameColumn;
    @FXML private TableColumn<OrderDetail, String> furnitureNameColumn;
    @FXML private TableColumn<OrderDetail, Integer> quantityColumn;
    @FXML private TableColumn<OrderDetail, Double> priceColumn;

    @FXML private Label feedbackLabel;

    private final ObservableList<OrderHead> orderList = FXCollections.observableArrayList();
    private final ObservableList<OrderDetail> detailList = FXCollections.observableArrayList();
    private FilteredList<OrderHead> filteredOrderList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup OrderHead table
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        orderDateColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().orderDate()));
        customerIdColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().customerId()));
        employeeIdColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().employeeId()));

        // Setup filtered list for orders
        filteredOrderList = new FilteredList<>(orderList, p -> true);
        orderTable.setItems(filteredOrderList);

        // Setup OrderDetail table
        detailIdColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().orderId()));
        detailOrderDateColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().orderDate()));
        customerNameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
                c.getValue().customerFirstName() + " " + c.getValue().customerLastName()));
        furnitureNameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().furnitureName()));
        quantityColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().quantity()));
        priceColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().price()));
        detailTable.setItems(detailList);

        // Load initial data
        showAllOrders();

        // Table row click handler for orders
        orderTable.setOnMouseClicked((MouseEvent me) -> {
            OrderHead sel = orderTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                populateOrderFields(sel);
                feedbackLabel.setText("");
            }
        });

        // Real-time validation
        customerIdField.textProperty().addListener((o, old, nw) -> validateField(customerIdField, "\\d+"));
        employeeIdField.textProperty().addListener((o, old, nw) -> validateField(employeeIdField, "\\d+"));
        orderIdField.textProperty().addListener((o, old, nw) -> validateField(orderIdField, "\\d+"));
        orderDatePicker.valueProperty().addListener((o, old, nw) -> validateDatePicker(orderDatePicker));

        // Filter field listener
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredOrderList.setPredicate(order -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return String.valueOf(order.customerId()).contains(lowerCaseFilter) ||
                        String.valueOf(order.employeeId()).contains(lowerCaseFilter);
            });
        });

        // Button event handlers
        createOrderButton.setOnAction(e -> createOrder());
        showOrdersButton.setOnAction(e -> showAllOrders());
        showOrdersWithNamesButton.setOnAction(e -> showOrdersWithCustomerNameForEmployee());
        showOrderDetailsButton.setOnAction(e -> showOrderDetailsForEmployee());
        deleteOrderButton.setOnAction(e -> deleteOrder());
        showOrderValueButton.setOnAction(e -> showOrderValue());
    }

    @FXML
    private void createOrder() {
        if (!collectAndValidateInput()) return;
        try {
            long cid = Long.parseLong(customerIdField.getText().trim());
            long eid = Long.parseLong(employeeIdField.getText().trim());

            // Validate customer and employee existence
            Customer customer = customerDao.getCustomerById(cid);
            if (customer == null) {
                feedbackLabel.setText("Kund med ID " + cid + " finns inte.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                return;
            }
            if (employeeDao.getEmployeeById(eid) == null) {
                feedbackLabel.setText("Anställd med ID " + eid + " finns inte.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                return;
            }

            // Dialog for adding order lines
            List<OrderLine> lines = new ArrayList<>();
            while (true) {
                TextInputDialog furnitureDialog = new TextInputDialog();
                furnitureDialog.setTitle("Lägg till orderrad");
                furnitureDialog.setHeaderText("Ange möbel-ID (eller lämna tomt för att avsluta)");
                furnitureDialog.setContentText("Möbel-ID:");
                String furnitureId = furnitureDialog.showAndWait().orElse("");
                if (furnitureId.isEmpty()) break;

                try {
                    long fid = Long.parseLong(furnitureId);
                    Furniture f = furnitureDao.getFurnitureById(fid);
                    if (f == null) {
                        feedbackLabel.setText("Möbel med ID " + fid + " finns inte.");
                        feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                        continue;
                    }

                    TextInputDialog qtyDialog = new TextInputDialog("1");
                    qtyDialog.setTitle("Lägg till orderrad");
                    qtyDialog.setHeaderText("Ange antal för möbel: " + f.name());
                    qtyDialog.setContentText("Antal:");
                    String qty = qtyDialog.showAndWait().orElse("");
                    int quantity = Integer.parseInt(qty);
                    if (quantity <= 0) {
                        feedbackLabel.setText("Antal måste vara större än 0.");
                        feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                        continue;
                    }

                    lines.add(new OrderLine(0, 0, fid, quantity));
                } catch (NumberFormatException ex) {
                    feedbackLabel.setText("Ogiltigt möbel-ID eller antal.");
                    feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                }
            }

            if (lines.isEmpty()) {
                feedbackLabel.setText("Minst en orderrad krävs.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                return;
            }

            OrderHead order = new OrderHead(0, orderDatePicker.getValue(), cid, eid);
            orderDao.createOrder(order, lines);
            feedbackLabel.setText("Order skapad!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            showAllOrders();
            clearFields();
        } catch (Exception ex) {
            feedbackLabel.setText("Fel: " + ex.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            logger.severe(ex.toString());
        }
    }

    @FXML
    private void showAllOrders() {
        try {
            orderList.setAll(orderDao.getAllOrders());
            filterField.clear();
            detailList.clear();
            feedbackLabel.setText("Alla ordrar visas.");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
        } catch (Exception ex) {
            feedbackLabel.setText("Fel vid hämtning: " + ex.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            logger.severe(ex.toString());
        }
    }

    @FXML
    private void showOrdersWithCustomerNameForEmployee() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Visa ordrar med kundnamn");
        dialog.setHeaderText("Ange anställd-ID");
        dialog.setContentText("Anställd-ID:");
        dialog.showAndWait().ifPresent(employeeId -> {
            try {
                long eid = Long.parseLong(employeeId);
                if (employeeDao.getEmployeeById(eid) == null) {
                    feedbackLabel.setText("Anställd med ID " + eid + " finns inte.");
                    feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                    return;
                }
                detailList.setAll(orderDao.listOrdersWithCustomerNameForEmployee(eid));
                feedbackLabel.setText("Ordrar med kundnamn för anställd " + eid + " visas.");
                feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            } catch (NumberFormatException ex) {
                feedbackLabel.setText("Ogiltigt anställd-ID.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            } catch (Exception ex) {
                feedbackLabel.setText("Fel: " + ex.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                logger.severe(ex.toString());
            }
        });
    }

    @FXML
    private void showOrderDetailsForEmployee() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Visa orderdetaljer");
        dialog.setHeaderText("Ange anställd-ID");
        dialog.setContentText("Anställd-ID:");
        dialog.showAndWait().ifPresent(employeeId -> {
            try {
                long eid = Long.parseLong(employeeId);
                if (employeeDao.getEmployeeById(eid) == null) {
                    feedbackLabel.setText("Anställd med ID " + eid + " finns inte.");
                    feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                    return;
                }
                detailList.setAll(orderDao.listOrdersWithDetailsForEmployee(eid));
                feedbackLabel.setText("Detaljerade ordrar för anställd " + eid + " visas.");
                feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            } catch (NumberFormatException ex) {
                feedbackLabel.setText("Ogiltigt anställd-ID.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            } catch (Exception ex) {
                feedbackLabel.setText("Fel: " + ex.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                logger.severe(ex.toString());
            }
        });
    }

    @FXML
    private void deleteOrder() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ta bort order");
        dialog.setHeaderText("Ange order-ID");
        dialog.setContentText("Order-ID:");
        dialog.showAndWait().ifPresent(orderId -> {
            try {
                long oid = Long.parseLong(orderId);
                orderDao.deleteOrder(oid);
                feedbackLabel.setText("Order " + oid + " borttagen!");
                feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
                showAllOrders();
                clearFields();
            } catch (NumberFormatException ex) {
                feedbackLabel.setText("Ogiltigt order-ID.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            } catch (Exception ex) {
                feedbackLabel.setText("Fel: " + ex.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                logger.severe(ex.toString());
            }
        });
    }

    @FXML
    private void showOrderValue() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Visa ordervärde");
        dialog.setHeaderText("Ange order-ID");
        dialog.setContentText("Order-ID:");
        dialog.showAndWait().ifPresent(orderId -> {
            try {
                long oid = Long.parseLong(orderId);
                double sum = orderDao.getTotalOrderValue(oid);
                feedbackLabel.setText(String.format("Totalt värde (order %d): %.2f", oid, sum));
                feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            } catch (NumberFormatException ex) {
                feedbackLabel.setText("Ogiltigt order-ID.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            } catch (Exception ex) {
                feedbackLabel.setText("Fel: " + ex.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                logger.severe(ex.toString());
            }
        });
    }

    private boolean collectAndValidateInput() {
        if (orderDatePicker.getValue() == null ||
                customerIdField.getText().isBlank() ||
                employeeIdField.getText().isBlank()) {
            feedbackLabel.setText("Alla fält måste fyllas i.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        if (orderDatePicker.getValue().isAfter(LocalDate.now())) {
            feedbackLabel.setText("Orderdatum kan inte vara i framtiden.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        if (!Pattern.matches("\\d+", customerIdField.getText().trim()) ||
                !Pattern.matches("\\d+", employeeIdField.getText().trim())) {
            feedbackLabel.setText("Kund-ID och anställd-ID måste vara numeriska.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        return true;
    }

    private void populateOrderFields(OrderHead o) {
        orderIdField.setText(String.valueOf(o.id()));
        orderDatePicker.setValue(o.orderDate());
        customerIdField.setText(String.valueOf(o.customerId()));
        employeeIdField.setText(String.valueOf(o.employeeId()));
    }

    private void clearFields() {
        orderIdField.clear();
        orderDatePicker.setValue(null);
        customerIdField.clear();
        employeeIdField.clear();
        detailList.clear();
        feedbackLabel.setText("");
    }

    private void validateField(TextField tf, String regex) {
        if (tf.getText().trim().isEmpty() || Pattern.matches(regex, tf.getText().trim())) {
            tf.setStyle("");
        } else {
            tf.setStyle("-fx-border-color: red;");
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