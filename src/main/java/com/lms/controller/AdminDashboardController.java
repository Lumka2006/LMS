package com.lms.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import com.lms.model.User;
import com.lms.util.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert.AlertType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;

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