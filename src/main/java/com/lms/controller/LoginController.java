package com.lms.controller;

// Importing model and service classes for the LMS system
import com.lms.model.User;  // Represents the User entity in the Learning Management System (LMS), which could be a student, teacher, or admin.
import com.lms.service.UserService;  // Service class that handles business logic related to User entities (e.g., registration, authentication).

// Importing JavaFX classes for working with the GUI and controls
import javafx.fxml.FXML;  // Annotation used to link the controller code to an FXML file (for handling UI events and data binding).
import javafx.scene.control.TextField;  // A text input field for user input (e.g., username, email).
import javafx.scene.control.PasswordField;  // A specialized input field for securely entering passwords (characters are hidden).
import javafx.scene.control.Alert;  // A dialog box used to show messages to the user (e.g., information, error, or warning messages).
import javafx.scene.control.Alert.AlertType;  // Enum defining the type of alert to show (e.g., INFORMATION, ERROR, WARNING).
import javafx.fxml.FXMLLoader;  // Used to load FXML files and convert them into JavaFX scene graphs, which bind the UI to the controller.
import javafx.scene.Parent;  // Represents the root node of a JavaFX scene graph, containing all the UI components.
import javafx.scene.Scene;  // Represents the scene (window) that is displayed inside the primary stage in a JavaFX application.
import javafx.stage.Stage;  // Represents the main window (stage) of a JavaFX application, which can contain multiple scenes.

import java.io.IOException;  // Exception class for handling input/output errors, such as issues when loading an FXML file or reading from a file.
import javafx.scene.layout.VBox;  // A layout container that arranges its children vertically (used for form elements like text fields, buttons).
import javafx.scene.control.ComboBox;  // A drop-down list (combo box) for selecting from a predefined set of options (e.g., role selection).
import javafx.animation.FadeTransition;  // Used to create a fade-in or fade-out animation effect for JavaFX components.
import javafx.util.Duration;  // Represents time duration for animations and transitions (used with `FadeTransition` to control animation speed).
import javafx.scene.control.Label;  // A label component to display text to the user (e.g., instructional text or validation messages).


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
        // Create fade out transition for login form
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), loginForm);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        // Create slide out effect
        javafx.animation.TranslateTransition slideOut = new javafx.animation.TranslateTransition(Duration.millis(500), loginForm);
        slideOut.setFromX(0);
        slideOut.setToX(-50);
        slideOut.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        // Combine animations
        javafx.animation.ParallelTransition parallelOut = new javafx.animation.ParallelTransition(fadeOut, slideOut);
        
        parallelOut.setOnFinished(e -> {
            loginForm.setVisible(false);
            loginForm.setManaged(false);
            registerForm.setVisible(true);
            registerForm.setManaged(true);
            
            // Reset register form position
            registerForm.setTranslateX(50);
            registerForm.setOpacity(0);
            
            // Create fade in transition for register form
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), registerForm);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(javafx.animation.Interpolator.EASE_IN);
            
            // create slide in effect
            javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(Duration.millis(500), registerForm);
            slideIn.setFromX(50);
            slideIn.setToX(0);
            slideIn.setInterpolator(javafx.animation.Interpolator.EASE_IN);
            
            // Combine animations
            javafx.animation.ParallelTransition parallelIn = new javafx.animation.ParallelTransition(fadeIn, slideIn);
            parallelIn.play();
        });
        
        parallelOut.play();
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
        // Create fade out transition for register form
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), registerForm);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        // Create slide out effect
        javafx.animation.TranslateTransition slideOut = new javafx.animation.TranslateTransition(Duration.millis(500), registerForm);
        slideOut.setFromX(0);
        slideOut.setToX(50);
        slideOut.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        // Combine animations
        javafx.animation.ParallelTransition parallelOut = new javafx.animation.ParallelTransition(fadeOut, slideOut);
        
        parallelOut.setOnFinished(e -> {
            registerForm.setVisible(false);
            registerForm.setManaged(false);
            loginForm.setVisible(true);
            loginForm.setManaged(true);
            
            // Reset login form position
            loginForm.setTranslateX(-50);
            loginForm.setOpacity(0);
            
            // Create fade in transition for login form
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), loginForm);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(javafx.animation.Interpolator.EASE_IN);
            
            // Create slide in effect
            javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(Duration.millis(500), loginForm);
            slideIn.setFromX(-50);
            slideIn.setToX(0);
            slideIn.setInterpolator(javafx.animation.Interpolator.EASE_IN);
            
            // Combine animations
            javafx.animation.ParallelTransition parallelIn = new javafx.animation.ParallelTransition(fadeIn, slideIn);
            parallelIn.play();
        });
        
        parallelOut.play();
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

        // Use the correct class loader to load the FXML file
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource(fxmlPath));
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