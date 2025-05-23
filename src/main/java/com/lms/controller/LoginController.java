package com.lms.controller;

import com.lms.model.User;
import com.lms.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.layout.VBox;
import javafx.scene.control.ComboBox;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.control.Label;

public class LoginController {
    private static User currentUser;
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private TextField registerFirstNameField;
    @FXML private TextField registerLastNameField;
    @FXML private TextField registerEmailField;
    @FXML private TextField registerUsernameField;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField registerConfirmPasswordField;
    @FXML private ComboBox<String> registerRoleComboBox;
    @FXML private Label registerErrorLabel;
    
    private UserService userService;

    public LoginController() {
        this.userService = new UserService();
    }

    @FXML
    public void initialize() {
        // Initialize role ComboBox
        registerRoleComboBox.getItems().addAll("STUDENT", "TEACHER");
        registerRoleComboBox.setValue("STUDENT"); // Set default value
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("Attempting login for username: " + username);

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Login Error", "Please enter both username and password.");
            return;
        }

        User user = userService.authenticate(username, password);
                if (user != null) {
            System.out.println("Login successful for user: " + user.getUsername() + " with role: " + user.getRole());
            currentUser = user;
            try {
                loadDashboard(user);
                    } catch (IOException e) {
                System.out.println("Error loading dashboard: " + e.getMessage());
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "Failed to load dashboard: " + e.getMessage());
            }
        } else {
            System.out.println("Login failed for username: " + username);
            showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
    }

    @FXML
    private void handleRegister() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), loginForm);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            loginForm.setVisible(false);
            loginForm.setManaged(false);
            registerForm.setVisible(true);
            registerForm.setManaged(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), registerForm);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void handleRegisterSubmit() {
        String username = registerUsernameField.getText();
        String password = registerPasswordField.getText();
        String email = registerEmailField.getText();
        String firstName = registerFirstNameField.getText();
        String lastName = registerLastNameField.getText();
        String role = registerRoleComboBox.getValue();

        // Validate input
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || 
            firstName.isEmpty() || lastName.isEmpty() || role == null) {
            showAlert(AlertType.ERROR, "Registration Error", 
                     "Please fill in all fields.");
            return;
        }

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(AlertType.ERROR, "Registration Error", 
                     "Please enter a valid email address.");
            return;
        }

        // Validate password strength
        if (password.length() < 6) {
            showAlert(AlertType.ERROR, "Registration Error", 
                     "Password must be at least 6 characters long.");
            return;
        }

        try {
            // Attempt to register the user
            User newUser = userService.registerUser(username, password, email, firstName, lastName, role);
            if (newUser != null) {
                showAlert(AlertType.INFORMATION, "Registration Successful", 
                         "Your account has been created. Please log in.");
                // Clear registration fields and return to login
                clearRegistrationFields();
                handleBackToLogin();
            } else {
                showAlert(AlertType.ERROR, "Registration Failed", 
                         "Username or email already exists.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Registration Error", 
                     "An error occurred during registration: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), registerForm);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            registerForm.setVisible(false);
            registerForm.setManaged(false);
            loginForm.setVisible(true);
            loginForm.setManaged(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loginForm);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void clearRegistrationFields() {
        registerUsernameField.clear();
        registerPasswordField.clear();
        registerEmailField.clear();
        registerFirstNameField.clear();
        registerLastNameField.clear();
    }

    private void loadDashboard(User user) throws IOException {
        String fxmlPath;
        if (user.getRole().equals("STUDENT")) {
            fxmlPath = "/fxml/StudentDashboard.fxml";
        } else if (user.getRole().equals("TEACHER")) {
            fxmlPath = "/fxml/InstructorDashboard.fxml";
        } else {
            fxmlPath = "/fxml/AdminDashboard.fxml";
        }

        System.out.println("Loading dashboard FXML from path: " + fxmlPath);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        if (loader.getLocation() == null) {
            System.out.println("ERROR: Could not find FXML file at path: " + fxmlPath);
            throw new IOException("Could not find FXML file: " + fxmlPath);
        }
        System.out.println("FXML file found, loading...");
        
        Parent root = loader.load();
        System.out.println("FXML loaded successfully");

        // Set the current user in the dashboard controller
        if (user.getRole().equals("STUDENT")) {
            System.out.println("Setting current user in StudentDashboardController");
            StudentDashboardController controller = loader.getController();
            controller.setCurrentUser(user);
        } else if (user.getRole().equals("TEACHER")) {
            System.out.println("Setting current user in InstructorDashboardController");
            InstructorDashboardController controller = loader.getController();
            controller.setCurrentUser(user);
        } else {
            System.out.println("Setting current user in AdminDashboardController");
            AdminDashboardController controller = loader.getController();
            controller.setCurrentUser(user);
        }

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Learning Management System - " + user.getRole() + " Dashboard");
        System.out.println("Dashboard scene set successfully");
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Static methods for accessing current user
    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clearCurrentUser() {
        currentUser = null;
    }
} 