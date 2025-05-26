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

public class FurnitureController implements Initializable {
    private static final Logger logger = Logger.getLogger(FurnitureController.class.getName());
    private final FurnitureDao furnitureDao = new FurnitureDao();
    private final ObservableList<Furniture> furnitureList = FXCollections.observableArrayList();
    private FilteredList<Furniture> filteredFurnitureList;

    @FXML private TableView<Furniture> furnitureTable;
    @FXML private TableColumn<Furniture, Long> idColumn;
    @FXML private TableColumn<Furniture, String> nameColumn;
    @FXML private TableColumn<Furniture, String> colorColumn;
    @FXML private TableColumn<Furniture, String> commentColumn;
    @FXML private TableColumn<Furniture, Double> priceColumn;
    @FXML private TableColumn<Furniture, LocalDate> purchaseDateColumn;
    @FXML private TableColumn<Furniture, Integer> shelfNbrColumn;
    @FXML private TableColumn<Furniture, Double> weightColumn;

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField colorField;
    @FXML private TextField commentField;
    @FXML private TextField priceField;
    @FXML private DatePicker purchaseDatePicker;
    @FXML private TextField shelfNbrField;
    @FXML private TextField weightField;
    @FXML private TextField filterField;
    @FXML private Label feedbackLabel;

