package com.lms.controller;

import com.lms.model.Assignment;
import com.lms.model.Course;
import com.lms.model.Submission;
import com.lms.model.User;
import com.lms.service.AssignmentService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.lms.util.DatabaseUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AssignmentController implements Initializable {
    @FXML private TableView<Assignment> assignmentsTable;
    @FXML private TableColumn<Assignment, String> titleColumn;
    @FXML private TableColumn<Assignment, String> dueDateColumn;
    @FXML private TableColumn<Assignment, String> statusColumn;
    @FXML private Label assignmentCountLabel;
    @FXML private ListView<Assignment> assignmentListView;
    @FXML private TextField searchField;
    @FXML private VBox assignmentDetailsBox;
    @FXML private TextArea submissionContent;
    @FXML private ListView<String> attachmentsList;
    @FXML private Button submitButton;
    @FXML private Button attachFileButton;
    @FXML private Label statusLabel;
    
    private User currentUser;
    private Course currentCourse;
    private AssignmentService assignmentService;
    private ObservableList<Assignment> assignments;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private List<String> attachments;
    private Assignment currentAssignment;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        assignmentService = new AssignmentService();
        assignments = FXCollections.observableArrayList();
        attachments = new ArrayList<>();
        
        // Initialize table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dueDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDueDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Set up table selection listener
        assignmentsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showAssignmentDetails(newValue));
        
        assignmentsTable.setItems(assignments);
        
        // Initialize UI components
        submitButton.setOnAction(e -> handleSubmit());
        attachFileButton.setOnAction(e -> handleAttachFile());
        
        // Disable submit button if no content
        submissionContent.textProperty().addListener((obs, oldVal, newVal) -> {
            submitButton.setDisable(newVal.trim().isEmpty());
        });
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            setupTable();
            loadAssignments();
        }
    }

    public void setCourse(Course course) {
        this.currentCourse = course;
        if (currentCourse != null) {
            loadAssignments();
        }
    }

    private void setupTable() {
        // Clear existing columns
        assignmentsTable.getColumns().clear();
        
        // Assignment table columns
        TableColumn<Assignment, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        
        TableColumn<Assignment, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(cellData -> {
            LocalDateTime dueDate = cellData.getValue().getDueDate();
            return new SimpleStringProperty(dueDate != null ? dueDate.format(dateFormatter) : "Not set");
        });
        dueDateCol.setPrefWidth(150);
        
        TableColumn<Assignment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        assignmentsTable.getColumns().addAll(titleCol, dueDateCol, statusCol);
        assignmentsTable.setItems(assignments);
    }

    private void loadAssignments() {
        if (currentUser == null || currentCourse == null) {
            System.out.println("Cannot load assignments - user or course is null");
            return;
        }
        
        String query = "SELECT a.*, c.name as course_name " +
                      "FROM assignments a " +
                      "JOIN courses c ON a.course_id = c.course_id " +
                      "WHERE a.course_id = ? " +
                      "ORDER BY a.due_date";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, currentCourse.getCourseId());
            ResultSet rs = pstmt.executeQuery();
            
            assignments.clear();
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
                assignments.add(assignment);
            }
            
            assignmentCountLabel.setText("Total Assignments: " + assignments.size());
            
        } catch (SQLException e) {
            System.out.println("Error loading assignments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterAssignments(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            assignmentsTable.setItems(assignments);
            return;
        }

            ObservableList<Assignment> filteredList = FXCollections.observableArrayList();
            for (Assignment assignment : assignments) {
                if (assignment.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                    assignment.getDescription().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(assignment);
                }
            }
        assignmentsTable.setItems(filteredList);
    }

    private void showAssignmentDetails(Assignment assignment) {
        if (assignment == null) {
            assignmentDetailsBox.getChildren().clear();
            return;
        }

        assignmentDetailsBox.getChildren().clear();

        // Assignment title
        Label titleLabel = new Label(assignment.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        assignmentDetailsBox.getChildren().add(titleLabel);

        // Assignment description
        Label descriptionLabel = new Label(assignment.getDescription());
        descriptionLabel.setWrapText(true);
        assignmentDetailsBox.getChildren().add(descriptionLabel);

        // Due date
        Label dueDateLabel = new Label("Due Date: " + 
            (assignment.getDueDate() != null ? assignment.getDueDate().format(dateFormatter) : "Not set"));
        assignmentDetailsBox.getChildren().add(dueDateLabel);

        // Max score
        Label maxScoreLabel = new Label("Maximum Score: " + assignment.getTotalPoints());
        assignmentDetailsBox.getChildren().add(maxScoreLabel);

        // Submission status
        Label submissionStatusLabel = new Label("Status: " + 
            (assignment.isSubmitted(currentUser) ? "Submitted" : "Not Submitted"));
        assignmentDetailsBox.getChildren().add(submissionStatusLabel);

        // Add submission section
        VBox submissionBox = new VBox(10);
        submissionBox.setPadding(new Insets(10));
        submissionBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        // Text submission area
        submissionContent.setPromptText("Add your work here...");
        submissionContent.setWrapText(true);
        submissionContent.setPrefRowCount(5);
        submissionContent.setPrefWidth(400);

        // File upload section
        VBox fileUploadBox = new VBox(5);
        Label fileLabel = new Label("Attach Files:");
        attachmentsList.setPrefHeight(100);
        
        HBox fileButtons = new HBox(10);
        attachFileButton = new Button("Add File");
        Button removeFileButton = new Button("Remove File");
        
        attachFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File");
            File file = fileChooser.showOpenDialog(assignmentDetailsBox.getScene().getWindow());
            if (file != null) {
                attachments.add(file.getAbsolutePath());
                attachmentsList.getItems().add(file.getName());
            }
        });
        
        removeFileButton.setOnAction(e -> {
            int selectedIndex = attachmentsList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                attachments.remove(selectedIndex);
                attachmentsList.getItems().remove(selectedIndex);
            }
        });
        
        fileButtons.getChildren().addAll(attachFileButton, removeFileButton);
        fileUploadBox.getChildren().addAll(fileLabel, attachmentsList, fileButtons);

        // Submit button
        submitButton = new Button("Submit Assignment");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setOnAction(e -> {
            if (submissionContent.getText().trim().isEmpty() && attachments.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Empty Submission");
                alert.setHeaderText(null);
                alert.setContentText("Please add some text or attach files before submitting.");
                alert.showAndWait();
                return;
            }
            submitAssignment(assignment, submissionContent.getText(), attachments);
            // Refresh the details panel to update the status label
            showAssignmentDetails(assignment);
        });

        // Add all components to submission box
        submissionBox.getChildren().addAll(
            new Label("Your Work:"),
            submissionContent,
            fileUploadBox,
            submitButton
        );

        // Add submission box to main details box
        assignmentDetailsBox.getChildren().add(submissionBox);

        // Add spacing between sections
        assignmentDetailsBox.setSpacing(15);

        // Check if already submitted
        if (assignmentService.isSubmitted(assignment, currentUser)) {
            Submission submission = assignmentService.getSubmission(assignment, currentUser);
            if (submission != null) {
                submissionContent.setText(submission.getContent());
                submissionContent.setEditable(false);
                attachmentsList.getItems().addAll(submission.getAttachments());
                submitButton.setDisable(true);
                attachFileButton.setDisable(true);
                statusLabel.setText("Already submitted on " + submission.getSubmittedAt());
            }
        }
    }

    private void submitAssignment(Assignment assignment, String text, List<String> files) {
        if (assignment == null || currentUser == null) return;

        // Create a dialog for submission
        Dialog<SubmissionData> dialog = new Dialog<>();
        dialog.setTitle("Submit Assignment");
        dialog.setHeaderText("Enter your submission");

        // Set the button types
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Create the submission form
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Text submission area
        submissionContent = new TextArea();
        submissionContent.setPromptText("Enter your assignment submission here...");
        submissionContent.setWrapText(true);
        submissionContent.setPrefRowCount(5);

        // File upload section
        VBox fileUploadBox = new VBox(5);
        Label fileLabel = new Label("Attach Files:");
        attachmentsList = new ListView<>();
        attachmentsList.setPrefHeight(100);
        
        HBox fileButtons = new HBox(10);
        attachFileButton = new Button("Add File");
        Button removeFileButton = new Button("Remove File");
        
        attachFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File");
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                attachments.add(file.getAbsolutePath());
                attachmentsList.getItems().add(file.getName());
            }
        });
        
        removeFileButton.setOnAction(e -> {
            int selectedIndex = attachmentsList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                attachments.remove(selectedIndex);
                attachmentsList.getItems().remove(selectedIndex);
            }
        });
        
        fileButtons.getChildren().addAll(attachFileButton, removeFileButton);
        fileUploadBox.getChildren().addAll(fileLabel, attachmentsList, fileButtons);

        // Add components to content
        content.getChildren().addAll(
            new Label("Submission Text:"),
            submissionContent,
            fileUploadBox
        );

        dialog.getDialogPane().setContent(content);

        // Convert the result to a submission when the submit button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return new SubmissionData(
                    text,
                    new ArrayList<>(attachments)
                );
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(submissionData -> {
            if (submissionData != null && (!submissionData.text.isEmpty() || !submissionData.files.isEmpty())) {
                try {
                    // Create new submission
                    Submission newSubmission = new Submission(
                        assignments.size() + 1,
                        currentUser,
                        assignment,
                        submissionData.text
                    );

                    // Add files to submission
                    for (String filePath : submissionData.files) {
                        // In a real application, you would handle file upload here
                        // For now, we'll just store the file paths
                        newSubmission.addAttachment(filePath);
                    }

                    // Update assignment status
                    assignment.setStatus("SUBMITTED");

                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Submission Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Your assignment has been submitted successfully.");
                    alert.showAndWait();

                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Submission Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to submit assignment: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        });
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

    private void viewFeedback(Assignment assignment) {
        if (!assignment.isSubmitted(currentUser)) {
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
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, assignment.getAssignmentId());
            pstmt.setInt(2, currentUser.getUserId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Label scoreLabel = new Label("Score: " + rs.getInt("score") + "/" + assignment.getTotalPoints());
                    TextArea feedbackArea = new TextArea(rs.getString("feedback"));
                    feedbackArea.setEditable(false);
                    feedbackArea.setWrapText(true);
                    feedbackArea.setPrefRowCount(5);
                    
                    content.getChildren().addAll(scoreLabel, new Label("Feedback:"), feedbackArea);
                }
            }
        } catch (SQLException e) {
            content.getChildren().add(new Label("Error loading feedback: " + e.getMessage()));
        }

        feedbackDialog.getDialogPane().setContent(content);
        feedbackDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        feedbackDialog.showAndWait();
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        if (currentCourse != null) {
            currentCourse.addAssignment(assignment);
        }
    }

    public void removeAssignment(Assignment assignment) {
        assignments.remove(assignment);
    }

    public VBox getAssignmentView() {
        VBox view = new VBox(10);
        view.setPadding(new Insets(10));
        
        // Add assignment button (only for instructors and admins)
        if (currentUser != null && (currentUser.getRole().equals("INSTRUCTOR") || currentUser.getRole().equals("ADMIN"))) {
            Button addAssignmentBtn = new Button("Add New Assignment");
            addAssignmentBtn.setOnAction(e -> showAddAssignmentDialog());
            view.getChildren().add(addAssignmentBtn);
        }
        
        // Add the assignments table
        view.getChildren().add(assignmentsTable);
        
        // Add the assignment details box
        assignmentDetailsBox.setPadding(new Insets(10));
        assignmentDetailsBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");
        view.getChildren().add(assignmentDetailsBox);
        
        // Load assignments
        loadAssignments();
        
        return view;
    }

    private void showAddAssignmentDialog() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session not initialized. Please log in again.");
            return;
        }

        Dialog<Assignment> dialog = new Dialog<>();
        dialog.setTitle("Add New Assignment");
        dialog.setHeaderText("Enter Assignment Details");

        // Create the custom dialog content
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

        // Add buttons
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Convert the result to an Assignment when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
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
                    currentCourse.getCourseId(),
                    titleField.getText(),
                    descriptionField.getText(),
                    dueDate,
                    totalPoints,
                    currentCourse.getTitle()
                );
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(assignment -> {
            if (assignmentService.addAssignment(assignment)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Assignment added successfully!");
                loadAssignments();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add assignment.");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleSubmit() {
        if (currentAssignment == null || currentUser == null) {
            showError("No assignment selected");
            return;
        }
        
        String content = submissionContent.getText().trim();
        if (content.isEmpty()) {
            showError("Please enter your submission content");
            return;
        }
        
        if (assignmentService.submitAssignment(currentAssignment, currentUser, content, attachments)) {
            showSuccess("Assignment submitted successfully");
            submitButton.setDisable(true);
            attachFileButton.setDisable(true);
            submissionContent.setEditable(false);
        } else {
            showError("Failed to submit assignment");
        }
    }

    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Attach");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx")
        );
        
        File file = fileChooser.showOpenDialog(attachFileButton.getScene().getWindow());
        if (file != null) {
            attachments.add(file.getAbsolutePath());
            attachmentsList.getItems().add(file.getName());
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
    }
}
