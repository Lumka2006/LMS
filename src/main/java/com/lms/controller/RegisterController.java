package com.lms.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.lms.util.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        ObservableList<String> roles = FXCollections.observableArrayList("STUDENT", "TEACHER");
        roleComboBox.setItems(roles);
        roleComboBox.setValue("STUDENT"); // Default role
    }

    @FXML
    private void handleRegister() {
        // Validate input
        if (usernameField.getText().isEmpty() || firstNameField.getText().isEmpty() || 
            lastNameField.getText().isEmpty() || emailField.getText().isEmpty() || 
            passwordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        try {
            // Add user to database
            DatabaseUtil.addUser(
                usernameField.getText(),
                passwordField.getText(),
                emailField.getText(),
                firstNameField.getText(),
                lastNameField.getText(),
                roleComboBox.getValue()
            );

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Your account has been created successfully!");
            alert.showAndWait();

            // Return to login screen
            handleBackToLogin();
        } catch (Exception e) {
            errorLabel.setText("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Error loading login screen: " + e.getMessage());
        }
    }
} 