package org.example;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Data Access Object (DAO) för kundhantering.
 */
public class CustomerDao {
    private static final Logger logger = Logger.getLogger(CustomerDao.class.getName());
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("\\d{5}");

    public void addCustomer(Customer customer) {
        validateCustomerFields(customer);
        String sql = """
            INSERT INTO customer
              (first_name, last_name, address, birth_date, city, postal_code)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);
            try {
                stmt.setString(1, customer.firstName());
                stmt.setString(2, customer.lastName());
                stmt.setString(3, customer.address());
                stmt.setDate(4, Date.valueOf(customer.birthDate()));
                stmt.setString(5, customer.city());
                stmt.setString(6, customer.postalCode());

                int affected = stmt.executeUpdate();
                if (affected != 1) {
                    throw new SQLException("Förväntade 1 rad, blev: " + affected);
                }
                conn.commit();
                logger.info("Kund tillagd: " + customer);
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Misslyckades lägga till kund: " + customer, e);
                throw new RuntimeException("Kunde inte lägga till kund: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Databasfel vid tillägg av kund", e);
            throw new RuntimeException("Databasfel: " + e.getMessage(), e);
        }
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = """
            SELECT id, first_name, last_name, address, birth_date, city, postal_code
              FROM customer
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("address"),
                        rs.getDate("birth_date").toLocalDate(),
                        rs.getString("city"),
                        rs.getString("postal_code")
                ));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Misslyckades hämta alla kunder", e);
            throw new RuntimeException("Kunde inte hämta kunder: " + e.getMessage(), e);
        }
        return customers;
    }

    public Customer getCustomerById(long id) {
        String sql = """
            SELECT id, first_name, last_name, address, birth_date, city, postal_code
              FROM customer WHERE id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getLong("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("address"),
                            rs.getDate("birth_date").toLocalDate(),
                            rs.getString("city"),
                            rs.getString("postal_code")
                    );
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Misslyckades hämta kund med ID " + id, e);
            throw new RuntimeException("Kunde inte hämta kund: " + e.getMessage(), e);
        }
        return null;
    }

    public void updateCustomer(Customer customer) {
        validateCustomerExists(customer.id());
        validateCustomerFields(customer);
        String sql = """
            UPDATE customer
               SET first_name  = ?,
                   last_name   = ?,
                   address     = ?,
                   birth_date  = ?,
                   city        = ?,
                   postal_code = ?
             WHERE id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            try {
                stmt.setString(1, customer.firstName());
                stmt.setString(2, customer.lastName());
                stmt.setString(3, customer.address());
                stmt.setDate(4, Date.valueOf(customer.birthDate()));
                stmt.setString(5, customer.city());
                stmt.setString(6, customer.postalCode());
                stmt.setLong(7, customer.id());

                int rows = stmt.executeUpdate();
                if (rows != 1) {
                    throw new SQLException("Förväntade 1 uppdaterad rad, blev: " + rows);
                }
                conn.commit();
                logger.info("Kund uppdaterad: " + customer);
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Misslyckades uppdatera kund: " + customer, e);
                throw new RuntimeException("Kunde inte uppdatera kund: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Databasfel vid uppdatering av kund", e);
            throw new RuntimeException("Databasfel: " + e.getMessage(), e);
        }
    }

    public void deleteCustomer(long id) {
        validateCustomerExists(id);
        String deleteLines = "DELETE FROM order_line WHERE order_id IN (SELECT id FROM order_head WHERE customer_id = ?)";
        String deleteHeads = "DELETE FROM order_head WHERE customer_id = ?";
        String deleteCust  = "DELETE FROM customer WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt1 = conn.prepareStatement(deleteLines);
                 PreparedStatement stmt2 = conn.prepareStatement(deleteHeads);
                 PreparedStatement stmt3 = conn.prepareStatement(deleteCust)) {

                stmt1.setLong(1, id);
                stmt1.executeUpdate();

                stmt2.setLong(1, id);
                stmt2.executeUpdate();

                stmt3.setLong(1, id);
                int rows = stmt3.executeUpdate();
                if (rows != 1) {
                    throw new SQLException("Förväntade 1 borttagen kundrad, blev: " + rows);
                }
                conn.commit();
                logger.info("Kund " + id + " och relaterade ordrar borttagna.");
            } catch (SQLException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Misslyckades ta bort kund med ID " + id, e);
                throw new RuntimeException("Kunde inte ta bort kund: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Databasfel vid borttagning av kund", e);
            throw new RuntimeException("Databasfel: " + e.getMessage(), e);
        }
    }

    public boolean customerExists(long id) {
        String sql = "SELECT COUNT(*) FROM customer WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Misslyckades kontrollera kundexistens för ID " + id, e);
            throw new RuntimeException("Kunde inte kontrollera kund: " + e.getMessage(), e);
        }
    }

    private void validateCustomerFields(Customer c) {
        if (c.firstName().isBlank()
                || c.lastName().isBlank()
                || c.address().isBlank()
                || c.city().isBlank()
                || !POSTAL_CODE_PATTERN.matcher(c.postalCode()).matches()
                || c.birthDate() == null
                || c.birthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Alla fält måste fyllas i korrekt, postnummer 5 siffror, födelsedatum inte i framtiden.");
        }
    }

    private void validateCustomerExists(long id) {
        if (!customerExists(id)) {
            throw new IllegalArgumentException("Ingen kund med ID " + id);
        }
    }
}
