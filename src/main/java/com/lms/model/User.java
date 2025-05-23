package com.lms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;

public class User {
    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty email;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty role;
    private final StringProperty name;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private static final List<User> users = new ArrayList<>();

    public enum UserRole {
        ADMIN,
        TEACHER,
        STUDENT
    }

    // Static initialization block for predefined users
    static {
        // Add some predefined users
        users.add(new User(1, "student1", "pass123", "student1@example.com", "STUDENT", "John Student"));
        users.add(new User(2, "teacher1", "pass123", "teacher1@example.com", "TEACHER", "Jane Teacher"));
        users.add(new User(3, "admin1", "pass123", "admin1@example.com", "ADMIN", "Admin User"));
    }

    // Constructor
    public User(int userId, String username, String email, String password, String firstName, String lastName, String role) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.email = new SimpleStringProperty(email);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.role = new SimpleStringProperty(role);
        this.name = new SimpleStringProperty(firstName + " " + lastName);
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Simplified constructor for login
    public User(int userId, String username, String password, String email, String role) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.firstName = new SimpleStringProperty("");
        this.lastName = new SimpleStringProperty("");
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.name = new SimpleStringProperty(username);
    }

    // Constructor for registration
    public User(int userId, String username, String password, String email, String role, String name) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.firstName = new SimpleStringProperty("");
        this.lastName = new SimpleStringProperty("");
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.name = new SimpleStringProperty(name);
    }

    // Getters for properties
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty emailProperty() { return email; }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty roleProperty() { return role; }
    public StringProperty nameProperty() { return name; }

    // Getters for values
    public int getUserId() { return userId.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getEmail() { return email.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getRole() { return role.get(); }
    public String getName() { return name.get(); }

    // Setters
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setUsername(String username) { this.username.set(username); }
    public void setPassword(String password) { this.password.set(password); }
    public void setEmail(String email) { this.email.set(email); }
    public void setFirstName(String firstName) { 
        this.firstName.set(firstName);
        this.name.set(firstName + " " + this.lastName.get());
    }
    public void setLastName(String lastName) { 
        this.lastName.set(lastName);
        this.name.set(this.firstName.get() + " " + lastName);
    }
    public void setRole(String role) { this.role.set(role); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getFullName() {
        return firstName.get() + " " + lastName.get();
    }

    public static User authenticate(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public static boolean addUser(User newUser) {
        // Check if username already exists
        for (User user : users) {
            if (user.getUsername().equals(newUser.getUsername())) {
                return false;
            }
        }
        users.add(newUser);
        return true;
    }

    public static int getNextId() {
        return users.size() + 1;
    }
} 