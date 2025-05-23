package com.lms.controller;

import com.lms.model.Assignment;
import com.lms.model.User;
import com.lms.util.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import java.sql.SQLException;
import javafx.geometry.Insets;

public class StudentAssignmentsController {
    @FXML private TableView<Assignment> assignmentsTable;
    @FXML private TableColumn<Assignment, String> courseColumn;
    @FXML private TableColumn<Assignment, String> titleColumn;
    @FXML private TableColumn<Assignment, String> dueDateColumn;
    @FXML private TableColumn<Assignment, String> statusColumn;
    @FXML private TableColumn<Assignment, String> gradeColumn;
    @FXML private TableColumn<Assignment, Void> actionsColumn;

    private User currentUser;
    private ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            setupTable();
            loadAssignments();
        }
    }

    private void setupTable() {
        // Course column
        courseColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        
        // Title column
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Due date column
        dueDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDueDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "Not set");
        });
        
        // Status column
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Grade column
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        
        // Actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button submitButton = new Button("Submit");
            private final Button viewButton = new Button("View");
            private final HBox buttons = new HBox(5, submitButton, viewButton);
            
            {
                submitButton.setOnAction(e -> {
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    showSubmitDialog(assignment);
                });
                
                viewButton.setOnAction(e -> {
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    showAssignmentDetails(assignment);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        assignmentsTable.setItems(assignments);
    }

    @FXML
    private void refreshAssignments() {
        if (currentUser == null) {
            showError("Error", "User session not initialized. Please log in again.");
            return;
        }
        loadAssignments();
    }

    private void loadAssignments() {
        if (currentUser == null) {
            showError("Error", "User session not initialized. Please log in again.");
            return;
        }

        try {
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
                
                assignments.clear();
                while (rs.next()) {
                    Assignment assignment = new Assignment(
                        rs.getInt("assignment_id"),
                        rs.getInt("course_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getTimestamp("due_date").toLocalDateTime(),
                        rs.getInt("total_points"),
                        rs.getString("course_name")
                    );
                    assignment.setStatus(rs.getString("status"));
                    assignment.setGrade(rs.getString("grade"));
                    assignments.add(assignment);
                }
                
                assignmentsTable.setItems(assignments);
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load assignments: " + e.getMessage());
        }
    }

    private void showSubmitDialog(Assignment assignment) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Assignment File");
        File file = fileChooser.showOpenDialog(assignmentsTable.getScene().getWindow());
        
        if (file != null) {
            try {
                String query = "INSERT INTO submissions (assignment_id, student_id, content, submitted_at) " +
                             "VALUES (?, ?, ?, ?)";
                
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    
                    pstmt.setInt(1, assignment.getAssignmentId());
                    pstmt.setInt(2, currentUser.getUserId());
                    pstmt.setString(3, file.getAbsolutePath());
                    pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    
                    pstmt.executeUpdate();
                    
                    // Update assignment status
                    assignment.setStatus("SUBMITTED");
                    assignmentsTable.refresh();
                    
                    showInfo("Success", "Assignment submitted successfully!");
                }
            } catch (SQLException e) {
                showError("Database Error", "Failed to submit assignment: " + e.getMessage());
            }
        }
    }

    private void showAssignmentDetails(Assignment assignment) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assignment Details");
        dialog.setHeaderText(assignment.getTitle());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        content.getChildren().addAll(
            new Label("Course: " + assignment.getCourseName()),
            new Label("Description: " + assignment.getDescription()),
            new Label("Due Date: " + assignment.getDueDate().format(dateFormatter)),
            new Label("Status: " + assignment.getStatus()),
            new Label("Grade: " + (assignment.getGrade() != null ? assignment.getGrade() : "Not graded"))
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    private void backToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentDashboard.fxml"));
            Parent root = loader.load();
            
            StudentDashboardController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) assignmentsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showError("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 