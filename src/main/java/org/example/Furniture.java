package org.example;

import java.time.LocalDate;

/**
 * Representerar en möbel.
 */
public record Furniture(
        long id,
        String name,
        String color,
        String comment,
        double price,
        LocalDate purchaseDate,
        int shelfNbr,
        double weight
) {
    public Furniture {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Name cannot be null or empty");
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        if (weight < 0) throw new IllegalArgumentException("Weight cannot be negative");
        if (shelfNbr < 0) throw new IllegalArgumentException("Shelf number cannot be negative");
        if (purchaseDate == null) throw new IllegalArgumentException("Purchase date cannot be null");
    }
}