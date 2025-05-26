package org.example;

/**
 * Representerar en orderlinje.
 */
public record OrderLine(long id, long orderId, long furnitureId, int quantity) {
    public OrderLine {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
}