    @FXML private Button addButton;
    @FXML private Button showAllButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button showByIdButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup table columns
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().name()));
        colorColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().color()));
        commentColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().comment()));
        priceColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().price()));
        purchaseDateColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().purchaseDate()));
        shelfNbrColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().shelfNbr()));
        weightColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().weight()));

        // Setup filtered list for search
        filteredFurnitureList = new FilteredList<>(furnitureList, p -> true);
        furnitureTable.setItems(filteredFurnitureList);

        // Load initial data
        loadFurniture();

        // Table row click handler
        furnitureTable.setOnMouseClicked((MouseEvent me) -> {
            Furniture sel = furnitureTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                populateFields(sel);
                feedbackLabel.setText("");
            }
        });

        // Real-time validation
        nameField.textProperty().addListener((o, a, n) -> validateField(nameField, ".+"));
        colorField.textProperty().addListener((o, a, n) -> validateField(colorField, "[A-Za-zÅÄÖåäö ]*"));
        priceField.textProperty().addListener((o, a, n) -> validateField(priceField, "\\d*(\\.\\d+)?"));
        shelfNbrField.textProperty().addListener((o, a, n) -> validateField(shelfNbrField, "\\d+"));
        weightField.textProperty().addListener((o, a, n) -> validateField(weightField, "\\d*(\\.\\d+)?"));
        purchaseDatePicker.valueProperty().addListener((o, a, n) -> validateDatePicker(purchaseDatePicker));

        // Filter field listener
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredFurnitureList.setPredicate(furniture -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return furniture.name().toLowerCase().contains(lowerCaseFilter) ||
                        furniture.color().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Button event handlers
        addButton.setOnAction(e -> handleAddFurniture());
        showAllButton.setOnAction(e -> showAllFurniture());
        updateButton.setOnAction(e -> handleUpdateFurniture());
        deleteButton.setOnAction(e -> handleDeleteFurniture());
        showByIdButton.setOnAction(e -> handleShowById());
    }

    @FXML
    private void handleAddFurniture() {
        if (!collectAndValidateInput()) return;
        try {
            Furniture f = new Furniture(
                    0L,
                    nameField.getText().trim(),
                    colorField.getText().trim(),
                    commentField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    purchaseDatePicker.getValue(),
                    Integer.parseInt(shelfNbrField.getText().trim()),
                    Double.parseDouble(weightField.getText().trim())
            );
            furnitureDao.addFurniture(f);
            feedbackLabel.setText("Möbel tillagd!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            loadFurniture();
            clearFields();
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("Ogiltigt numeriskt värde.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        } catch (Exception ex) {
            logger.severe("Fel vid tillägg: " + ex.getMessage());
            feedbackLabel.setText("Fel: " + ex.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    @FXML
    private void showAllFurniture() {
        loadFurniture();
        filterField.clear();
        clearFields();
        feedbackLabel.setText("Alla möbler visas.");
        feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
    }

    @FXML
    private void handleUpdateFurniture() {
        Furniture sel = furnitureTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            feedbackLabel.setText("Välj en möbel först.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return;
        }
        if (!collectAndValidateInput()) return;
        try {
            Furniture f = new Furniture(
                    sel.id(),
                    nameField.getText().trim(),
                    colorField.getText().trim(),
                    commentField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    purchaseDatePicker.getValue(),
                    Integer.parseInt(shelfNbrField.getText().trim()),
                    Double.parseDouble(weightField.getText().trim())
            );
            furnitureDao.updateFurniture(f);
            feedbackLabel.setText("Möbel uppdaterad!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            loadFurniture();
            clearFields();
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("Ogiltigt numeriskt värde.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        } catch (Exception ex) {
            logger.severe("Fel vid uppdatering: " + ex.getMessage());
            feedbackLabel.setText("Fel: " + ex.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    @FXML
    private void handleDeleteFurniture() {
        Furniture sel = furnitureTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            feedbackLabel.setText("Välj en möbel att ta bort.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return;
        }
        try {
            furnitureDao.deleteFurniture(sel.id());
            feedbackLabel.setText("Möbel borttagen!");
            feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
            loadFurniture();
            clearFields();
        } catch (Exception ex) {
            logger.severe("Fel vid borttagning: " + ex.getMessage());
            feedbackLabel.setText("Fel: " + ex.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    @FXML
    private void handleShowById() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sök möbel efter ID");
        dialog.setHeaderText("Ange möbel-ID");
        dialog.setContentText("Möbel-ID:");
        dialog.showAndWait().ifPresent(furnitureId -> {
            try {
                long id = Long.parseLong(furnitureId);
                Furniture f = furnitureDao.getFurnitureById(id);
                if (f != null) {
                    furnitureList.setAll(f);
                    feedbackLabel.setText("Visar möbel: " + f.name());
                    feedbackLabel.setStyle("-fx-text-fill: #388e3c;");
                    populateFields(f);
                } else {
                    feedbackLabel.setText("Ingen möbel med ID " + id + " hittades.");
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
        if (nameField.getText().isBlank() || purchaseDatePicker.getValue() == null) {
            feedbackLabel.setText("Namn och inköpsdatum måste fyllas i.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            int shelf = Integer.parseInt(shelfNbrField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());
            if (price < 0 || shelf < 0 || weight < 0) {
                feedbackLabel.setText("Pris, hyllnummer och vikt får inte vara negativa.");
                feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
                return false;
            }
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("Ogiltigt format för pris, hyllnummer eller vikt.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        if (purchaseDatePicker.getValue().isAfter(LocalDate.now())) {
            feedbackLabel.setText("Inköpsdatum kan inte vara i framtiden.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        if (!colorField.getText().isBlank() && !Pattern.matches("[A-Za-zÅÄÖåäö ]+", colorField.getText().trim())) {
            feedbackLabel.setText("Ogiltigt format för färg.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
        return true;
    }

    private void loadFurniture() {
        try {
            furnitureList.setAll(furnitureDao.getAllFurniture());
        } catch (Exception ex) {
            logger.severe("Kunde inte ladda möbler: " + ex.getMessage());
            feedbackLabel.setText("Fel vid inläsning: " + ex.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    private void populateFields(Furniture furniture) {
        idField.setText(String.valueOf(furniture.id()));
        nameField.setText(furniture.name());
        colorField.setText(furniture.color());
        commentField.setText(furniture.comment());
        priceField.setText(String.valueOf(furniture.price()));
        purchaseDatePicker.setValue(furniture.purchaseDate());
        shelfNbrField.setText(String.valueOf(furniture.shelfNbr()));
        weightField.setText(String.valueOf(furniture.weight()));
    }

    private void clearFields() {
        idField.clear();
        nameField.clear();
        colorField.clear();
        commentField.clear();
        priceField.clear();
        purchaseDatePicker.setValue(null);
        shelfNbrField.clear();
        weightField.clear();
        furnitureTable.getSelectionModel().clearSelection();
        feedbackLabel.setText("");
    }

    private void validateField(TextField field, String regex) {
        if (field.getText().trim().isEmpty() || Pattern.matches(regex, field.getText().trim())) {
            field.setStyle("");
        } else {
            field.setStyle("-fx-border-color: red;");
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