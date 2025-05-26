package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) för att hantera ordrar i databasen.
 */
public class OrderDao {
    private static final Logger logger = Logger.getLogger(OrderDao.class.getName());

    /**
     * Hämta *alla* ordrar oavsett anställd.
     */
    public List<OrderHead> getAllOrders() {
        List<OrderHead> all = new ArrayList<>();
        String sql = """
            SELECT id,
                   order_date,
                   customer_id,
                   employee_id
              FROM order_head
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                all.add(new OrderHead(
                        rs.getLong("id"),
                        rs.getDate("order_date").toLocalDate(),
                        rs.getLong("customer_id"),
                        rs.getLong("employee_id")
                ));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Kunde inte hämta alla ordrar", e);
            throw new RuntimeException("Could not fetch all orders: " + e.getMessage(), e);
        }
        return all;
    }

    /**
     * Hämta alla ordrar för en specifik anställd.
     */
    public List<OrderHead> listOrdersForEmployee(long employeeId) {
        List<OrderHead> orders = new ArrayList<>();
        String sql = "SELECT id, order_date, customer_id, employee_id FROM order_head WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            validateEmployee(employeeId);
            stmt.setLong(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new OrderHead(
                            rs.getLong("id"),
                            rs.getDate("order_date").toLocalDate(),
                            rs.getLong("customer_id"),
                            rs.getLong("employee_id")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve orders for employee ID: {0}", employeeId);
            throw new RuntimeException("Could not retrieve orders: " + e.getMessage(), e);
        }
        return orders;
    }

    /**
     * Creates a new order with associated order lines.
     *
     * @param order The {@link OrderHead} object containing order details.
     * @param orderLines A list of {@link OrderLine} objects representing the items in the order.
     * @throws IllegalArgumentException if customer, employee, or furniture is invalid.
     * @throws RuntimeException if a database error occurs.
     */
    public void createOrder(OrderHead order, List<OrderLine> orderLines) {
        String orderSql = "INSERT INTO order_head (order_date, customer_id, employee_id) VALUES (?, ?, ?)";
        String lineSql = "INSERT INTO order_line (furniture_id, order_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                validateCustomer(order.customerId());
                validateEmployee(order.employeeId());
                if (orderLines.isEmpty()) {
                    throw new IllegalArgumentException("Order must contain at least one order line.");
                }

                // Insert OrderHead
                long orderId;
                try (PreparedStatement stmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setDate(1, Date.valueOf(order.orderDate()));
                    stmt.setLong(2, order.customerId());
                    stmt.setLong(3, order.employeeId());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            orderId = rs.getLong(1);
                        } else {
                            throw new SQLException("Failed to retrieve generated order ID.");
                        }
                    }
                }

                // Insert OrderLines
                try (PreparedStatement lineStmt = conn.prepareStatement(lineSql)) {
                    for (OrderLine line : orderLines) {
                        validateFurniture(line.furnitureId());
                        if (line.quantity() <= 0) {
                            throw new IllegalArgumentException("Quantity must be greater than 0 for furniture ID: " + line.furnitureId());
                        }
                        lineStmt.setLong(1, line.furnitureId()); // Antagande: long furnitureId
                        lineStmt.setLong(2, orderId);
                        lineStmt.setInt(3, line.quantity());
                        lineStmt.executeUpdate();
                    }
                }
                conn.commit();
                System.out.println("Order created with ID: " + orderId);
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Failed to create order: {0}", order);
                throw new RuntimeException("Could not create order: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while creating order", e);
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an order and its associated order lines.
     *
     * @param orderId The ID of the order to delete.
     * @throws IllegalArgumentException if the order does not exist.
     * @throws RuntimeException if a database error occurs.
     */
    public void deleteOrder(long orderId) {
        String deleteLinesSql = "DELETE FROM order_line WHERE order_id = ?";
        String deleteOrderSql = "DELETE FROM order_head WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!orderExists(orderId)) {
                    throw new IllegalArgumentException("Order with ID " + orderId + " does not exist.");
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteLinesSql)) {
                    stmt.setLong(1, orderId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteOrderSql)) {
                    stmt.setLong(1, orderId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("No order was deleted, check ID: " + orderId);
                    }
                }
                conn.commit();
                System.out.println("Order with ID " + orderId + " deleted successfully!");
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Failed to delete order with ID: {0}", orderId);
                throw new RuntimeException("Could not delete order: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while deleting order", e);
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates the total value of an order based on furniture prices and quantities.
     *
     * @param orderId The ID of the order.
     * @return The total value of the order.
     * @throws IllegalArgumentException if the order does not exist.
     * @throws RuntimeException if a database error occurs.
     */
    public double getTotalOrderValue(long orderId) {
        String sql = "SELECT SUM(f.price * ol.quantity) AS total " +
                "FROM order_line ol JOIN furniture f ON ol.furniture_id = f.id " +
                "WHERE ol.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (!orderExists(orderId)) {
                throw new IllegalArgumentException("Order with ID " + orderId + " does not exist.");
            }
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to calculate order value for order ID: {0}", orderId);
            throw new RuntimeException("Could not calculate order value: " + e.getMessage(), e);
        }
        return 0.0;
    }

    /**
     * Checks if a customer exists in the database.
     *
     * @param customerId The ID of the customer.
     * @return true if the customer exists, false otherwise.
     * @throws RuntimeException if a database error occurs.
     */
    public boolean customerExists(long customerId) {
        return exists("customer", customerId);
    }

    /**
     * Checks if an employee exists in the database.
     *
     * @param employeeId The ID of the employee.
     * @return true if the employee exists, false otherwise.
     * @throws RuntimeException if a database error occurs.
     */
    private boolean employeeExists(long employeeId) {
        return exists("employee", employeeId);
    }

    /**
     * Checks if a furniture item exists in the database.
     *
     * @param furnitureId The ID of the furniture.
     * @return true if the furniture exists, false otherwise.
     * @throws RuntimeException if a database error occurs.
     */
    private boolean furnitureExists(long furnitureId) {
        return exists("furniture", furnitureId);
    }

    /**
     * Checks if an order exists in the database.
     *
     * @param orderId The ID of the order.
     * @return true if the order exists, false otherwise.
     * @throws RuntimeException if a database error occurs.
     */
    private boolean orderExists(long orderId) {
        return exists("order_head", orderId);
    }

    /**
     * Generic method to check if an entity exists in the specified table.
     *
     * @param tableName The name of the table (e.g., customer, employee, furniture, order_head).
     * @param id The ID of the entity.
     * @return true if the entity exists, false otherwise.
     * @throws RuntimeException if a database error occurs.
     */
    private boolean exists(String tableName, long id) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to check existence in table {0} for ID: {1}", new Object[]{tableName, id});
            throw new RuntimeException("Could not check existence in " + tableName + ": " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Validates that a customer exists.
     *
     * @param customerId The ID of the customer.
     * @throws IllegalArgumentException if the customer does not exist.
     */
    private void validateCustomer(long customerId) {
        if (!customerExists(customerId)) {
            throw new IllegalArgumentException("Customer with ID " + customerId + " does not exist.");
        }
    }

    /**
     * Validates that an employee exists.
     *
     * @param employeeId The ID of the employee.
     * @throws IllegalArgumentException if the employee does not exist.
     */
    private void validateEmployee(long employeeId) {
        if (!employeeExists(employeeId)) {
            throw new IllegalArgumentException("Employee with ID " + employeeId + " does not exist.");
        }
    }

    /**
     * Validates that a furniture item exists.
     *
     * @param furnitureId The ID of the furniture.
     * @throws IllegalArgumentException if the furniture does not exist.
     */
    private void validateFurniture(long furnitureId) {
        if (!furnitureExists(furnitureId)) {
            throw new IllegalArgumentException("Furniture with ID " + furnitureId + " does not exist.");
        }
    }

    /**
     * Retrieves orders with customer names and furniture details for a specific employee.
     *
     * @param employeeId The ID of the employee.
     * @return A list of {@link OrderDetail} objects with customer and furniture information.
     * @throws IllegalArgumentException if the employee does not exist.
     * @throws RuntimeException if a database error occurs.
     */
    public List<OrderDetail> listOrdersWithCustomerNameForEmployee(long employeeId) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        String sql = "SELECT o.id AS order_id, o.order_date, c.first_name AS customer_first_name, c.last_name AS customer_last_name, " +
                "ol.quantity, f.name AS furniture_name, f.price " +
                "FROM order_head o " +
                "JOIN customer c ON o.customer_id = c.id " +
                "LEFT JOIN order_line ol ON o.id = ol.order_id " +
                "LEFT JOIN furniture f ON ol.furniture_id = f.id " +
                "WHERE o.employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            validateEmployee(employeeId);
            stmt.setLong(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orderDetails.add(new OrderDetail(
                            rs.getLong("order_id"), // Antagande: long orderId
                            rs.getDate("order_date").toLocalDate(),
                            rs.getString("customer_first_name"),
                            rs.getString("customer_last_name"),
                            rs.getString("furniture_name") != null ? rs.getString("furniture_name") : "No furniture",
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve orders with customer names for employee ID: {0}", employeeId);
            throw new RuntimeException("Could not retrieve orders with customer names: " + e.getMessage(), e);
        }
        return orderDetails;
    }

    /**
     * Retrieves detailed order information for a specific employee.
     *
     * @param employeeId The ID of the employee.
     * @return A list of {@link OrderDetail} objects with detailed order information.
     * @throws IllegalArgumentException if the employee does not exist.
     * @throws RuntimeException if a database error occurs.
     */
    public List<OrderDetail> listOrdersWithDetailsForEmployee(long employeeId) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        String sql = "SELECT o.id AS order_id, o.order_date, c.first_name AS customer_first_name, c.last_name AS customer_last_name, " +
                "ol.quantity, f.name AS furniture_name, f.price " +
                "FROM order_head o " +
                "JOIN customer c ON o.customer_id = c.id " +
                "JOIN order_line ol ON o.id = ol.order_id " +
                "JOIN furniture f ON ol.furniture_id = f.id " +
                "WHERE o.employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            validateEmployee(employeeId);
            stmt.setLong(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orderDetails.add(new OrderDetail(
                            rs.getLong("order_id"), // Antagande: long orderId
                            rs.getDate("order_date").toLocalDate(),
                            rs.getString("customer_first_name"),
                            rs.getString("customer_last_name"),
                            rs.getString("furniture_name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve detailed order information for employee ID: {0}", employeeId);
            throw new RuntimeException("Could not retrieve detailed orders: " + e.getMessage(), e);
        }
        return orderDetails;
    }
}