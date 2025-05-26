package org.example;

import java.time.LocalDate;

/**
 * Representerar detaljerad information om en order.
 */
public record OrderDetail(
        long orderId,
        LocalDate orderDate,
        String customerFirstName,
        String customerLastName,
        String furnitureName,
        double price,
        int quantity
) {
    public OrderDetail {
        if (orderDate == null) {
            throw new IllegalArgumentException("Order date cannot be null");
        }
        if (customerFirstName == null || customerFirstName.isEmpty()) {
            throw new IllegalArgumentException("Customer first name cannot be null or empty");
        }
        if (customerLastName == null || customerLastName.isEmpty()) {
            throw new IllegalArgumentException("Customer last name cannot be null or empty");
        }
        if (furnitureName == null || furnitureName.isEmpty()) {
            throw new IllegalArgumentException("Furniture name cannot be null or empty");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    @Override
    public String toString() {
        return "Order ID: " + orderId + ", Order Date: " + orderDate + ", Customer: " + customerFirstName + " " + customerLastName +
                ", Furniture: " + furnitureName + ", Quantity: " + quantity + ", Price: " + price;
    }
}