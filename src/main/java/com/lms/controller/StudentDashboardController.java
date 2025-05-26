package com.lms.controller;

import com.lms.model.Course;
import com.lms.model.User;
import com.lms.model.Assignment;
import com.lms.model.Submission;
import com.lms.service.CourseService;
import com.lms.service.StudentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;
import javafx.application.Platform;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.lms.util.DatabaseUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.text.SimpleDateFormat;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressBar;

public class StudentDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;
    @FXML private TableView<Course> enrolledCoursesTable;
    @FXML private TableView<Assignment> assignmentsTable;
    @FXML private Label courseCountLabel;
    @FXML private Label assignmentCountLabel;
    @FXML private Pagination coursesPagination;
    @FXML private Pagination assignmentsPagination;
    @FXML private ScrollPane mainScrollPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ProgressBar courseProgressBar;
    
    private ObservableList<Course> enrolledCourses = FXCollections.observableArrayList();
    private ObservableList<Assignment> allAssignments = FXCollections.observableArrayList();
    private User currentUser;
    private static final int ITEMS_PER_PAGE = 5;
    private CourseService courseService = new CourseService();

    public void setCurrentUser(User user) {
        System.out.println("StudentDashboardController.setCurrentUser called with user: " + (user != null ? user.getUsername() : "null"));
        this.currentUser = user;
        if (currentUser != null) {
            System.out.println("Setting up tables and loading data for user: " + currentUser.getUsername());
            setupTables();
            loadUserData();
        } else {
            System.out.println("ERROR: currentUser is null in setCurrentUser");
        }
    }

    @FXML
    public void initialize() {
        System.out.println("StudentDashboardController.initialize called");
        
        // Add visual effects
        addVisualEffects();
        
        // Setup pagination
        setupPagination();
        
        // Get the current user from LoginController
        currentUser = LoginController.getCurrentUser();
        if (currentUser == null) {
            System.out.println("ERROR: currentUser is null in initialize()");
            return;
        }
        
        System.out.println("Current user in initialize: " + currentUser.getUsername() + " (ID: " + currentUser.getUserId() + ")");
        
        setupTables();
        loadUserData();
    }

    private void addVisualEffects() {
        // Add shadow effect to the main container
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(3.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.color(0, 0, 0, 0.2));
        mainTabPane.setEffect(shadow);
    }

    private void setupPagination() {
        coursesPagination.setPageCount(1);
        assignmentsPagination.setPageCount(1);
        
        coursesPagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            int fromIndex = newVal.intValue() * ITEMS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, enrolledCourses.size());
            enrolledCoursesTable.setItems(FXCollections.observableArrayList(enrolledCourses.subList(fromIndex, toIndex)));
        });
        
        assignmentsPagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            int fromIndex = newVal.intValue() * ITEMS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allAssignments.size());
            assignmentsTable.setItems(FXCollections.observableArrayList(allAssignments.subList(fromIndex, toIndex)));
        });
    }

    private VBox createPage(int pageIndex) {
        VBox pageBox = new VBox(10);
        pageBox.setPadding(new Insets(10));
        
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allAssignments.size());
        
        for (int i = fromIndex; i < toIndex; i++) {
            Assignment assignment = allAssignments.get(i);
            VBox assignmentBox = createAssignmentBox(assignment);
            pageBox.getChildren().add(assignmentBox);
        }
        
        return pageBox;
    }

    private VBox createAssignmentBox(Assignment assignment) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label titleLabel = new Label(assignment.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label courseLabel = new Label("Course: " + assignment.getCourseName());
        Label dueDateLabel = new Label("Due: " + assignment.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        Label statusLabel = new Label("Status: " + assignment.getStatus());
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(assignment.isSubmitted(currentUser) ? 1.0 : 0.0);
        
        box.getChildren().addAll(titleLabel, courseLabel, dueDateLabel, statusLabel, progressBar);
        
        // Add hover effect
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");
        });
        
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        });
        
        return box;
    }

    private void setupTables() {
        System.out.println("Setting up tables...");
        
        // Clear existing columns
        enrolledCoursesTable.getColumns().clear();
        assignmentsTable.getColumns().clear();
        
        // Course table columns
        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        
        TableColumn<Course, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        TableColumn<Course, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        TableColumn<Course, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        
        TableColumn<Course, LocalDateTime> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
            }
        });
        
        TableColumn<Course, LocalDateTime> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endDateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
            }
        });
        
        enrolledCoursesTable.getColumns().addAll(codeCol, titleCol, descriptionCol, instructorCol, startDateCol, endDateCol);
        
        // Assignment table columns
        TableColumn<Assignment, String> assignmentNameCol = new TableColumn<>("Assignment");
        assignmentNameCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        assignmentNameCol.setPrefWidth(200);
        
        TableColumn<Assignment, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        courseCol.setPrefWidth(150);
        
        TableColumn<Assignment, LocalDateTime> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });
        dueDateCol.setPrefWidth(150);
        
        TableColumn<Assignment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        assignmentsTable.getColumns().addAll(assignmentNameCol, courseCol, dueDateCol, statusCol);
        
        // Set the items
        enrolledCoursesTable.setItems(enrolledCourses);
        assignmentsTable.setItems(allAssignments);
        
        System.out.println("Tables setup completed");
    }

    private void loadUserData() {
        if (currentUser == null) {
            System.out.println("ERROR: Cannot load user data - currentUser is null");
            return;
        }
        
        System.out.println("Loading data for user: " + currentUser.getUsername());
        
        // Show loading indicator
        loadingIndicator.setVisible(true);
        
        Platform.runLater(() -> {
            try {
                welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName());
                loadEnrolledCourses();
                loadAssignments();
                
                // Update pagination
                int pageCount = (int) Math.ceil((double) allAssignments.size() / ITEMS_PER_PAGE);
                assignmentsPagination.setPageCount(Math.max(1, pageCount));
            } finally {
                // Hide loading indicator
                loadingIndicator.setVisible(false);
            }
        });
    }

    @FXML
    private void loadEnrolledCourses() {
        if (currentUser == null) {
            System.out.println("ERROR: Cannot load courses - currentUser is null");
            return;
        }
        
        System.out.println("Loading enrolled courses for user ID: " + currentUser.getUserId());
        
        String query = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) as instructor_name " +
                      "FROM courses c " +
                      "JOIN enrollments e ON c.course_id = e.course_id " +
                      "LEFT JOIN users u ON c.teacher_id = u.user_id " +
                      "WHERE e.student_id = ? AND e.status = 'ACTIVE'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            enrolledCourses.clear();
            while (rs.next()) {
                java.sql.Timestamp startTimestamp = rs.getTimestamp("start_date");
                java.sql.Timestamp endTimestamp = rs.getTimestamp("end_date");
                
                LocalDateTime startDate = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
                LocalDateTime endDate = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;
                
                Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("teacher_id"),
                    rs.getString("instructor_name"),
                    startDate,
                    endDate
                );
                enrolledCourses.add(course);
            }
            
            System.out.println("Total courses found: " + enrolledCourses.size());
            courseCountLabel.setText("Enrolled Courses: " + enrolledCourses.size());
            
            // Update courses pagination
            int pageCount = (int) Math.ceil((double) enrolledCourses.size() / ITEMS_PER_PAGE);
            coursesPagination.setPageCount(Math.max(1, pageCount));
            
            // Set initial page
            if (!enrolledCourses.isEmpty()) {
                enrolledCoursesTable.setItems(FXCollections.observableArrayList(
                    enrolledCourses.subList(0, Math.min(ITEMS_PER_PAGE, enrolledCourses.size()))
                ));
            }
            
        } catch (SQLException e) {
            System.out.println("Error loading courses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void loadAssignments() {
        if (currentUser == null) {
            System.out.println("ERROR: Cannot load assignments - currentUser is null");
            return;
        }
        
        System.out.println("Loading assignments for user ID: " + currentUser.getUserId());
        
        String query = "SELECT a.*, c.title as course_name " +
                      "FROM assignments a " +
                      "JOIN courses c ON a.course_id = c.course_id " +
                      "JOIN enrollments e ON c.course_id = e.course_id " +
                      "WHERE e.student_id = ? " +
                      "ORDER BY a.due_date";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            allAssignments.clear();
            while (rs.next()) {
                java.sql.Timestamp timestamp = rs.getTimestamp("due_date");
                LocalDateTime dueDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                
                Assignment assignment = new Assignment(
                    rs.getInt("assignment_id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    dueDate,
                    rs.getInt("total_points"),
                    rs.getString("course_name")
                );
                allAssignments.add(assignment);
            }
            
            System.out.println("Total assignments found: " + allAssignments.size());
            assignmentCountLabel.setText("Pending Assignments: " + allAssignments.size());
            
            // Update assignments pagination
            int pageCount = (int) Math.ceil((double) allAssignments.size() / ITEMS_PER_PAGE);
            assignmentsPagination.setPageCount(Math.max(1, pageCount));
            
            // Set initial page
            if (!allAssignments.isEmpty()) {
                assignmentsTable.setItems(FXCollections.observableArrayList(
                    allAssignments.subList(0, Math.min(ITEMS_PER_PAGE, allAssignments.size()))
                ));
            }
            
        } catch (SQLException e) {
            System.out.println("Error loading assignments: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Failed to load assignments: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAssignmentDetails(Assignment assignment, VBox detailsBox) {
        System.out.println("showAssignmentDetails called for: " + (assignment != null ? assignment.getTitle() : "null"));
        detailsBox.getChildren().clear();
        detailsBox.getChildren().add(new Label("TEST: Assignment details loaded!"));
        if (assignment == null) {
            detailsBox.getChildren().clear();
            return;
        }

        detailsBox.getChildren().clear();

        // Assignment title
        Label titleLabel = new Label(assignment.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        detailsBox.getChildren().add(titleLabel);

        // Assignment description
        Label descriptionLabel = new Label(assignment.getDescription());
        descriptionLabel.setWrapText(true);
        detailsBox.getChildren().add(descriptionLabel);

        // Due date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        Label dueDateLabel = new Label("Due Date: " + 
            (assignment.getDueDate() != null ? assignment.getDueDate().format(formatter) : "Not set"));
        detailsBox.getChildren().add(dueDateLabel);

        // Submission status
        Label submissionStatusLabel = new Label("Status: " + 
            (assignment.isSubmitted(LoginController.getCurrentUser()) ? "Submitted" : "Not Submitted"));
        detailsBox.getChildren().add(submissionStatusLabel);

        // Add submission section
        VBox submissionBox = new VBox(10);
        submissionBox.setPadding(new Insets(10));
        submissionBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        // Text submission area
        TextArea submissionArea = new TextArea();
        submissionArea.setPromptText("Add your work here...");
        submissionArea.setWrapText(true);
        submissionArea.setPrefRowCount(5);
        submissionArea.setPrefWidth(400);

        // File upload section
        VBox fileUploadBox = new VBox(5);
        Label fileLabel = new Label("Attach Files:");
        ListView<String> fileList = new ListView<>();
        fileList.setPrefHeight(100);
        
        HBox fileButtons = new HBox(10);
        Button addFileButton = new Button("Add File");
        Button removeFileButton = new Button("Remove File");
        
        addFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File");
            File file = fileChooser.showOpenDialog(detailsBox.getScene().getWindow());
            if (file != null) {
                fileList.getItems().add(file.getAbsolutePath());
            }
        });
        
        removeFileButton.setOnAction(e -> {
            int selectedIndex = fileList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                fileList.getItems().remove(selectedIndex);
            }
        });
        
        fileButtons.getChildren().addAll(addFileButton, removeFileButton);
        fileUploadBox.getChildren().addAll(fileLabel, fileList, fileButtons);

        // Submit button
        Button submitButton = new Button("Submit Assignment");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setOnAction(e -> {
            if (submissionArea.getText().trim().isEmpty() && fileList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Empty Submission");
                alert.setHeaderText(null);
                alert.setContentText("Please add some text or attach files before submitting.");
                alert.showAndWait();
                return;
            }
            if (submitAssignment(assignment, submissionArea.getText(), fileList.getItems())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Assignment submitted successfully!");
                loadAssignments(); // Refresh the assignments list
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit assignment. Please try again.");
            }
        });

        // Add all components to submission box
        submissionBox.getChildren().addAll(
            new Label("Your Work:"),
            submissionArea,
            fileUploadBox,
            submitButton
        );

        // Add submission box to main details box
        detailsBox.getChildren().add(submissionBox);

        // Add spacing between sections
        detailsBox.setSpacing(15);
    }

    private void viewFeedback(Assignment assignment) {
        if (!assignment.isSubmitted(LoginController.getCurrentUser())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Feedback");
            alert.setHeaderText(null);
            alert.setContentText("This assignment has not been submitted yet.");
            alert.showAndWait();
            return;
        }

        Dialog<Void> feedbackDialog = new Dialog<>();
        feedbackDialog.setTitle("Assignment Feedback");
        feedbackDialog.setHeaderText("Feedback for " + assignment.getTitle());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Get feedback from database
        String query = "SELECT score, feedback FROM submissions WHERE assignment_id = ? AND user_id = ?";
        try {
            // In a real application, this would query the database
            Label scoreLabel = new Label("Score: 85/100");
            TextArea feedbackArea = new TextArea("Good work! Your solution demonstrates a good understanding of the concepts.");
            feedbackArea.setEditable(false);
            feedbackArea.setWrapText(true);
            feedbackArea.setPrefRowCount(5);
            
            content.getChildren().addAll(scoreLabel, new Label("Feedback:"), feedbackArea);
        } catch (Exception e) {
            content.getChildren().add(new Label("Error loading feedback: " + e.getMessage()));
        }

        feedbackDialog.getDialogPane().setContent(content);
        feedbackDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        feedbackDialog.showAndWait();
    }

    @FXML
    private void showGrades() {
        if (currentUser == null) {
            showError("Error", "User session not initialized. Please log in again.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("My Grades");
        dialog.setHeaderText("Assignment Grades");

        // Create table for grades
        TableView<Grade> gradesTable = new TableView<>();
        
        // Grade columns
        TableColumn<Grade, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> cellData.getValue().courseProperty());
        
        TableColumn<Grade, String> assignmentCol = new TableColumn<>("Assignment");
        assignmentCol.setCellValueFactory(cellData -> cellData.getValue().assignmentProperty());
        
        TableColumn<Grade, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(cellData -> cellData.getValue().gradeProperty());
        
        gradesTable.getColumns().addAll(courseCol, assignmentCol, gradeCol);

        // Load grades from database
        String query = "SELECT c.title as course_name, a.title as assignment_title, " +
                      "s.grade, s.feedback " +
                      "FROM submissions s " +
                      "JOIN assignments a ON s.assignment_id = a.assignment_id " +
                      "JOIN courses c ON a.course_id = c.course_id " +
                      "WHERE s.student_id = ? AND s.grade IS NOT NULL " +
                      "ORDER BY c.title, a.title";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            ObservableList<Grade> grades = FXCollections.observableArrayList();
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                String assignmentTitle = rs.getString("assignment_title");
                int grade = rs.getInt("grade");
                String feedback = rs.getString("feedback");
                
                Grade gradeObj = new Grade(
                    courseName,
                    assignmentTitle,
                    grade + "/100" + (feedback != null ? " - " + feedback : "")
                );
                grades.add(gradeObj);
            }
            
            gradesTable.setItems(grades);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load grades: " + e.getMessage());
        }

        // Layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().add(gradesTable);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private boolean submitAssignment(Assignment assignment, String content, List<String> filePaths) {
        if (assignment == null || currentUser == null) {
            System.out.println("Error: Assignment or currentUser is null");
            return false;
        }

        String query = "INSERT INTO submissions (assignment_id, student_id, content, submitted_at) " +
                      "VALUES (?, ?, ?, ?) RETURNING submission_id";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, assignment.getAssignmentId());
            pstmt.setInt(2, currentUser.getUserId());
            pstmt.setString(3, content);
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int submissionId = rs.getInt(1);
                
                // Save attachments if any
                if (filePaths != null && !filePaths.isEmpty()) {
                    saveAttachments(submissionId, filePaths);
                }
                
                // Refresh assignments list
                loadAssignments();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Submission Error", "Failed to submit assignment: " + e.getMessage());
        }
        return false;
    }

    private void saveAttachments(int submissionId, List<String> filePaths) throws SQLException {
        String query = "INSERT INTO submission_attachments (submission_id, file_path, file_name) " +
                      "VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            for (String filePath : filePaths) {
                File file = new File(filePath);
                pstmt.setInt(1, submissionId);
                pstmt.setString(2, filePath);
                pstmt.setString(3, file.getName());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // Helper class for grades table
    private static class Grade {
        private final StringProperty course;
        private final StringProperty assignment;
        private final StringProperty grade;

        public Grade(String course, String assignment, String grade) {
            this.course = new SimpleStringProperty(course);
            this.assignment = new SimpleStringProperty(assignment);
            this.grade = new SimpleStringProperty(grade);
        }

        public StringProperty courseProperty() { return course; }
        public StringProperty assignmentProperty() { return assignment; }
        public StringProperty gradeProperty() { return grade; }
    }

    // Helper class to hold submission data
    private static class SubmissionData {
        final String text;
        final List<String> files;

        SubmissionData(String text, List<String> files) {
            this.text = text;
            this.files = files;
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            // Get the current stage and set the new scene
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            
            // Clear the current user
            currentUser = null;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }

    @FXML
    private void showAvailableCourses() {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null) {
            showError("Error", "User session not initialized. Please log in again.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Available Courses");
        dialog.setHeaderText("Courses Available for Enrollment");

        // Create table for available courses
        TableView<Course> coursesTable = new TableView<>();
        
        // Course columns
        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        
        TableColumn<Course, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        TableColumn<Course, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        TableColumn<Course, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        
        TableColumn<Course, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getStartDate();
            return new SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        });
        
        TableColumn<Course, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getEndDate();
            return new SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        });

        coursesTable.getColumns().addAll(codeCol, titleCol, descriptionCol, instructorCol, startDateCol, endDateCol);

        // Add enroll button
        Button enrollButton = new Button("Enroll in Selected Course");
        enrollButton.setOnAction(e -> {
            Course selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
            if (selectedCourse == null) {
                showError("Error", "Please select a course to enroll in.");
                return;
            }

            // Enroll the student
            StudentService studentService = new StudentService();
            if (studentService.enrollStudent(currentUser.getUserId(), selectedCourse.getCourseId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully enrolled in " + selectedCourse.getTitle());
                coursesTable.getItems().remove(selectedCourse); // Remove enrolled course from the list
                loadEnrolledCourses(); // Refresh enrolled courses
            } else {
                showError("Error", "Failed to enroll in the course. Please try again.");
            }
        });

        // Load available courses
        List<Course> availableCourses = courseService.getAvailableCourses(currentUser.getUserId());
        coursesTable.setItems(FXCollections.observableArrayList(availableCourses));

        // Layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(coursesTable, enrollButton);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void showSubmitAssignmentDialog() {
        Assignment selectedAssignment = assignmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            showAlert(Alert.AlertType.WARNING, "No Assignment Selected", 
                     "Please select an assignment to submit.");
            return;
        }

        // Check if already submitted
        if (selectedAssignment.isSubmitted(currentUser)) {
            showAlert(Alert.AlertType.INFORMATION, "Already Submitted", 
                     "You have already submitted this assignment.");
            return;
        }

        // Create submission dialog
        Dialog<SubmissionData> dialog = new Dialog<>();
        dialog.setTitle("Submit Assignment");
        dialog.setHeaderText("Submit Assignment: " + selectedAssignment.getTitle());

        // Set the button types
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Create the submission form
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Text submission area
        TextArea submissionText = new TextArea();
        submissionText.setPromptText("Enter your submission here...");
        submissionText.setWrapText(true);
        submissionText.setPrefRowCount(5);

        // File upload section
        VBox fileUploadBox = new VBox(5);
        Label fileLabel = new Label("Attach Files:");
        ListView<String> fileList = new ListView<>();
        fileList.setPrefHeight(100);
        
        HBox fileButtons = new HBox(10);
        Button addFileButton = new Button("Add File");
        Button removeFileButton = new Button("Remove File");
        
        addFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Attach");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx")
            );
            
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                fileList.getItems().add(file.getAbsolutePath());
            }
        });
        
        removeFileButton.setOnAction(e -> {
            int selectedIndex = fileList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                fileList.getItems().remove(selectedIndex);
            }
        });
        
        fileButtons.getChildren().addAll(addFileButton, removeFileButton);
        fileUploadBox.getChildren().addAll(fileLabel, fileList, fileButtons);

        // Add components to content
        content.getChildren().addAll(
            new Label("Submission Text:"),
            submissionText,
            fileUploadBox
        );

        dialog.getDialogPane().setContent(content);

        // Convert the result to a submission when the submit button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return new SubmissionData(
                    submissionText.getText(),
                    new ArrayList<>(fileList.getItems())
                );
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(submissionData -> {
            if (submissionData != null && (!submissionData.text.isEmpty() || !submissionData.files.isEmpty())) {
                try {
                    // Submit the assignment
                    if (submitAssignment(selectedAssignment, submissionData.text, submissionData.files)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", 
                                "Assignment submitted successfully!");
                        // Refresh assignments list
                        loadAssignments();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", 
                                "Failed to submit assignment. Please try again.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "An error occurred: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void showAbout() {
        showAlert(Alert.AlertType.INFORMATION, "About", "Learning Management System\nVersion 1.0");
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleViewAssignments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentAssignments.fxml"));
            Parent root = loader.load();
            
            StudentAssignmentsController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to load assignments view: " + e.getMessage());
        }
    }

    @FXML
    private void showStudentProgress() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session not initialized. Please log in again.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("My Progress");
        dialog.setHeaderText("Academic Progress Overview");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Overall Progress Section
        VBox overallProgressBox = new VBox(10);
        Label overallProgressLabel = new Label("Overall Progress");
        overallProgressLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ProgressBar overallProgressBar = new ProgressBar();
        overallProgressBar.setPrefWidth(300);
        
        Label progressPercentageLabel = new Label();
        progressPercentageLabel.setStyle("-fx-font-size: 14px;");
        
        overallProgressBox.getChildren().addAll(overallProgressLabel, overallProgressBar, progressPercentageLabel);

        // Course Progress Table
        TableView<CourseProgress> courseProgressTable = new TableView<>();
        
        TableColumn<CourseProgress, String> courseNameCol = new TableColumn<>("Course");
        courseNameCol.setCellValueFactory(cellData -> cellData.getValue().courseNameProperty());
        
        TableColumn<CourseProgress, String> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(cellData -> cellData.getValue().progressProperty());
        
        TableColumn<CourseProgress, String> assignmentsCol = new TableColumn<>("Assignments");
        assignmentsCol.setCellValueFactory(cellData -> cellData.getValue().assignmentsProperty());
        
        courseProgressTable.getColumns().addAll(courseNameCol, progressCol, assignmentsCol);

        // Load data
        try {
            // Get enrolled courses
            List<Course> courses = courseService.getCoursesByStudent(currentUser.getUserId());
            ObservableList<CourseProgress> courseProgressList = FXCollections.observableArrayList();
            
            double totalProgress = 0;
            int totalAssignments = 0;
            int completedAssignments = 0;

            for (Course course : courses) {
                // Calculate course progress
                String progressQuery = """
                    SELECT 
                        COUNT(*) as total_assignments,
                        SUM(CASE WHEN s.grade IS NOT NULL THEN s.grade ELSE 0 END) as total_grade,
                        COUNT(CASE WHEN s.grade IS NOT NULL THEN 1 END) as graded_count
                    FROM assignments a
                    LEFT JOIN submissions s ON a.assignment_id = s.assignment_id 
                        AND s.student_id = ?
                    WHERE a.course_id = ?
                """;

                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(progressQuery)) {
                    
                    pstmt.setInt(1, currentUser.getUserId());
                    pstmt.setInt(2, course.getCourseId());
                    
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        int courseAssignments = rs.getInt("total_assignments");
                        double totalGrade = rs.getDouble("total_grade");
                        int gradedCount = rs.getInt("graded_count");
                        
                        // Calculate progress based on actual grades
                        double courseProgress = gradedCount > 0 ? (totalGrade / (gradedCount * 100)) * 100 : 0;
                        totalProgress += courseProgress;

                        CourseProgress progress = new CourseProgress(
                            course.getTitle(),
                            String.format("%.1f%%", courseProgress),
                            String.format("%d/%d", gradedCount, courseAssignments)
                        );
                        courseProgressList.add(progress);
                    }
                }
            }

            // Update overall progress
            double averageProgress = courses.isEmpty() ? 0 : totalProgress / courses.size();
            overallProgressBar.setProgress(averageProgress / 100);
            progressPercentageLabel.setText(String.format("%.1f%%", averageProgress));
            
            // Set table items
            courseProgressTable.setItems(courseProgressList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Failed to load progress data: " + e.getMessage());
        }

        content.getChildren().addAll(overallProgressBox, courseProgressTable);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // Helper class for course progress table
    private static class CourseProgress {
        private final StringProperty courseName;
        private final StringProperty progress;
        private final StringProperty assignments;

        public CourseProgress(String courseName, String progress, String assignments) {
            this.courseName = new SimpleStringProperty(courseName);
            this.progress = new SimpleStringProperty(progress);
            this.assignments = new SimpleStringProperty(assignments);
        }

        public StringProperty courseNameProperty() { return courseName; }
        public StringProperty progressProperty() { return progress; }
        public StringProperty assignmentsProperty() { return assignments; }
    }

    @FXML
    private void handleAssignmentSubmission(Assignment assignment) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session not initialized. Please log in again.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Assignment File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(welcomeLabel.getScene().getWindow());
        if (file != null) {
            try {
                // Create submission
                Submission submission = new Submission(
                    0, // ID will be set by database
                    currentUser,
                    assignment,
                    file.getName()
                );
                
                // Save submission to database
                String query = """
                    INSERT INTO submissions (assignment_id, student_id, content, submitted_at)
                    VALUES (?, ?, ?, ?) RETURNING submission_id
                """;
                
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    
                    pstmt.setInt(1, assignment.getAssignmentId());
                    pstmt.setInt(2, currentUser.getUserId());
                    pstmt.setString(3, file.getName());
                    pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        int submissionId = rs.getInt(1);
                        
                        // Save file path to attachments
                        String attachmentQuery = """
                            INSERT INTO submission_attachments (submission_id, file_path, file_name)
                            VALUES (?, ?, ?)
                        """;
                        try (PreparedStatement attachStmt = conn.prepareStatement(attachmentQuery)) {
                            attachStmt.setInt(1, submissionId);
                            attachStmt.setString(2, file.getAbsolutePath());
                            attachStmt.setString(3, file.getName());
                            attachStmt.executeUpdate();
                        }
                        
                        // Update course progress
                        List<Course> courses = courseService.getCoursesByStudent(currentUser.getUserId());
                        Course course = courses.stream()
                            .filter(c -> c.getCourseId() == assignment.getCourseId())
                            .findFirst()
                            .orElse(null);
                            
                        if (course != null) {
                            course.calculateProgress(currentUser);
                            showStudentProgress(); // Refresh progress display
                            showAlert(Alert.AlertType.INFORMATION, "Success", 
                                    "Assignment submitted successfully. Progress updated.");
                        }
                        
                        loadAssignments(); // Refresh assignments list
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to submit assignment: " + e.getMessage());
            }
        }
    }
} 