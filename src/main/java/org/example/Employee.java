package org.example;

import java.time.LocalDate;

/**
 * Representerar en anställd.
 */
public record Employee(
        long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate hireDate
) {
    public Employee {
        if (firstName == null || firstName.isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (hireDate == null) {
            throw new IllegalArgumentException("Hire date cannot be null");
        }
    }
}