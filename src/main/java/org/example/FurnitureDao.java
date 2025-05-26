package org.example;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) for managing furniture in the database.
 * Provides methods to create, retrieve, update, and delete furniture items.
 */
public class FurnitureDao {
    private static final Logger logger = Logger.getLogger(FurnitureDao.class.getName());

    /**
     * Adds a new furniture item to the database.
     *
     * @param furniture The {@link Furniture} object containing furniture details.
     * @throws IllegalArgumentException if required fields (name, color) are empty or invalid.
     * @throws RuntimeException if a database error occurs.
     */
    public void addFurniture(Furniture furniture) {
        String sql = "INSERT INTO furniture (name, color, comment, price, purchase_date, shelf_nbr, weight) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                validateFurnitureFields(furniture);
                stmt.setString(1, furniture.name());
                stmt.setString(2, furniture.color());
                stmt.setString(3, furniture.comment());
                stmt.setDouble(4, furniture.price());
                stmt.setDate(5, Date.valueOf(furniture.purchaseDate()));
                stmt.setInt(6, furniture.shelfNbr());
                stmt.setDouble(7, furniture.weight());
                stmt.executeUpdate();
                conn.commit();
                System.out.println("Furniture added: " + furniture.name());
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Failed to add furniture: {0}", furniture);
                throw new RuntimeException("Could not add furniture: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while adding furniture", e);
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all furniture items from the database.
     *
     * @return A list of {@link Furniture} objects.
     * @throws RuntimeException if a database error occurs.
     */
    public List<Furniture> getAllFurniture() {
        List<Furniture> furnitureList = new ArrayList<>();
        String sql = "SELECT id, name, color, comment, price, purchase_date, shelf_nbr, weight FROM furniture";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                furnitureList.add(new Furniture(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("color"),
                        rs.getString("comment"),
                        rs.getDouble("price"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getInt("shelf_nbr"),
                        rs.getDouble("weight")
                ));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve furniture", e);
            throw new RuntimeException("Could not retrieve furniture: " + e.getMessage(), e);
        }
        return furnitureList;
    }

    /**
     * Retrieves a furniture item by its ID.
     *
     * @param furnitureId The ID of the furniture item.
     * @return The {@link Furniture} object, or null if not found.
     * @throws RuntimeException if a database error occurs.
     */
    public Furniture getFurnitureById(long furnitureId) {
        String sql = "SELECT id, name, color, comment, price, purchase_date, shelf_nbr, weight FROM furniture WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, furnitureId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Furniture(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("color"),
                            rs.getString("comment"),
                            rs.getDouble("price"),
                            rs.getDate("purchase_date").toLocalDate(),
                            rs.getInt("shelf_nbr"),
                            rs.getDouble("weight")
                    );
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve furniture with ID: {0}", furnitureId);
            throw new RuntimeException("Could not retrieve furniture: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Updates an existing furniture item in the database.
     *
     * @param furniture The {@link Furniture} object with updated details.
     * @throws IllegalArgumentException if the furniture does not exist or required fields are empty.
     * @throws RuntimeException if a database error occurs.
     */
    public void updateFurniture(Furniture furniture) {
        String sql = "UPDATE furniture SET name = ?, color = ?, comment = ?, price = ?, purchase_date = ?, shelf_nbr = ?, weight = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            validateFurnitureExists(furniture.id());
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                validateFurnitureFields(furniture);
                stmt.setString(1, furniture.name());
                stmt.setString(2, furniture.color());
                stmt.setString(3, furniture.comment());
                stmt.setDouble(4, furniture.price());
                stmt.setDate(5, Date.valueOf(furniture.purchaseDate()));
                stmt.setInt(6, furniture.shelfNbr());
                stmt.setDouble(7, furniture.weight());
                stmt.setLong(8, furniture.id());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No furniture was updated, check ID: " + furniture.id());
                }
                conn.commit();
                System.out.println("Furniture updated: " + furniture.name());
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Failed to update furniture: {0}", furniture);
                throw new RuntimeException("Could not update furniture: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating furniture", e);
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a furniture item and its associated order lines from the database.
     *
     * @param id The ID of the furniture item to delete.
     * @throws IllegalArgumentException if the furniture does not exist.
     * @throws RuntimeException if a database error occurs.
     */
    public void deleteFurniture(long id) {
        String deleteOrderLinesSql = "DELETE FROM order_line WHERE furniture_id = ?";
        String deleteFurnitureSql = "DELETE FROM furniture WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            validateFurnitureExists(id);
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(deleteOrderLinesSql)) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteFurnitureSql)) {
                    stmt.setLong(1, id);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("No furniture was deleted, check ID: " + id);
                    }
                }
                conn.commit();
                System.out.println("Furniture with ID " + id + " deleted successfully!");
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Failed to delete furniture with ID: {0}", id);
                throw new RuntimeException("Could not delete furniture: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while deleting furniture", e);
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a furniture item exists in the database.
     *
     * @param furnitureId The ID of the furniture item.
     * @return true if the furniture exists, false otherwise.
     * @throws RuntimeException if a database error occurs.
     */
    public boolean furnitureExists(long furnitureId) {
        String sql = "SELECT COUNT(*) FROM furniture WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, furnitureId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to check furniture existence for ID: {0}", furnitureId);
            throw new RuntimeException("Could not check furniture existence: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Validates that all required furniture fields are non-empty and valid.
     *
     * @param furniture The {@link Furniture} object to validate.
     * @throws IllegalArgumentException if required fields are empty or invalid.
     */
    private void validateFurnitureFields(Furniture furniture) {
        if (furniture.name() == null || furniture.name().isEmpty()) {
            throw new IllegalArgumentException("Furniture name must be non-empty.");
        }
        if (furniture.color() == null || furniture.color().isEmpty()) {
            throw new IllegalArgumentException("Furniture color must be non-empty.");
        }
        if (furniture.price() < 0) {
            throw new IllegalArgumentException("Furniture price must be non-negative.");
        }
        if (furniture.shelfNbr() < 0) {
            throw new IllegalArgumentException("Shelf number must be non-negative.");
        }
        if (furniture.weight() < 0) {
            throw new IllegalArgumentException("Furniture weight must be non-negative.");
        }
        if (furniture.purchaseDate() == null) {
            throw new IllegalArgumentException("Purchase date must be provided.");
        }
    }

    /**
     * Validates that a furniture item exists.
     *
     * @param furnitureId The ID of the furniture item.
     * @throws IllegalArgumentException if the furniture does not exist.
     */
    private void validateFurnitureExists(long furnitureId) {
        if (!furnitureExists(furnitureId)) {
            throw new IllegalArgumentException("Furniture with ID " + furnitureId + " does not exist.");
        }
    }
}