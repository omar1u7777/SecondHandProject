package org.example;

import java.time.LocalDate;

/**
 * Representerar en kund.
 */
public record Customer(
        long id,
        String firstName,
        String lastName,
        String address,
        LocalDate birthDate,
        String city,
        String postalCode
) {
    public Customer {
        if (firstName == null || firstName.isEmpty()) throw new IllegalArgumentException("First name cannot be null or empty");
        if (lastName == null || lastName.isEmpty())  throw new IllegalArgumentException("Last name cannot be null or empty");
        if (address == null || address.isEmpty())   throw new IllegalArgumentException("Address cannot be null or empty");
        if (city == null || city.isEmpty())         throw new IllegalArgumentException("City cannot be null or empty");
        if (postalCode == null || postalCode.isEmpty()) throw new IllegalArgumentException("Postal code cannot be null or empty");
        if (birthDate == null)                      throw new IllegalArgumentException("Birth date cannot be null");
    }

    @Override
    public String toString() {
        return String.format(
                "%d: %s %s, %s, %s %s, född %s",
                id, firstName, lastName, address, postalCode, city, birthDate
        );
    }
}
