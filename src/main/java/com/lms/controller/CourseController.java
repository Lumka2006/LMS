package com.lms.controller;

import com.lms.model.Course;
import com.lms.model.User;
import com.lms.util.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

public class CourseController {
    public User currentUser;
    private TableView<Course> courseTable;
    private ObservableList<Course> courses;

    public CourseController() {
        courses = FXCollections.observableArrayList();
        initializeCourseTable();
    }

    private void initializeCourseTable() {
        courseTable = new TableView<>();
        
        TableColumn<Course, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        
        TableColumn<Course, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        
        TableColumn<Course, Double> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(cellData -> cellData.getValue().progressProperty().asObject());
        
        courseTable.getColumns().add(titleCol);
        courseTable.getColumns().add(descriptionCol);
        courseTable.getColumns().add(progressCol);
        courseTable.setItems(courses);
    }

    public VBox getCourseView() {
        VBox view = new VBox(10);
        view.setPadding(new Insets(10));
        
        // Add course button (only for instructors and admins)
        if (currentUser != null && (currentUser.getRole().equals("INSTRUCTOR") || currentUser.getRole().equals("ADMIN"))) {
            Button addCourseBtn = new Button("Add New Course");
            addCourseBtn.setOnAction(e -> showAddCourseDialog());
            view.getChildren().add(addCourseBtn);
        }
        
        view.getChildren().add(courseTable);
        loadCourses();
        return view;
    }

    public void loadCourses() {
        courses.clear();
        if (currentUser == null) {
            return;
        }
        
        String query = currentUser.getRole().equals("STUDENT") ?
            "SELECT c.*, e.progress FROM courses c " +
            "LEFT JOIN enrollments e ON c.id = e.course_id AND e.student_id = ?" :
            "SELECT * FROM courses WHERE teacher_id = ?";
            
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp startTimestamp = rs.getTimestamp("start_date");
                    java.sql.Timestamp endTimestamp = rs.getTimestamp("end_date");
                    
                    LocalDateTime startDate = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
                    LocalDateTime endDate = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;
                    
                    Course course = new Course(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("description"),
                        currentUser.getUserId(),
                        currentUser.getName(),
                        startDate,
                        endDate
                    );
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading courses", e.getMessage());
        }
    }

    private void showAddCourseDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Add New Course");
        dialog.setHeaderText("Enter Course Details");

        TextField codeField = new TextField();
        codeField.setPromptText("Course Code");
        TextField titleField = new TextField();
        titleField.setPromptText("Course Title");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Course Description");
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        dialog.getDialogPane().setContent(new VBox(10, 
            new Label("Code:"), codeField,
            new Label("Title:"), titleField,
            new Label("Description:"), descriptionArea,
            new Label("Start Date:"), startDatePicker,
            new Label("End Date:"), endDatePicker
        ));

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                
                LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
                LocalDateTime endDateTime = endDate != null ? endDate.atStartOfDay() : null;
                
                return new Course(
                    0,
                    codeField.getText(),
                    titleField.getText(),
                    descriptionArea.getText(),
                    currentUser.getUserId(),
                    currentUser.getName(),
                    startDateTime,
                    endDateTime
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(course -> {
            addCourse(course);
        });
    }

    private void addCourse(Course course) {
        String query = "INSERT INTO courses (code, title, description, teacher_id, teacher_name, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setString(3, course.getDescription());
            pstmt.setInt(4, currentUser.getUserId());
            pstmt.setString(5, currentUser.getName());
            pstmt.setTimestamp(6, course.getStartDate() != null ? 
                java.sql.Timestamp.valueOf(course.getStartDate()) : null);
            pstmt.setTimestamp(7, course.getEndDate() != null ? 
                java.sql.Timestamp.valueOf(course.getEndDate()) : null);
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    course.setCourseId(rs.getInt(1));
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error adding course", e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 