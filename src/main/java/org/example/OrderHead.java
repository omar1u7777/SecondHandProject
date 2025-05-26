package org.example;

import java.time.LocalDate;

/**
 * Representerar ett orderhuvud.
 */
public record OrderHead(long id, LocalDate orderDate, long customerId, long employeeId) {
    public OrderHead {
        if (orderDate == null) {
            throw new IllegalArgumentException("Order date cannot be null");
        }
    }
}