-- schema.sql
-- Database schema for SecondHandProject
-- Defines tables for employees, customers, furniture, orders, and order lines

-- Drop tables if they exist to ensure a clean setup
DROP TABLE IF EXISTS `order_line`;
DROP TABLE IF EXISTS `order_head`;
DROP TABLE IF EXISTS `furniture`;
DROP TABLE IF EXISTS `customer`;
DROP TABLE IF EXISTS `employee`;

-- Table for employees
CREATE TABLE `employee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `first_name` VARCHAR(255) NOT NULL,
    `last_name` VARCHAR(255) NOT NULL,
    `address` VARCHAR(255),
    `city` VARCHAR(255),
    `postal_code` VARCHAR(20),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table for customers
CREATE TABLE `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `first_name` VARCHAR(255) NOT NULL,
    `last_name` VARCHAR(255) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `birth_date` DATE NOT NULL,
    `city` VARCHAR(255) NOT NULL,
    `postal_code` VARCHAR(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table for furniture items
CREATE TABLE `furniture` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `color` VARCHAR(255) NOT NULL,
    `comment` TEXT,
    `price` DOUBLE NOT NULL,
    `purchase_date` DATE NOT NULL,
    `shelf_nbr` INT NOT NULL,
    `weight` DOUBLE NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table for order headers
CREATE TABLE `order_head` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_date` DATE NOT NULL,
    `customer_id` BIGINT NOT NULL,
    `employee_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`customer_id`) REFERENCES `customer`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`employee_id`) REFERENCES `employee`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table for order lines
CREATE TABLE `order_line` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `furniture_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`order_id`) REFERENCES `order_head`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`furniture_id`) REFERENCES `furniture`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert sample data for employees
INSERT INTO `employee` (`id`, `first_name`, `last_name`, `address`, `city`, `postal_code`) VALUES
(1, 'Erik', 'Johansson', 'Vägen 123', 'Stockholm', '111 22'),
(2, 'Anna', 'Svensson', 'Stigen 456', 'Göteborg', '222 33'),
(3, 'Lars', 'Nilsson', 'Allén 789', 'Malmö', '333 44');

-- Insert sample data for customers
INSERT INTO `customer` (`id`, `first_name`, `last_name`, `address`, `birth_date`, `city`, `postal_code`) VALUES
(3, 'Emma', 'Karlsson', 'Kungsgatan 5', '1975-12-09', 'Örebro', '666 77'),
(7, 'eee', 'eeeeeeeeeee', 'ee', '2025-02-02', 'w', '333'),
(10, 'äö', 'df', 'dd3', '2024-04-04', 'fg', '234'),
(11, 'pp', 'ee', 'ff', '2025-04-04', 'de', '3333'),
(12, 'pp', 'dds', 'ssd', '2020-02-29', 'dd', '222');

-- Insert sample data for furniture
INSERT INTO `furniture` (`id`, `name`, `color`, `comment`, `price`, `purchase_date`, `shelf_nbr`, `weight`) VALUES
(1, 'Stol', 'Blå', 'En klassisk stol', 299.99, '2024-01-01', 1, 7.5),
(2, 'Soffa', 'Vit', 'Bekväm och rymlig', 4999.50, '2024-01-02', 2, 35.2),
(3, 'Bord', 'Brun', 'Ekträ', 1299.99, '2024-01-03', 3, 15.7);

-- Insert sample data for order headers
INSERT INTO `order_head` (`id`, `order_date`, `customer_id`, `employee_id`) VALUES
(4, '2025-01-03', 3, 1),
(6, '2024-03-03', 3, 1),
(10, '2023-04-04', 3, 1),
(11, '2024-04-04', 3, 1);

-- Insert sample data for order lines
INSERT INTO `order_line` (`id`, `order_id`, `furniture_id`, `quantity`) VALUES
(10, 4, 1, 2),
(11, 4, 1, 1),
(12, 6, 1, 2),
(13, 6, 1, 1),
(14, 10, 1, 2),
(15, 10, 1, 1),
(16, 11, 1, 2),
(17, 11, 1, 1);