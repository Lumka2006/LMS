# Learning Management System

A desktop-based Learning Management System built with JavaFX and PostgreSQL.

## Features

- Modern JavaFX user interface
- PostgreSQL database integration
- Course progress tracking
- Pagination and scrolling content
- Animated UI elements
- Menu system

## Prerequisites

- Java 17 or higher
- Maven
- PostgreSQL 12 or higher

## Database Setup

1. Create a new PostgreSQL database named `lms_db`:
```sql
CREATE DATABASE lms_db;
```

2. Update the database connection settings in `src/main/java/com/lms/util/DatabaseUtil.java` if needed:
```java
private static final String URL = "jdbc:postgresql://localhost:5432/lms_db";
private static final String USER = "postgres";
private static final String PASSWORD = "postgres";
```

## Building and Running

1. Clone the repository:
```bash
git clone <repository-url>
cd learning-management-system
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn javafx:run
```

## Project Structure

- `src/main/java/com/lms/Main.java` - Application entry point
- `src/main/java/com/lms/controller/MainController.java` - Main UI controller
- `src/main/java/com/lms/util/DatabaseUtil.java` - Database connection utility
- `src/main/resources/fxml/main.fxml` - Main UI layout

## Technologies Used

- JavaFX 17
- PostgreSQL
- JDBC
- Maven

## License

This project is licensed under the MIT License - see the LICENSE file for details. 