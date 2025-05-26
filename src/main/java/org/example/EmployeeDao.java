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
 * Data Access Object (DAO) for managing employees in the database.
 * Provides methods to create, retrieve, update, and delete employee records.
 */
public class EmployeeDao {
    private static final Logger logger = Logger.getLogger(EmployeeDao.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{10}");

    public void addEmployee(Employee employee) {
        String sql = "INSERT INTO employee (first_name, last_name, email, phone, hire_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                validateEmployeeFields(employee);
                stmt.setString(1, employee.firstName());
                stmt.setString(2, employee.lastName());
                stmt.setString(3, employee.email());
                stmt.setString(4, employee.phone());
                stmt.setDate(5, Date.valueOf(employee.hireDate()));
                stmt.executeUpdate();
                conn.commit();
                logger.info("Employee added: " + employee);
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Failed to add employee: " + employee, e);
                throw new RuntimeException("Could not add employee: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while adding employee", e);
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, phone, hire_date FROM employee";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                employees.add(new Employee(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDate("hire_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve employees", e);
            throw new RuntimeException("Could not retrieve employees: " + e.getMessage(), e);
        }
        return employees;
    }

    public Employee getEmployeeById(long employeeId) {
        String sql = "SELECT id, first_name, last_name, email, phone, hire_date FROM employee WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                            rs.getLong("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getDate("hire_date").toLocalDate()
                    );
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve employee with ID: " + employeeId, e);
            throw new RuntimeException("Could not retrieve employee: " + e.getMessage(), e);
        }
        return null;
    }

    public void updateEmployee(Employee employee) {
        String sql = "UPDATE employee SET first_name = ?, last_name = ?, email = ?, phone = ?, hire_date = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            validateEmployeeExists(employee.id());
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                validateEmployeeFields(employee);
                stmt.setString(1, employee.firstName());
                stmt.setString(2, employee.lastName());
                stmt.setString(3, employee.email());
                stmt.setString(4, employee.phone());
                stmt.setDate(5, Date.valueOf(employee.hireDate()));
                stmt.setLong(6, employee.id());
                int updated = stmt.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("Expected to update 1 row, updated: " + updated);
                }
                conn.commit();
                logger.info("Employee updated: " + employee);
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Failed to update employee: " + employee, e);
                throw new RuntimeException("Could not update employee: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating employee", e);
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public void deleteEmployee(long employeeId) {
        validateEmployeeExists(employeeId);

        String deleteLines = "DELETE FROM order_line WHERE order_id IN (SELECT id FROM order_head WHERE employee_id = ?)";
        String deleteHeads = "DELETE FROM order_head WHERE employee_id = ?";
        String deleteEmp   = "DELETE FROM employee WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt1 = conn.prepareStatement(deleteLines);
                 PreparedStatement stmt2 = conn.prepareStatement(deleteHeads);
                 PreparedStatement stmt3 = conn.prepareStatement(deleteEmp)) {

                stmt1.setLong(1, employeeId);
                stmt1.executeUpdate();

                stmt2.setLong(1, employeeId);
                stmt2.executeUpdate();

                stmt3.setLong(1, employeeId);
                int deleted = stmt3.executeUpdate();
                if (deleted != 1) {
                    throw new SQLException("Expected to delete 1 employee row, deleted: " + deleted);
                }

                conn.commit();
                logger.info("Employee " + employeeId + " and related orders deleted.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to delete employee with ID " + employeeId, e);
            throw new RuntimeException("Could not delete employee: " + e.getMessage(), e);
        }
    }

    public boolean employeeExists(long employeeId) {
        String sql = "SELECT COUNT(*) FROM employee WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to check employee existence for ID " + employeeId, e);
            throw new RuntimeException("Could not check employee existence: " + e.getMessage(), e);
        }
    }

    private void validateEmployeeFields(Employee e) {
        if (e.firstName().isBlank()
                || e.lastName().isBlank()
                || !EMAIL_PATTERN.matcher(e.email()).matches()
                || !PHONE_PATTERN.matcher(e.phone()).matches()
                || e.hireDate() == null
                || e.hireDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "All fields must be non-empty, email valid, phone 10 digits, hire date not in future.");
        }
    }

    private void validateEmployeeExists(long employeeId) {
        if (!employeeExists(employeeId)) {
            throw new IllegalArgumentException("No employee with ID " + employeeId);
        }
    }
}
