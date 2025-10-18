# Contributing to SecondHandProject

Thank you for your interest in contributing to SecondHandProject! 🎉

## How to Contribute

### 1. Fork the Repository
Click the "Fork" button at the top right of the repository page.

### 2. Clone Your Fork
```bash
git clone https://github.com/YOUR-USERNAME/SecondHandProject.git
cd SecondHandProject
```

### 3. Create a Branch
```bash
git checkout -b feature/your-feature-name
```

### 4. Make Your Changes
- Follow the existing code style
- Write clear, concise commit messages
- Add comments to complex code sections
- Test your changes thoroughly

### 5. Commit Your Changes
```bash
git add .
git commit -m "Add: brief description of your changes"
```

### 6. Push to Your Fork
```bash
git push origin feature/your-feature-name
```

### 7. Open a Pull Request
Go to the original repository and click "New Pull Request"

## Code Style Guidelines

### Java Code
- Use meaningful variable and method names
- Follow JavaDoc conventions for documentation
- Keep methods short and focused (single responsibility)
- Use proper exception handling

### Example:
```java
/**
 * Adds a new customer to the database.
 * 
 * @param customer The customer object to add
 * @return true if successful, false otherwise
 * @throws SQLException if database connection fails
 */
public boolean addCustomer(Customer customer) throws SQLException {
    // Implementation
}
```

### Database
- Always use prepared statements to prevent SQL injection
- Close database connections in finally blocks or use try-with-resources

### UI (JavaFX)
- Keep FXML and controller logic separate
- Use CSS for styling instead of inline styles
- Provide user feedback for all actions (success/error messages)

## Testing
- Add unit tests for new features
- Ensure all existing tests pass before submitting
- Test edge cases and error conditions

## Reporting Bugs
When reporting bugs, please include:
- Description of the issue
- Steps to reproduce
- Expected behavior
- Actual behavior
- Screenshots (if applicable)
- Your environment (OS, Java version, MySQL version)

## Feature Requests
We welcome feature requests! Please:
- Check if the feature already exists
- Clearly describe the feature and its benefits
- Provide examples or mockups if possible

## Questions?
Feel free to open an issue or contact: **omaralhaek97@gmail.com**

Thank you for contributing! 🚀