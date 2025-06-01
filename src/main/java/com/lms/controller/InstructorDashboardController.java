package com.lms.controller;

// Importing model classes representing various entities in the LMS
import com.lms.model.Course;  // Represents the Course entity in the Learning Management System (LMS).
import com.lms.model.Assignment;  // Represents the Assignment entity in the LMS, which can be linked to a course.
import com.lms.model.Student;  // Represents the Student entity, containing information about students in the LMS.
import com.lms.model.User;  // Represents the User entity in the system, which could be a student, teacher, or admin.
import com.lms.model.Submission;  // Represents a Submission entity, capturing data related to student assignment submissions.
import com.lms.model.SubmissionAttachment;  // Represents any attachments made to a Submission, such as files uploaded by the student.

// Importing service classes to handle business logic for LMS entities
import com.lms.service.CourseService;  // Service class responsible for managing course-related logic (CRUD operations, etc.).
import com.lms.service.AssignmentService;  // Service class to manage assignment-related operations (e.g., creating, editing assignments).
import com.lms.service.StudentService;  // Service class to handle student-specific logic (e.g., enrollment, fetching student details).
import com.lms.service.SubmissionService;  // Service class to manage submission-related logic (e.g., saving and grading submissions).

// Importing JavaFX classes to handle user interface (UI) logic
import javafx.fxml.FXML;  // Annotation to link the controller code with an FXML file (to handle UI events and bindings).
import javafx.fxml.Initializable;  // Interface that allows initialization of a controller class when the FXML is loaded.
import javafx.scene.control.*;  // Importing common JavaFX controls such as Buttons, Labels, TableViews, ComboBoxes, TextFields, etc.
import javafx.scene.control.cell.PropertyValueFactory;  // Used to map data from an object to a TableView column (displaying model data in tables).
import javafx.collections.FXCollections;  // Utility class for creating and manipulating ObservableLists.
import javafx.collections.ObservableList;  // Represents a list that can be observed and automatically updated by the UI (e.g., for TableView, ComboBox).
import javafx.scene.layout.VBox;  // A vertical layout container used to arrange UI elements vertically (often for forms).
import javafx.scene.layout.GridPane;  // A layout container that arranges elements in a grid-like structure (rows and columns).
import javafx.scene.layout.HBox;  // A horizontal layout container used to arrange UI elements in a row.
import javafx.geometry.Insets;  // Defines padding (margins) for JavaFX layout containers like VBox and GridPane, for spacing between elements.
import javafx.beans.property.SimpleStringProperty;  // A simple string property that can be bound to UI components (used for TableView columns and other UI elements).

// Importing classes for URL handling and managing resources for JavaFX
import java.net.URL;  // Represents a Uniform Resource Locator, commonly used to load resources like FXML files and images.
import java.util.ResourceBundle;  // Holds data (such as strings) for internationalization (i18n) and localization (l10n), usually for FXML and UI elements.

import java.util.List;  // Represents a collection of objects, commonly used for storing multiple items (e.g., list of students, submissions).
import java.time.LocalDateTime;  // Represents the current date and time without time zone information (useful for timestamps).
import java.time.format.DateTimeFormatter;  // Used to format and parse date-time objects as strings.

import java.sql.Connection;  // Represents a connection to a SQL database (used for querying and interacting with the database).
import java.sql.PreparedStatement;  // Represents a SQL query that can be precompiled for repeated execution with different parameters (to prevent SQL injection).
import java.sql.ResultSet;  // Represents the result set of a SQL query, which contains data returned from the database.
import java.sql.SQLException;  // Exception class for handling database errors such as connection issues or invalid SQL queries.

import com.lms.util.DatabaseUtil;  // A utility class to handle database connections and interactions, making the database logic reusable and manageable.

