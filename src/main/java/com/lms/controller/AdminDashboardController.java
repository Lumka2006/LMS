package com.lms.controller;

// Importing JavaFX classes for creating the GUI and UI controls
import javafx.fxml.FXML;  // Annotation to connect FXML file with Java code (for controllers).
import javafx.scene.control.*;  // Importing common JavaFX UI controls such as Button, Label, TextField, ComboBox, etc.
import javafx.scene.layout.VBox;  // A container layout that arranges its children vertically (often used for forms).
import javafx.geometry.Insets;  // Defines padding around UI elements (e.g., for spacing).
import javafx.scene.control.cell.PropertyValueFactory;  // Used to map data to the columns in a TableView (e.g., setting the value of a column).
import javafx.scene.control.Alert.AlertType;  // Enum for defining the type of alert to be shown (information, error, warning, etc.).

// Importing model and utility classes for the LMS system
import com.lms.model.User;  // Represents the User entity in the Learning Management System (LMS).
import com.lms.util.DatabaseUtil;  // Utility class that simplifies database connections and queries (e.g., for opening connections).

// Importing classes for working with the database
import java.sql.Connection;  // Represents a connection to the database.
import java.sql.PreparedStatement;  // Used to execute precompiled SQL queries (prepared statements) in the database.
import java.sql.ResultSet;  // Represents the result of a database query (a table of data).
import java.sql.SQLException;  // Represents an exception thrown when there is an issue with database interaction (e.g., connection or query error).

import javafx.beans.property.SimpleStringProperty;  // A simple string property that can be bound to JavaFX controls (used for TableView).
import javafx.collections.FXCollections;  // Provides utility methods to create ObservableLists for dynamic data binding in JavaFX.
import javafx.collections.ObservableList;  // Represents a list that can be observed and automatically updated in the UI (useful for TableView, ComboBox, etc.).

// Importing JavaFX for handling concurrency and updating the UI on the application thread
import javafx.application.Platform;  // Ensures UI updates are done on the JavaFX Application Thread (used when performing tasks in background threads).


public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    
    @FXML private TextField userSearchField;
    @FXML private Pagination usersPagination;
    @FXML private ProgressIndicator loadingIndicator;
    
    private ObservableList<User> users = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 10;
    private User currentUser;

    @FXML
    public void initialize() {
        // Get the current user from LoginController
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> showError("Error", "User session not initialized. Please log in again."));
            return;
        }
        
        // Show loading indicator
        loadingIndicator.setVisible(true);
        
        // Load data in background
        new Thread(() -> {
            try {
        loadUsers();
            } finally {
                // Hide loading indicator on JavaFX thread
                Platform.runLater(() -> loadingIndicator.setVisible(false));
            }
        }).start();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFirstName() + " " + user.getLastName());
        }
        initializeDashboard();
    }

    private void initializeDashboard() {
        setupTabs();
        // Show loading indicator
        loadingIndicator.setVisible(true);
        
        // Load data in background
        new Thread(() -> {
            try {
        loadUsers();
            } finally {
                // Hide loading indicator on JavaFX thread
                Platform.runLater(() -> loadingIndicator.setVisible(false));
            }
        }).start();
    }

    private void setupTabs() {
        // Users Tab
        Tab usersTab = new Tab("Users");
        VBox usersContent = new VBox(10);
        usersContent.setPadding(new Insets(10));
        
        // Setup users table columns
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActive() ? "ACTIVE" : "INACTIVE")
        );
        
        usersTable.setItems(users);
        
        Button addUserBtn = new Button("Add User");
        addUserBtn.setOnAction(e -> showAddUserDialog());
        
        usersContent.getChildren().addAll(usersTable, addUserBtn);
        usersTab.setContent(usersContent);

        mainTabPane.getTabs().add(usersTab);
    }

    @FXML
    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Enter User Details");

        TextField usernameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        ComboBox<String> roleComboBox = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "TEACHER", "STUDENT"));

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("First Name:"), firstNameField,
            new Label("Last Name:"), lastNameField,
            new Label("Email:"), emailField,
            new Label("Password:"), passwordField,
            new Label("Role:"), roleComboBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    DatabaseUtil.addUser(
                        usernameField.getText(),
                        passwordField.getText(),
                        emailField.getText(),
                        firstNameField.getText(),
                        lastNameField.getText(),
                        roleComboBox.getValue()
                    );
                    loadUsers(); // Reload users after adding
                    return null;
                } catch (SQLException e) {
                    showError("Database Error", "Failed to add user: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void loadUsers() {
        if (currentUser == null) {
            showError("Error", "User session not initialized. Please log in again.");
            return;
        }

        // Show loading indicator
        Platform.runLater(() -> loadingIndicator.setVisible(true));

        try {
            String query = "SELECT * FROM users ORDER BY user_id";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
                users.clear();
            while (rs.next()) {
                    User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("role")
                );
                    user.setActive(rs.getString("status").equals("ACTIVE"));
                    users.add(user);
                }
                
                // Update table on JavaFX thread
                Platform.runLater(() -> {
                    usersTable.setItems(users);
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> 
                showError("Database Error", "Failed to load users: " + e.getMessage())
            );
        } finally {
            // Hide loading indicator
            Platform.runLater(() -> loadingIndicator.setVisible(false));
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void showAbout() {
        showInfo("About LMS", 
            "Learning Management System\n" +
            "Version 1.0\n\n" +
            "A comprehensive platform for managing courses, users, and educational content.\n\n" +
            "Features:\n" +
            "- User Management\n" +
            "- Course Management\n" +
            "- Enrollment Tracking\n" +
            "- Analytics and Reporting\n" +
            "- Assignment Management\n\n" +
            "© 2024 LMS Team");
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
        });
    }
} 