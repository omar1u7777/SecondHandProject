package org.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Label dashboardStats;

    private final CustomerDao customerDao = new CustomerDao();
    private final FurnitureDao furnitureDao = new FurnitureDao();
    private final OrderDao orderDao = new OrderDao();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateDashboardStats();
    }

    private void updateDashboardStats() {
        try {
            int customerCount = customerDao.getAllCustomers().size();
            int furnitureCount = furnitureDao.getAllFurniture().size();
            int orderCount = orderDao.getAllOrders().size();
            String stats = String.format("Antal kunder: %d\nAntal möbler: %d\nAntal ordrar: %d",
                    customerCount, furnitureCount, orderCount);
            dashboardStats.setText(stats);
        } catch (Exception e) {
            dashboardStats.setText("Kunde inte ladda statistik: " + e.getMessage());
        }
    }
}