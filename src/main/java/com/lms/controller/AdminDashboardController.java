package com.lms.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import com.lms.model.User;
import com.lms.model.Course;
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
import java.util.Date;
import javafx.application.Platform;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    
    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, String> courseIdColumn;
    @FXML private TableColumn<Course, String> courseCodeColumn;
    @FXML private TableColumn<Course, String> courseTitleColumn;
    @FXML private TableColumn<Course, String> instructorColumn;
    
    @FXML private TextField userSearchField;
    @FXML private TextField courseSearchField;
    @FXML private Pagination usersPagination;
    @FXML private Pagination coursesPagination;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ProgressBar courseProgressBar;
    
    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<Course> courses = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 10;
    private User currentUser;

    @FXML
    public void initialize() {
        // Get the current user from LoginController
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null) {
            showError("Error", "User session not initialized. Please log in again.");
            return;
        }
        
        // Show loading indicator
        loadingIndicator.setVisible(true);
        
        // Load data in background
        new Thread(() -> {
            try {
        loadUsers();
        loadCourses();
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
        
        // Setup users table
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> data.getValue().emailProperty());
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> data.getValue().roleProperty());
        
        usersTable.getColumns().addAll(usernameCol, emailCol, roleCol);
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
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                        rs.getString("role"),
                        rs.getString("status")
                    );
                    users.add(user);
                }
                
                // Update table on JavaFX thread
                Platform.runLater(() -> {
                    usersTable.setItems(users);
                    setupTableColumns();
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

    private void setupTableColumns() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    private void loadCourses() {
        if (currentUser == null) {
            showError("Error", "User session not initialized. Please log in again.");
            return;
        }

        try {
            String query = "SELECT * FROM courses ORDER BY course_id";
        try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
            
            courses.clear();
            while (rs.next()) {
                Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("teacher_id"),
                    rs.getString("instructor_name"),
                        rs.getTimestamp("start_date").toLocalDateTime(),
                        rs.getTimestamp("end_date").toLocalDateTime()
                );
                courses.add(course);
            }
            
                coursesTable.setItems(courses);
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load courses: " + e.getMessage());
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

    @FXML
    private void showCourseProgress() {
        if (currentUser == null) {
            showError("Error", "User session not initialized. Please log in again.");
            return;
        }

        // First show course selection dialog
        Dialog<Course> courseDialog = new Dialog<>();
        courseDialog.setTitle("Select Course");
        courseDialog.setHeaderText("Choose a course to view progress");

        // Create course selection table
        TableView<Course> courseTable = new TableView<>();
        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        
        TableColumn<Course, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        courseTable.getColumns().addAll(codeCol, titleCol);
        courseTable.setItems(courses);

        courseDialog.getDialogPane().setContent(courseTable);
        courseDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        courseDialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return courseTable.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Course> selectedCourse = courseDialog.showAndWait();
        if (selectedCourse.isPresent()) {
            showCourseProgressDetails(selectedCourse.get());
        }
    }

    private void showCourseProgressDetails(Course course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Progress");
        dialog.setHeaderText("Progress for " + course.getTitle());

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Course Overview Section
        VBox overviewBox = new VBox(10);
        Label overviewLabel = new Label("Course Overview");
        overviewLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Get course statistics
        String query = """
            SELECT 
                COUNT(DISTINCT e.student_id) as total_students,
                COUNT(DISTINCT a.assignment_id) as total_assignments,
                COUNT(DISTINCT CASE WHEN s.status = 'GRADED' THEN s.submission_id END) as graded_submissions,
                AVG(CASE WHEN s.grade IS NOT NULL THEN s.grade ELSE 0 END) as average_grade
            FROM courses c
            LEFT JOIN enrollments e ON c.course_id = e.course_id
            LEFT JOIN assignments a ON c.course_id = a.course_id
            LEFT JOIN submissions s ON a.assignment_id = s.assignment_id
            WHERE c.course_id = ?
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, course.getCourseId());
            ResultSet rs = pstmt.executeQuery();
            
                if (rs.next()) {
                int totalStudents = rs.getInt("total_students");
                int totalAssignments = rs.getInt("total_assignments");
                int gradedSubmissions = rs.getInt("graded_submissions");
                double averageGrade = rs.getDouble("average_grade");
                
                // Create statistics labels
                Label studentsLabel = new Label("Total Students: " + totalStudents);
                Label assignmentsLabel = new Label("Total Assignments: " + totalAssignments);
                Label submissionsLabel = new Label("Graded Submissions: " + gradedSubmissions);
                Label averageLabel = new Label(String.format("Average Grade: %.1f%%", averageGrade));
                
                overviewBox.getChildren().addAll(
                    overviewLabel,
                    studentsLabel,
                    assignmentsLabel,
                    submissionsLabel,
                    averageLabel
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load course statistics: " + e.getMessage());
            return;
        }
        
        // Student Progress Table
        TableView<StudentProgress> studentProgressTable = new TableView<>();
        
        TableColumn<StudentProgress, String> studentNameCol = new TableColumn<>("Student");
        studentNameCol.setCellValueFactory(cellData -> cellData.getValue().studentNameProperty());
        
        TableColumn<StudentProgress, String> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(cellData -> cellData.getValue().progressProperty());
        
        TableColumn<StudentProgress, String> assignmentsCol = new TableColumn<>("Assignments");
        assignmentsCol.setCellValueFactory(cellData -> cellData.getValue().assignmentsProperty());
        
        TableColumn<StudentProgress, String> gradeCol = new TableColumn<>("Average Grade");
        gradeCol.setCellValueFactory(cellData -> cellData.getValue().gradeProperty());
        
        studentProgressTable.getColumns().addAll(studentNameCol, progressCol, assignmentsCol, gradeCol);

        // Load student progress data
        String studentQuery = """
            SELECT 
                u.user_id,
                CONCAT(u.first_name, ' ', u.last_name) as student_name,
                COUNT(DISTINCT a.assignment_id) as total_assignments,
                COUNT(DISTINCT CASE WHEN s.status = 'GRADED' THEN s.submission_id END) as completed_assignments,
                AVG(CASE WHEN s.grade IS NOT NULL THEN s.grade ELSE 0 END) as average_grade,
                SUM(CASE WHEN s.grade IS NOT NULL THEN s.grade ELSE 0 END) as total_grade,
                COUNT(CASE WHEN s.grade IS NOT NULL THEN 1 END) as graded_count
            FROM users u
            JOIN enrollments e ON u.user_id = e.student_id
            LEFT JOIN assignments a ON e.course_id = a.course_id
            LEFT JOIN submissions s ON a.assignment_id = s.assignment_id AND u.user_id = s.student_id
            WHERE e.course_id = ?
            GROUP BY u.user_id, u.first_name, u.last_name
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(studentQuery)) {
            
            pstmt.setInt(1, course.getCourseId());
            ResultSet rs = pstmt.executeQuery();
            
            ObservableList<StudentProgress> studentProgressList = FXCollections.observableArrayList();
            while (rs.next()) {
                int totalAssignments = rs.getInt("total_assignments");
                int completedAssignments = rs.getInt("completed_assignments");
                double totalGrade = rs.getDouble("total_grade");
                int gradedCount = rs.getInt("graded_count");
                double averageGrade = rs.getDouble("average_grade");
                
                // Calculate progress based on grades
                double progress = gradedCount > 0 ? (totalGrade / (gradedCount * 100)) * 100 : 0;
                
                StudentProgress studentProgress = new StudentProgress(
                    rs.getString("student_name"),
                    String.format("%.1f%%", progress),
                    String.format("%d/%d", completedAssignments, totalAssignments),
                    String.format("%.1f%%", averageGrade)
                );
                studentProgressList.add(studentProgress);
            }
            
            studentProgressTable.setItems(studentProgressList);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load student progress: " + e.getMessage());
        }

        content.getChildren().addAll(overviewBox, studentProgressTable);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // Helper class for student progress table
    private static class StudentProgress {
        private final StringProperty studentName;
        private final StringProperty progress;
        private final StringProperty assignments;
        private final StringProperty grade;

        public StudentProgress(String studentName, String progress, String assignments, String grade) {
            this.studentName = new SimpleStringProperty(studentName);
            this.progress = new SimpleStringProperty(progress);
            this.assignments = new SimpleStringProperty(assignments);
            this.grade = new SimpleStringProperty(grade);
            }

        public StringProperty studentNameProperty() { return studentName; }
        public StringProperty progressProperty() { return progress; }
        public StringProperty assignmentsProperty() { return assignments; }
        public StringProperty gradeProperty() { return grade; }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 