SecondHandProject
A JavaFX-based application for managing a second-hand furniture store. The application provides a graphical user interface (GUI) for handling customer management, furniture inventory, order processing, and employee order tracking. It features robust error handling, input validation, and database integration with MySQL.
Features

Customer Management: Add, update, delete, and view customers with details like first name, last name, email, and phone.
Furniture Inventory: Add, update, delete, and view furniture items, including name, color, comment, price, purchase date, shelf number, and weight.
Order Processing: Create, delete, and view orders with order lines, including furniture items and quantities.
Employee Order Tracking: View orders by employee, including customer names, detailed order information, and total order value.
Search and Filter: Search customers, furniture, or orders by ID, and filter lists by relevant fields (e.g., name, color, customer ID).
Graphical User Interface: Intuitive JavaFX GUI with styled components, tooltips, and real-time input validation.
Database Integration: Persistent storage using MySQL, configured via db.properties.
Logging: File-based logging for debugging and error tracking.
Error Handling: Robust input validation and user-friendly error messages in the GUI.

Project Structure
SecondHandProject/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/
│   │   │       ├── App.java
│   │   │       ├── CustomerController.java
│   │   │       ├── EmployeeController.java
│   │   │       ├── FurnitureController.java
│   │   │       ├── OrderController.java
│   │   │       ├── OrderDetail.java
│   │   │       ├── OrderHead.java
│   │   │       ├── OrderLine.java
│   │   │       ├── Customer.java
│   │   │       ├── Employee.java
│   │   │       ├── Furniture.java
│   │   │       ├── CustomerDao.java
│   │   │       ├── EmployeeDao.java
│   │   │       ├── FurnitureDao.java
│   │   │       ├── OrderDao.java
│   │   │       └── DatabaseConfig.java
│   │   ├── resources/
│   │   │   ├── org/example/
│   │   │   │   ├── main.fxml
│   │   │   │   ├── customer.fxml
│   │   │   │   ├── employee.fxml
│   │   │   │   ├── furniture.fxml
│   │   │   │   ├── order.fxml
│   │   │   │   └── style.css
│   │   │   ├── schema.sql
│   │   │   └── db.properties
├── pom.xml
├── README.md
└── LICENSE

Setup
Follow these steps to set up and run the project:

Clone the Repository:
git clone https://github.com/<your-username>/SecondHandProject.git
cd SecondHandProject


Set Up MySQL Database:

Install MySQL and create a database (e.g., furniture_db).
Run the SQL script located at src/main/resources/schema.sql to create the necessary tables (customers, employees, furniture, orders, order_lines).
Example:mysql -u <your_username> -p furniture_db < src/main/resources/schema.sql




Configure db.properties:

Locate the db.properties file in src/main/resources.
Update it with your MySQL database credentials. Example:db.url=jdbc:mysql://localhost:3306/furniture_db
db.username=<your_username>
db.password=<your_password>
db.driver=com.mysql.cj.jdbc.Driver


Ensure db.properties is not committed to version control if it contains sensitive information. Add it to .gitignore.


Build the Project:

Ensure you have Java 23 and Maven installed.
Build the project using Maven:mvn clean install




Run the Application:

Execute the JavaFX application:java -jar target/SecondHandProject-1.0-SNAPSHOT.jar


Alternatively, run directly from Maven:mvn javafx:run





Dependencies
The project uses the following dependencies (defined in pom.xml):

JavaFX (version 23):
javafx-controls
javafx-fxml


MySQL Connector/J (version 9.3.0)
Maven for build management
Java 23 runtime

Example pom.xml dependencies snippet:
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>23</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>23</version>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>9.3.0</version>
    </dependency>
</dependencies>

Usage

Launch the Application: Run the application to open the JavaFX GUI.
Navigate Tabs: Use the tabbed interface (main.fxml) to access:
Home: Overview or dashboard (if implemented).
Customers: Manage customer records.
Employees: Manage employee records.
Furniture: Manage furniture inventory.
Orders: Create and manage orders with order lines.


Interact with Features:
Add, update, or delete records using the provided buttons.
Use filter fields to search for specific records.
Search by ID using dialog-based inputs.
View detailed order information, including customer names and total order value.



Notes

Database: Ensure the MySQL server is running before launching the application.
JavaFX: Requires JavaFX SDK and proper configuration in pom.xml (use javafx-maven-plugin for running).
Logging: Logs are written to a file for debugging (configure logging in your DAO classes if needed).
Security: Avoid storing sensitive database credentials in db.properties in production; consider environment variables or a secure vault.

License
This project is licensed under the MIT License. See the LICENSE file for details.
Contributing
Contributions are welcome! Please fork the repository, create a feature branch, and submit a pull request. Ensure code follows the existing style and includes tests where applicable.
Contact
For questions or issues, please open an issue on GitHub or contact [omaralhaek97@gmail.com].