import javafx.fxml.FXMLLoader;  // Used to load FXML files and convert them into JavaFX scene graphs (connecting the UI with the controller logic).
import javafx.scene.Parent;  // Represents the root of the scene graph in a JavaFX application (used as a container for all UI components).
import javafx.scene.Scene;  // Represents the scene (UI window) that is displayed in the primary stage (JavaFX app window).
import javafx.stage.Stage;  // Represents the primary window (stage) of the JavaFX application. A stage contains a scene and controls how it is displayed.


public class InstructorDashboardController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalAssignmentsLabel;
    
    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, String> codeColumn;
    @FXML private TableColumn<Course, String> titleColumn;
    @FXML private TableColumn<Course, String> startDateColumn;
    @FXML private TableColumn<Course, String> endDateColumn;
    
    @FXML private TableView<Assignment> assignmentsTable;
    @FXML private TableColumn<Assignment, String> assignmentTitleColumn;
    @FXML private TableColumn<Assignment, String> assignmentCourseColumn;
    @FXML private TableColumn<Assignment, String> assignmentDueDateColumn;
    
    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> studentNameColumn;
    @FXML private TableColumn<Student, String> studentEmailColumn;
    @FXML private TableColumn<Student, String> studentCoursesColumn;

    private ObservableList<Course> courses = FXCollections.observableArrayList();
    private User currentUser;
    private CourseService courseService;
    private AssignmentService assignmentService;
    private StudentService studentService;
    private SubmissionService submissionService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        courseService = new CourseService();
        assignmentService = new AssignmentService();
        studentService = new StudentService();
        submissionService = new SubmissionService();
        
        // Add CSS styling for table headers
        String tableStyle = ".table-view .column-header { -fx-background-color: #808080; } .table-view .column-header .label { -fx-text-fill: white; -fx-font-weight: bold; } .table-view .column { -fx-background-color: #f0f0f0; } .table-view { -fx-background-color: white; } .table-row-cell { -fx-background-color: white; } .table-row-cell:odd { -fx-background-color: #f0f0f0; }";
        coursesTable.setStyle(tableStyle);
        assignmentsTable.setStyle(tableStyle);
        studentsTable.setStyle(tableStyle);
        
        initializeTableColumns();
    }
    
    private void initializeTableColumns() {
        // Course table columns
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        startDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getStartDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });
        endDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getEndDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });
        
        // Assignment table columns
        assignmentTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        assignmentCourseColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        assignmentDueDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDueDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });
        
        // Student table columns
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        studentCoursesColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            String query = """
                SELECT STRING_AGG(c.title, ', ') as enrolled_courses
                FROM enrollments e
                JOIN courses c ON e.course_id = c.course_id
                WHERE e.student_id = ? AND e.status = 'ACTIVE'
            """;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, student.getId());
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    String courses = rs.getString("enrolled_courses");
                    return new SimpleStringProperty(courses != null ? courses : "No courses enrolled");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("Error loading courses");
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
        welcomeLabel.setText("Welcome, " + user.getFirstName() + " " + user.getLastName());
        }
        setupTable();
        loadDashboardData();
    }

    private void setupTable() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        startDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getStartDate();
            return new SimpleStringProperty(date != null ? 
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        });
        endDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getEndDate();
            return new SimpleStringProperty(date != null ? 
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        });
        
        coursesTable.setItems(courses);
    }

    @FXML
    private void loadDashboardData() {
        if (currentUser == null) {
            currentUser = LoginController.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "User session not initialized. Please log in again.");
                return;
            }
        }
        
        try {
            // Load courses
            List<Course> courses = courseService.getCoursesByInstructor(currentUser.getUserId());
            if (courses != null) {
                coursesTable.setItems(FXCollections.observableArrayList(courses));
                totalCoursesLabel.setText(String.valueOf(courses.size()));
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No courses found for this instructor.");
            }
            
            // Load assignments
            List<Assignment> assignments = assignmentService.getAssignmentsByInstructor(currentUser.getUserId());
            if (assignments != null) {
                assignmentsTable.setItems(FXCollections.observableArrayList(assignments));
                totalAssignmentsLabel.setText(String.valueOf(assignments.size()));
            }
            
            // Load students
            List<Student> students = studentService.getStudentsByInstructor(currentUser.getUserId());
            if (students != null) {
                studentsTable.setItems(FXCollections.observableArrayList(students));
                totalStudentsLabel.setText(String.valueOf(students.size()));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void showAddCourseDialog() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session not initialized. Please log in again.");
            return;
        }

        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Add New Course");
        dialog.setHeaderText("Enter Course Details");

        // Create the custom dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.setPromptText("Course Code");
        TextField titleField = new TextField();
        titleField.setPromptText("Course Title");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefRowCount(3);
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(new Label("End Date:"), 0, 4);
        grid.add(endDatePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Convert the result to a Course when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                    if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please select both start and end dates.");
                        return null;
                    }
                    
                LocalDateTime startDate = startDatePicker.getValue().atStartOfDay();
                LocalDateTime endDate = endDatePicker.getValue().atStartOfDay();
                
                Course course = new Course(
                    0, // ID will be set by the database
                        codeField.getText(),
                        titleField.getText(),
                        descriptionField.getText(),
                        currentUser.getUserId(),
                    currentUser.getName(),
                    startDate,
                    endDate
                );
                return course;
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(course -> {
            if (courseService.addCourse(course)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course added successfully!");
                loadDashboardData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add course.");
            }
        });
    }

    @FXML
    private void showCourseStats() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session not initialized. Please log in again.");
            return;
        }
        Course selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a course first.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Statistics");
        dialog.setHeaderText("Statistics for " + selectedCourse.getTitle());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Get enrolled students
        List<Student> enrolledStudents = studentService.getStudentsByCourse(selectedCourse.getCourseId());
        
        // Get assignments
        List<Assignment> assignments = assignmentService.getAssignmentsByInstructor(currentUser.getUserId());
        assignments.removeIf(a -> a.getCourseId() != selectedCourse.getCourseId());

        // Create statistics labels
        Label studentCountLabel = new Label("Total Enrolled Students: " + enrolledStudents.size());
        Label assignmentCountLabel = new Label("Total Assignments: " + assignments.size());
        
        // Calculate average progress based on grades
        String query = """
            SELECT AVG(
                CASE 
                    WHEN s.grade IS NOT NULL THEN 
                        CAST(s.grade AS DECIMAL) / 100
                    ELSE 0.0
                END
            ) as avg_progress
            FROM submissions s
            JOIN assignments a ON s.assignment_id = a.assignment_id
            WHERE a.course_id = ?
        """;
        
        double avgProgress = 0.0;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, selectedCourse.getCourseId());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                avgProgress = rs.getDouble("avg_progress") * 100; // Convert to percentage
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to calculate average progress: " + e.getMessage());
        }
        
        Label avgProgressLabel = new Label(String.format("Average Student Progress: %.1f%%", avgProgress));

        content.getChildren().addAll(studentCountLabel, assignmentCountLabel, avgProgressLabel);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    private void showCreateAssignmentDialog() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session not initialized. Please log in again.");
            return;
        }
        Course selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a course first.");
            return;
        }

        Dialog<Assignment> dialog = new Dialog<>();
        dialog.setTitle("Create New Assignment");
        dialog.setHeaderText("Enter Assignment Details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Assignment Title");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefRowCount(3);
        DatePicker dueDatePicker = new DatePicker();
        TextField totalPointsField = new TextField();
        totalPointsField.setPromptText("Total Points");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Due Date:"), 0, 2);
        grid.add(dueDatePicker, 1, 2);
        grid.add(new Label("Total Points:"), 0, 3);
        grid.add(totalPointsField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    if (dueDatePicker.getValue() == null) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Please select a due date.");
                        return null;
                    }
                    
                    String totalPointsStr = totalPointsField.getText().trim();
                    if (totalPointsStr.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Please enter total points.");
                        return null;
                    }
                    
                    int totalPoints;
                    try {
                        totalPoints = Integer.parseInt(totalPointsStr);
                        if (totalPoints <= 0) {
                            showAlert(Alert.AlertType.ERROR, "Error", "Total points must be greater than 0.");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Total points must be a valid number.");
                        return null;
                    }
                    
                    LocalDateTime dueDate = dueDatePicker.getValue().atStartOfDay();
                    return new Assignment(
                        0, // ID will be set by the database
                        selectedCourse.getCourseId(),
                        titleField.getText(),
                        descriptionField.getText(),
                        dueDate,
                        totalPoints,
                        selectedCourse.getTitle()
                    );
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid values for all fields.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(assignment -> {
            if (assignmentService.addAssignment(assignment)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Assignment created successfully!");
                loadDashboardData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create assignment.");
            }
        });
    }

    @FXML
    private void showGradeSubmissions() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session not initialized. Please log in again.");
            return;
        }
        Assignment selectedAssignment = assignmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an assignment first.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Grade Submissions");
        dialog.setHeaderText("Grade submissions for " + selectedAssignment.getTitle());
        dialog.getDialogPane().setPrefSize(800, 600);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Create the submissions table
        TableView<Submission> submissionsTable = new TableView<>();
        
        TableColumn<Submission, String> studentNameCol = new TableColumn<>("Student");
        studentNameCol.setCellValueFactory(cellData -> {
            User student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student.getFirstName() + " " + student.getLastName());
        });
        
        TableColumn<Submission, String> submittedAtCol = new TableColumn<>("Submitted At");
        submittedAtCol.setCellValueFactory(cellData -> {
            LocalDateTime submittedAt = cellData.getValue().getSubmittedAt();
            return new SimpleStringProperty(submittedAt != null ? 
                submittedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Not submitted");
        });
        
        TableColumn<Submission, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            boolean isGraded = cellData.getValue().isGraded();
            return new SimpleStringProperty(isGraded ? "GRADED" : "SUBMITTED");
        });
        
        TableColumn<Submission, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(cellData -> {
            double score = cellData.getValue().getScore();
            return new SimpleStringProperty(score > 0 ? String.valueOf(score) : "Not graded");
        });
        
        submissionsTable.getColumns().addAll(studentNameCol, submittedAtCol, statusCol, gradeCol);
        
        // Add a button to view submission details
        Button viewButton = new Button("View Submission");
        viewButton.setOnAction(e -> {
            Submission selectedSubmission = submissionsTable.getSelectionModel().getSelectedItem();
            if (selectedSubmission != null) {
                showSubmissionDetails(selectedSubmission);
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a submission to view.");
            }
        });

        // Add a button to grade submission
        Button gradeButton = new Button("Grade Submission");
        gradeButton.setOnAction(e -> {
            Submission selectedSubmission = submissionsTable.getSelectionModel().getSelectedItem();
            if (selectedSubmission != null) {
                showGradeDialog(selectedSubmission);
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a submission to grade.");
            }
        });

        HBox buttonBox = new HBox(10, viewButton, gradeButton);
        buttonBox.setStyle("-fx-alignment: center;");

        content.getChildren().addAll(submissionsTable, buttonBox);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Load submissions
        try {
            List<Submission> submissions = submissionService.getSubmissionsByAssignment(selectedAssignment.getAssignmentId());
            submissionsTable.setItems(FXCollections.observableArrayList(submissions));
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load submissions: " + ex.getMessage());
        }

        dialog.showAndWait();
    }

    private void showSubmissionDetails(Submission submission) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Submission Details");
        dialog.setHeaderText("Submission by " + submission.getStudent().getFirstName() + " " + 
                           submission.getStudent().getLastName());
        dialog.getDialogPane().setPrefSize(600, 400);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Display submission content
        TextArea contentArea = new TextArea(submission.getContent());
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(10);

        // Display attachments if any
        VBox attachmentsBox = new VBox(5);
        attachmentsBox.setPadding(new Insets(10));
        Label attachmentsLabel = new Label("Attachments:");
        attachmentsBox.getChildren().add(attachmentsLabel);

        try {
            List<SubmissionAttachment> attachments = submissionService.getSubmissionAttachments(submission.getId());
            for (SubmissionAttachment attachment : attachments) {
                Hyperlink link = new Hyperlink(attachment.getFileName());
                link.setOnAction(e -> {
                    // TODO: Implement file download
                    showAlert(Alert.AlertType.INFORMATION, "Download", 
                            "File download functionality will be implemented here.");
                });
                attachmentsBox.getChildren().add(link);
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load attachments: " + ex.getMessage());
        }

        content.getChildren().addAll(
            new Label("Submitted at: " + submission.getSubmittedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))),
            new Label("Status: " + (submission.isGraded() ? "GRADED" : "SUBMITTED")),
            new Label("Content:"),
            contentArea,
            attachmentsBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showGradeDialog(Submission submission) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Grade Submission");
        dialog.setHeaderText("Grade submission by " + submission.getStudent().getFirstName() + " " + 
                           submission.getStudent().getLastName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField gradeField = new TextField();
        gradeField.setPromptText("Enter grade (0-100)");
        if (submission.getScore() > 0) {
            gradeField.setText(String.valueOf(submission.getScore()));
        }

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Enter feedback");
        feedbackArea.setPrefRowCount(3);
        if (submission.getFeedback() != null) {
            feedbackArea.setText(submission.getFeedback());
        }

        grid.add(new Label("Grade:"), 0, 0);
        grid.add(gradeField, 1, 0);
        grid.add(new Label("Feedback:"), 0, 1);
        grid.add(feedbackArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType gradeButtonType = new ButtonType("Submit Grade", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(gradeButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == gradeButtonType) {
                try {
                    int grade = Integer.parseInt(gradeField.getText());
                    if (grade < 0 || grade > 100) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Grade must be between 0 and 100");
                        return false;
                    }
                    submission.setScore(grade);
                    submission.setFeedback(feedbackArea.getText());
                    submission.grade(grade, feedbackArea.getText(), currentUser);
                    
                    if (submissionService.updateSubmission(submission)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Grade submitted successfully!");
                        return true;
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit grade.");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid grade (0-100)");
                    return false;
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + ex.getMessage());
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait();
    }

    @FXML
    private void showEnrolledStudents() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session not initialized. Please log in again.");
            return;
        }
        Course selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a course first.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Enrolled Students");
        dialog.setHeaderText("Students enrolled in " + selectedCourse.getTitle());

        TableView<Student> studentsTable = new TableView<>();
        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<Student, String> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            // Calculate progress based on grades for this specific course
            String query = """
                SELECT AVG(
                    CASE 
                        WHEN s.grade IS NOT NULL THEN 
                            CAST(s.grade AS DECIMAL) / 100
                        ELSE 0.0
                    END
                ) as student_progress
                FROM submissions s
                JOIN assignments a ON s.assignment_id = a.assignment_id
                WHERE a.course_id = ? AND s.student_id = ?
            """;
            
            double progress = 0.0;
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selectedCourse.getCourseId());
                pstmt.setInt(2, student.getId());
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    progress = rs.getDouble("student_progress") * 100; // Convert to percentage
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return new SimpleStringProperty(String.format("%.1f%%", progress));
        });

        studentsTable.getColumns().addAll(nameCol, emailCol, progressCol);
        
        List<Student> students = studentService.getStudentsByCourse(selectedCourse.getCourseId());
        studentsTable.setItems(FXCollections.observableArrayList(students));

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().add(studentsTable);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Learning Management System");
        alert.setContentText("Version 1.0\nDeveloped for educational institutions");
        alert.showAndWait();
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 