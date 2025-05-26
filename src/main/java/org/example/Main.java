package org.example;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.IOException;

/**
 * Main class Main the SecondHandProject application.
 * Provides a console-based interface for managing customers, furniture, and orders.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerDao customerDao = new CustomerDao();
    private static final OrderDao orderDao = new OrderDao();
    private static final FurnitureDao furnitureDao = new FurnitureDao();

    static {
        try {
            FileHandler fileHandler = new FileHandler("secondhand.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not configure log file", e);
        }
    }

    /**
     * Main entry point for the application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            while (true) {
                showMenu();
                int choice = getUserChoice();
                switch (choice) {
                    case 1 -> addCustomer();
                    case 2 -> showCustomers();
                    case 3 -> updateCustomer();
                    case 4 -> deleteCustomer();
                    case 5 -> createOrder();
                    case 6 -> showOrdersForEmployee();
                    case 7 -> showOrdersWithCustomerNameForEmployee();
                    case 8 -> showOrderDetailsForEmployee();
                    case 9 -> addFurniture();
                    case 10 -> showFurniture();
                    case 11 -> updateFurniture();
                    case 12 -> deleteFurniture();
                    case 13 -> showCustomerById();
                    case 14 -> showFurnitureById();
                    case 15 -> deleteOrder();
                    case 16 -> showOrderValue();
                    case 17 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice! Please try again.");
                }
            }
        } finally {
            scanner.close();
        }
    }

    /**
     * Displays the main menu options.
     */
    private static void showMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1. Add customer");
        System.out.println("2. Show all customers");
        System.out.println("3. Update customer");
        System.out.println("4. Delete customer");
        System.out.println("5. Add order");
        System.out.println("6. Show all orders for an employee");
        System.out.println("7. Show orders with customer name for an employee");
        System.out.println("8. Show detailed orders for an employee");
        System.out.println("9. Add furniture");
        System.out.println("10. Show all furniture");
        System.out.println("11. Update furniture");
        System.out.println("12. Delete furniture");
        System.out.println("13. Show customer by ID");
        System.out.println("14. Show furniture by ID");
        System.out.println("15. Delete order");
        System.out.println("16. Show total order value");
        System.out.println("17. Exit");
    }

    /**
     * Gets a valid menu choice from the user.
     *
     * @return The user's choice as an integer.
     */
    private static int getUserChoice() {
        while (true) {
            System.out.print("Choose an option: ");
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine();
                return choice;
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    /**
     * Adds a new customer to the database.
     */
    private static void addCustomer() {
        try {
            System.out.print("First name: ");
            String first = getValidInput();
            System.out.print("Last name: ");
            String last = getValidInput();
            System.out.print("Address: ");
            String address = getValidInput();
            System.out.print("Birth date (YYYY-MM-DD): ");
            LocalDate birthDate = getValidDateFromUser();
            System.out.print("City: ");
            String city = getValidInput();
            System.out.print("Postal code: ");
            String postalCode = getValidInput();

            Customer customer = new Customer(0, first, last, address, birthDate, city, postalCode);
            customerDao.addCustomer(customer);
        } catch (Exception e) {
            System.out.println("Error adding customer: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in addCustomer", e);
        }
    }

    /**
     * Gets a non-empty string input from the user.
     *
     * @return The validated input string.
     */
    private static String getValidInput() {
        String input;
        while (true) {
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Error: This field cannot be empty. Please try again.");
        }
    }

    /**
     * Displays all customers in the database.
     */
    private static void showCustomers() {
        try {
            List<Customer> customers = customerDao.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("No customers found.");
            } else {
                System.out.println("\nAll customers:");
                for (Customer c : customers) {
                    System.out.println(c);
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving customers: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in showCustomers", e);
        }
    }

    /**
     * Updates an existing customer in the database.
     */
    private static void updateCustomer() {
        try {
            List<Customer> customers = customerDao.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("No customers available to update.");
                return;
            }
            System.out.println("Select a customer to update:");
            for (int i = 0; i < customers.size(); i++) {
                System.out.println((i + 1) + ". " + customers.get(i).firstName() + " " + customers.get(i).lastName());
            }
            int choice = getValidChoice(customers.size());
            Customer existing = customers.get(choice - 1);

            System.out.print("New first name: ");
            String first = getValidInput();
            System.out.print("New last name: ");
            String last = getValidInput();
            System.out.print("New address: ");
            String address = getValidInput();
            System.out.print("New birth date (YYYY-MM-DD): ");
            LocalDate birthDate = getValidDateFromUser();
            System.out.print("New city: ");
            String city = getValidInput();
            System.out.print("New postal code: ");
            String postalCode = getValidInput();

            Customer updatedCustomer = new Customer(existing.id(), first, last, address, birthDate, city, postalCode);
            customerDao.updateCustomer(updatedCustomer);
        } catch (Exception e) {
            System.out.println("Error updating customer: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in updateCustomer", e);
        }
    }

    /**
     * Deletes a customer from the database.
     */
    private static void deleteCustomer() {
        try {
            List<Customer> customers = customerDao.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("No customers available to delete.");
                return;
            }
            System.out.println("Select a customer to delete:");
            for (int i = 0; i < customers.size(); i++) {
                System.out.println((i + 1) + ". " + customers.get(i).firstName() + " " + customers.get(i).lastName());
            }
            int choice = getValidChoice(customers.size());
            customerDao.deleteCustomer(customers.get(choice - 1).id());
        } catch (Exception e) {
            System.out.println("Error deleting customer: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in deleteCustomer", e);
        }
    }

    /**
     * Creates a new order with associated order lines.
     */
    private static void createOrder() {
        try {
            LocalDate orderDate = getValidDateFromUser();
            int customerId = getCustomerIdFromUser();
            System.out.print("Enter employee ID: ");
            int employeeId = getValidPositiveInt();

            if (customerId == 0) {
                System.out.println("Invalid customer ID.");
                return;
            }

            List<Furniture> furnitureList = furnitureDao.getAllFurniture();
            if (furnitureList.isEmpty()) {
                System.out.println("No furniture available in the database.");
                return;
            }

            System.out.println("Select furniture for the order (enter 0 to finish):");
            List<OrderLine> orderLines = new ArrayList<>();
            while (true) {
                System.out.println("\nAvailable furniture:");
                for (int i = 0; i < furnitureList.size(); i++) {
                    Furniture f = furnitureList.get(i);
                    System.out.println((i + 1) + ". " + f.name() + " (ID: " + f.id() + ", Price: " + f.price() + ")");
                }
                System.out.print("Select furniture number (0 to finish): ");
                int choice = getValidChoice(furnitureList.size() + 1);
                if (choice == 0) break;

                Furniture selectedFurniture = furnitureDao.getFurnitureById(furnitureList.get(choice - 1).id());
                if (selectedFurniture == null) {
                    System.out.println("Invalid furniture ID.");
                    continue;
                }

                System.out.print("Enter quantity: ");
                int quantity = getValidPositiveInt();
                orderLines.add(new OrderLine(0, 0, selectedFurniture.id(), quantity));
            }

            if (orderLines.isEmpty()) {
                System.out.println("No order lines selected. Order cancelled.");
                return;
            }

            System.out.println("\nSelected order lines:");
            for (OrderLine line : orderLines) {
                Furniture f = furnitureDao.getFurnitureById(line.furnitureId());
                System.out.println("Furniture: " + (f != null ? f.name() : "Unknown") + ", Quantity: " + line.quantity());
            }
            System.out.print("Confirm order (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes")) {
                System.out.println("Order cancelled.");
                return;
            }

            OrderHead order = new OrderHead(0, orderDate, customerId, employeeId);
            orderDao.createOrder(order, orderLines);
        } catch (Exception e) {
            System.out.println("Error creating order: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in createOrder", e);
        }
    }

    /**
     * Gets a valid date from the user in YYYY-MM-DD format.
     *
     * @return The parsed {@link LocalDate}.
     */
    private static LocalDate getValidDateFromUser() {
        while (true) {
            System.out.print("Enter date (YYYY-MM-DD): ");
            String dateString = scanner.nextLine().trim();
            try {
                return LocalDate.parse(dateString);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    /**
     * Gets a valid customer ID by letting the user select from a list.
     *
     * @return The selected customer ID, or 0 if none available.
     */
    private static int getCustomerIdFromUser() {
        try {
            List<Customer> customers = customerDao.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("No customers available.");
                return 0;
            }
            System.out.println("Select a customer:");
            for (int i = 0; i < customers.size(); i++) {
                System.out.println((i + 1) + ". " + customers.get(i).firstName() + " " + customers.get(i).lastName());
            }
            int choice = getValidChoice(customers.size());
            Customer selectedCustomer = customerDao.getCustomerById(customers.get(choice - 1).id());
            if (selectedCustomer == null) {
                System.out.println("Invalid customer ID.");
                return 0;
            }
            return (int) selectedCustomer.id();
        } catch (Exception e) {
            System.out.println("Error selecting customer: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in getCustomerIdFromUser", e);
            return 0;
        }
    }

    /**
     * Gets a valid choice within the specified range.
     *
     * @param maxChoice The maximum valid choice.
     * @return The validated choice.
     */
    private static int getValidChoice(int maxChoice) {
        while (true) {
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice >= 0 && choice <= maxChoice) {
                    return choice;
                }
                System.out.println("Invalid choice. Please select a number between 0 and " + maxChoice + ".");
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    /**
     * Gets a positive integer from the user.
     *
     * @return The validated positive integer.
     */
    private static int getValidPositiveInt() {
        while (true) {
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be greater than 0. Please try again.");
            } else {
                System.out.println("Invalid input. Please enter a positive number.");
                scanner.nextLine();
            }
        }
    }

    /**
     * Displays all orders for a specific employee.
     */
    private static void showOrdersForEmployee() {
        try {
            System.out.print("Enter employee ID: ");
            int employeeId = getValidPositiveInt();
            List<OrderHead> orders = orderDao.listOrdersForEmployee(employeeId);
            if (orders.isEmpty()) {
                System.out.println("No orders found for this employee.");
            } else {
                System.out.println("Orders for employee ID " + employeeId + ":");
                for (OrderHead order : orders) {
                    System.out.println(order);
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving orders: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in showOrdersForEmployee", e);
        }
    }

    /**
     * Displays orders with customer names for a specific employee.
     */
    private static void showOrdersWithCustomerNameForEmployee() {
        try {
            System.out.print("Enter employee ID: ");
            int employeeId = getValidPositiveInt();
            List<OrderDetail> orderDetails = orderDao.listOrdersWithCustomerNameForEmployee(employeeId);
            if (orderDetails.isEmpty()) {
                System.out.println("No orders with customer names found for this employee.");
            } else {
                System.out.println("Orders with customer names for employee ID " + employeeId + ":");
                for (OrderDetail detail : orderDetails) {
                    System.out.println(detail);
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving orders: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in showOrdersWithCustomerNameForEmployee", e);
        }
    }

    /**
     * Displays detailed orders for a specific employee.
     */
    private static void showOrderDetailsForEmployee() {
        try {
            System.out.print("Enter employee ID: ");
            int employeeId = getValidPositiveInt();
            List<OrderDetail> orderDetails = orderDao.listOrdersWithDetailsForEmployee(employeeId);
            if (orderDetails.isEmpty()) {
                System.out.println("No detailed orders found for this employee.");
            } else {
                System.out.println("Detailed orders for employee ID " + employeeId + ":");
                for (OrderDetail detail : orderDetails) {
                    System.out.println(detail);
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving detailed orders: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in showOrderDetailsForEmployee", e);
        }
    }

    /**
     * Adds a new furniture item to the database.
     */
    private static void addFurniture() {
        try {
            System.out.print("Name: ");
            String name = getValidInput();
            System.out.print("Color: ");
            String color = getValidInput();
            System.out.print("Comment: ");
            String comment = scanner.nextLine().trim();
            System.out.print("Price: ");
            double price = getValidPositiveDouble();
            System.out.print("Purchase date (YYYY-MM-DD): ");
            LocalDate purchaseDate = getValidDateFromUser();
            System.out.print("Shelf number: ");
            int shelfNbr = getValidPositiveInt();
            System.out.print("Weight: ");
            double weight = getValidPositiveDouble();

            Furniture furniture = new Furniture(0, name, color, comment, price, purchaseDate, shelfNbr, weight);
            furnitureDao.addFurniture(furniture);
        } catch (Exception e) {
            System.out.println("Error adding furniture: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in addFurniture", e);
        }
    }

    /**
     * Gets a positive double from the user.
     *
     * @return The validated positive double.
     */
    private static double getValidPositiveDouble() {
        while (true) {
            if (scanner.hasNextDouble()) {
                double value = scanner.nextDouble();
                scanner.nextLine();
                if (value >= 0) {
                    return value;
                }
                System.out.println("Value must be non-negative. Please try again.");
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    /**
     * Displays all furniture items in the database.
     */
    private static void showFurniture() {
        try {
            List<Furniture> furnitureList = furnitureDao.getAllFurniture();
            if (furnitureList.isEmpty()) {
                System.out.println("No furniture found.");
            } else {
                System.out.println("\nAll furniture:");
                for (Furniture f : furnitureList) {
                    System.out.println(f);
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving furniture: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in showFurniture", e);
        }
    }

    /**
     * Updates an existing furniture item in the database.
     */
    private static void updateFurniture() {
        try {
            List<Furniture> furnitureList = furnitureDao.getAllFurniture();
            if (furnitureList.isEmpty()) {
                System.out.println("No furniture available to update.");
                return;
            }
            System.out.println("Select a furniture to update:");
            for (int i = 0; i < furnitureList.size(); i++) {
                System.out.println((i + 1) + ". " + furnitureList.get(i).name());
            }
            int choice = getValidChoice(furnitureList.size());
            Furniture existing = furnitureDao.getFurnitureById(furnitureList.get(choice - 1).id());
            if (existing == null) {
                System.out.println("Invalid furniture ID.");
                return;
            }

            System.out.print("New name: ");
            String name = getValidInput();
            System.out.print("New color: ");
            String color = getValidInput();
            System.out.print("New comment: ");
            String comment = scanner.nextLine().trim();
            System.out.print("New price: ");
            double price = getValidPositiveDouble();
            System.out.print("New purchase date (YYYY-MM-DD): ");
            LocalDate purchaseDate = getValidDateFromUser();
            System.out.print("New shelf number: ");
            int shelfNbr = getValidPositiveInt();
            System.out.print("New weight: ");
            double weight = getValidPositiveDouble();

            Furniture updatedFurniture = new Furniture(existing.id(), name, color, comment, price, purchaseDate, shelfNbr, weight);
            furnitureDao.updateFurniture(updatedFurniture);
        } catch (Exception e) {
            System.out.println("Error updating furniture: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in updateFurniture", e);
        }
    }

    /**
     * Deletes a furniture item from the database.
     */
    private static void deleteFurniture() {
        try {
            List<Furniture> furnitureList = furnitureDao.getAllFurniture();
            if (furnitureList.isEmpty()) {
                System.out.println("No furniture available to delete.");
                return;
            }
            System.out.println("Select a furniture to delete:");
            for (int i = 0; i < furnitureList.size(); i++) {
                System.out.println((i + 1) + ". " + furnitureList.get(i).name());
            }
            int choice = getValidChoice(furnitureList.size());
            Furniture selected = furnitureDao.getFurnitureById(furnitureList.get(choice - 1).id());
            if (selected == null) {
                System.out.println("Invalid furniture ID.");
                return;
            }
            furnitureDao.deleteFurniture(selected.id());
        } catch (Exception e) {
            System.out.println("Error deleting furniture: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in deleteFurniture", e);
        }
    }

    /**
     * Displays a customer by their ID.
     */
    private static void showCustomerById() {
        try {
            System.out.print("Enter customer ID: ");
            int id = getValidPositiveInt();
            Customer customer = customerDao.getCustomerById(id);
            if (customer != null) {
                System.out.println(customer);
            } else {
                System.out.println("Customer with ID " + id + " not found.");
            }
        } catch (Exception e) {
            System.out.println("Error retrieving customer: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in showCustomerById", e);
        }
    }

    /**
     * Displays a furniture item by its ID.
     */
    private static void showFurnitureById() {
        try {
            System.out.print("Enter furniture ID: ");
            int id = getValidPositiveInt();
            Furniture furniture = furnitureDao.getFurnitureById(id);
            if (furniture != null) {
                System.out.println(furniture);
            } else {
                System.out.println("Furniture with ID " + id + " not found.");
            }
        } catch (Exception e) {
            System.out.println("Error retrieving furniture: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in showFurnitureById", e);
        }
    }

    /**
     * Deletes an order from the database.
     */
    private static void deleteOrder() {
        try {
            System.out.print("Enter order ID to delete: ");
            int orderId = getValidPositiveInt();
            orderDao.deleteOrder(orderId);
        } catch (Exception e) {
            System.out.println("Error deleting order: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in deleteOrder", e);
        }
    }

    /**
     * Displays the total value of an order.
     */
    private static void showOrderValue() {
        try {
            System.out.print("Enter order ID: ");
            int orderId = getValidPositiveInt();
            double total = orderDao.getTotalOrderValue(orderId);
            System.out.printf("Total order value for order %d: %.2f%n", orderId, total);
        } catch (Exception e) {
            System.out.println("Error retrieving order value: " + e.getMessage());
            logger.log(Level.SEVERE, "Error in showOrderValue", e);
        }
    }
}