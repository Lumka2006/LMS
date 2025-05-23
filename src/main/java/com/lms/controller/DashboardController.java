package com.lms.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.effect.*;
import javafx.scene.paint.Color;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.Popup;
import com.lms.model.Student;
import com.lms.util.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {
    @FXML private ListView<Student> studentList;
    @FXML private ProgressBar progressBar;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button fadeButton;
    @FXML private Button glowButton;
    @FXML private Button rotateButton;
    @FXML private Label pageLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalAssignmentsLabel;
    @FXML private Button prevBtn, nextBtn;
    @FXML private LineChart<Number, Number> progressChart;
    @FXML private ListView<String> activityList;
    @FXML private Button notificationBtn;
    @FXML private TabPane mainTabPane;
    @FXML private VBox classesContainer;
    @FXML private VBox assignmentsContainer;
    @FXML private VBox announcementsContainer;
    @FXML private Button profileBtn;
    @FXML private Button themeToggleBtn;
    @FXML private Label activeStudentsLabel;

    private FadeTransition fadeTransition;
    private RotateTransition rotateTransition;
    private boolean isDarkTheme = false;
    private Random random = new Random();
    private ObservableList<Student> students = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupClassroomStructure();
        initializeStudentList();
        initializeProgress();
        setupEffects();
        setupPagination();
        initializeProgressChart();
        initializeActivityList();
        setupNotificationButton();
        setupProfileButton();
        setupThemeToggle();
        loadStatistics();
    }

    private void setupClassroomStructure() {
        // Setup Classes Tab
        Tab classesTab = new Tab("Classes");
        classesTab.setClosable(false);
        VBox classesContent = new VBox(10);
        classesContent.setPadding(new Insets(10));
        
        // Add class cards
        for (int i = 1; i <= 5; i++) {
            VBox classCard = createClassCard("Class " + i, "Subject " + i, "Teacher " + i);
            classesContent.getChildren().add(classCard);
        }
        
        ScrollPane classesScroll = new ScrollPane(classesContent);
        classesScroll.setFitToWidth(true);
        classesTab.setContent(classesScroll);

        // Setup Assignments Tab
        Tab assignmentsTab = new Tab("Assignments");
        assignmentsTab.setClosable(false);
        VBox assignmentsContent = new VBox(10);
        assignmentsContent.setPadding(new Insets(10));
        
        // Add assignment cards
        for (int i = 1; i <= 5; i++) {
            VBox assignmentCard = createAssignmentCard(
                "Assignment " + i,
                "Due: " + (i + 1) + " days",
                "Class " + (i % 3 + 1)
            );
            assignmentsContent.getChildren().add(assignmentCard);
        }
        
        ScrollPane assignmentsScroll = new ScrollPane(assignmentsContent);
        assignmentsScroll.setFitToWidth(true);
        assignmentsTab.setContent(assignmentsScroll);

        // Setup Announcements Tab
        Tab announcementsTab = new Tab("Announcements");
        announcementsTab.setClosable(false);
        VBox announcementsContent = new VBox(10);
        announcementsContent.setPadding(new Insets(10));
        
        // Add announcement cards
        for (int i = 1; i <= 5; i++) {
            VBox announcementCard = createAnnouncementCard(
                "Announcement " + i,
                "Posted " + i + " days ago",
                "Important information for all students"
            );
            announcementsContent.getChildren().add(announcementCard);
        }
        
        ScrollPane announcementsScroll = new ScrollPane(announcementsContent);
        announcementsScroll.setFitToWidth(true);
        announcementsTab.setContent(announcementsScroll);

        // Setup Calendar Tab
        Tab calendarTab = new Tab("Calendar");
        calendarTab.setClosable(false);
        VBox calendarContent = new VBox(10);
        calendarContent.setPadding(new Insets(10));
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select Date");
        
        ListView<String> eventList = new ListView<>();
        eventList.getItems().addAll(
            "Class: Java Programming - 10:00 AM",
            "Assignment Due: Database Design - 11:59 PM",
            "Group Meeting: Project Discussion - 2:00 PM",
            "Quiz: Web Development - 3:30 PM"
        );
        
        calendarContent.getChildren().addAll(datePicker, eventList);
        calendarTab.setContent(calendarContent);

        // Setup Materials Tab
        Tab materialsTab = new Tab("Materials");
        materialsTab.setClosable(false);
        VBox materialsContent = new VBox(10);
        materialsContent.setPadding(new Insets(10));
        
        ListView<String> materialsList = new ListView<>();
        materialsList.getItems().addAll(
            "Java Programming Textbook (PDF)",
            "Database Design Slides (PPT)",
            "Web Development Tutorial (Video)",
            "Project Guidelines (DOC)",
            "Sample Code Repository (ZIP)"
        );
        
        materialsContent.getChildren().add(materialsList);
        materialsTab.setContent(materialsContent);

        // Add all tabs to main tab pane
        mainTabPane.getTabs().addAll(classesTab, assignmentsTab, announcementsTab, calendarTab, materialsTab);
    }

    private VBox createClassCard(String title, String subject, String teacher) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label subjectLabel = new Label(subject);
        Label teacherLabel = new Label("Teacher: " + teacher);
        
        HBox buttonBox = new HBox(10);
        Button viewButton = new Button("View Class");
        Button materialsButton = new Button("Materials");
        viewButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white;");
        materialsButton.setStyle("-fx-background-color: #34a853; -fx-text-fill: white;");
        
        buttonBox.getChildren().addAll(viewButton, materialsButton);
        
        card.getChildren().addAll(titleLabel, subjectLabel, teacherLabel, buttonBox);
        return card;
    }

    private VBox createAssignmentCard(String title, String dueDate, String className) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label dueDateLabel = new Label(dueDate);
        Label classLabel = new Label(className);
        
        HBox buttonBox = new HBox(10);
        Button submitButton = new Button("Submit Assignment");
        Button viewButton = new Button("View Details");
        submitButton.setStyle("-fx-background-color: #0f9d58; -fx-text-fill: white;");
        viewButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white;");
        
        buttonBox.getChildren().addAll(submitButton, viewButton);
        
        card.getChildren().addAll(titleLabel, dueDateLabel, classLabel, buttonBox);
        return card;
    }

    private VBox createAnnouncementCard(String title, String date, String content) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label dateLabel = new Label(date);
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        
        HBox buttonBox = new HBox(10);
        Button viewButton = new Button("View Full Announcement");
        Button shareButton = new Button("Share");
        viewButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white;");
        shareButton.setStyle("-fx-background-color: #34a853; -fx-text-fill: white;");
        
        buttonBox.getChildren().addAll(viewButton, shareButton);
        
        card.getChildren().addAll(titleLabel, dateLabel, contentLabel, buttonBox);
        return card;
    }

    private void initializeStudentList() {
        studentList.setItems(students);
        studentList.setCellFactory(lv -> new ListCell<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText(null);
                } else {
                    setText(student.getName() + " (" + student.getEmail() + ")");
                }
            }
        });
        students.addAll(Student.PREDEFINED_STUDENTS);
    }

    private void initializeProgress() {
        progressBar.setProgress(0.5);
        progressIndicator.setProgress(0.5);
    }

    private void setupEffects() {
        // Setup DropShadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.5));
        fadeButton.setEffect(dropShadow);
        
        // Setup continuous fade transition
        fadeTransition = new FadeTransition(Duration.seconds(1), fadeButton);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.3);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
        
        // Setup glow effect
        Glow glow = new Glow();
        glow.setLevel(0.8);
        glowButton.setEffect(glow);
        glowButton.setOnMouseEntered(e -> glow.setLevel(1.0));
        glowButton.setOnMouseExited(e -> glow.setLevel(0.8));
        
        // Setup rotate effect
        rotateTransition = new RotateTransition(Duration.seconds(2), rotateButton);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateButton.setOnMouseEntered(e -> rotateTransition.play());
        rotateButton.setOnMouseExited(e -> rotateTransition.stop());
    }

    private void setupPagination() {
        prevBtn.setOnAction(e -> updatePage(-1));
        nextBtn.setOnAction(e -> updatePage(1));
        updatePageLabel(1);
    }

    @FXML
    private void handleAddStudent() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter Student Details");

        // Create the custom dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextField nameField = new TextField();
        nameField.setPromptText("Student Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Student Email");

        content.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("Email:"), emailField
        );

        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Convert the result to a student when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
                    showError("Validation Error", "Name and email are required fields.");
                    return null;
                }
                return new Student(students.size() + 1, nameField.getText(), emailField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(student -> {
            students.add(student);
            activityList.getItems().add(0, "New student added: " + student.getName());
            
            // Animate new item
            int lastIndex = students.size() - 1;
            studentList.scrollTo(lastIndex);
            studentList.getSelectionModel().select(lastIndex);
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void initializeProgressChart() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Progress");
        
        // Add some sample data
        for (int i = 0; i < 10; i++) {
            series.getData().add(new XYChart.Data<>(i, random.nextDouble() * 100));
        }
        
        progressChart.getData().add(series);
        progressChart.setCreateSymbols(true);
        progressChart.setAnimated(true);
    }

    private void initializeActivityList() {
        activityList.getItems().addAll(
            "New course added: Java Programming",
            "Assignment submitted: Database Design",
            "Quiz completed: Web Development",
            "Course updated: Python Basics",
            "New student enrolled: John Doe"
        );
    }

    private void setupNotificationButton() {
        notificationBtn.setOnAction(e -> {
            // Create a popup notification
            Popup popup = new Popup();
            VBox content = new VBox(10);
            content.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");
            content.getChildren().addAll(
                new Label("New Notification"),
                new Label("You have 3 new messages"),
                new Button("View All")
            );
            popup.getContent().add(content);
            popup.show(notificationBtn.getScene().getWindow());
        });
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleRefresh() {
        loadStatistics();
        // Animate refresh
        RotateTransition refreshAnimation = new RotateTransition(Duration.seconds(0.5), progressIndicator);
        refreshAnimation.setByAngle(360);
        refreshAnimation.play();
    }

    @FXML
    private void handleToggleTheme() {
        isDarkTheme = !isDarkTheme;
        if (isDarkTheme) {
            progressIndicator.getScene().getRoot().getStyleClass().add("dark-theme");
        } else {
            progressIndicator.getScene().getRoot().getStyleClass().remove("dark-theme");
        }
    }

    private void setupProfileButton() {
        profileBtn.setOnAction(e -> showProfileDialog());
    }

    private void showProfileDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("User Profile");
        dialog.setHeaderText("Profile Information");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Add profile information
        content.getChildren().addAll(
            new Label("Name: John Doe"),
            new Label("Role: Student"),
            new Label("Email: john.doe@example.com"),
            new Label("Last Login: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")))
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void setupThemeToggle() {
        themeToggleBtn.setOnAction(e -> toggleTheme());
    }

    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        if (isDarkTheme) {
            mainTabPane.getScene().getRoot().getStyleClass().add("dark-theme");
        } else {
            mainTabPane.getScene().getRoot().getStyleClass().remove("dark-theme");
        }
    }

    private void loadStatistics() {
        try {
            // Load total courses
            int totalCourses = DatabaseUtil.getTotalCourses();
            totalCoursesLabel.setText(String.valueOf(totalCourses));
            
            // Load total assignments
            int totalAssignments = DatabaseUtil.getTotalAssignments();
            totalAssignmentsLabel.setText(String.valueOf(totalAssignments));
            
            // Load active students
            int activeStudents = students.size();
            activeStudentsLabel.setText(String.valueOf(activeStudents));
            
            // Update progress
            double progress = DatabaseUtil.getAverageProgress();
            setProgress(progress);
            
            // Update chart with new data
            updateProgressChart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProgressChart() {
        if (!progressChart.getData().isEmpty()) {
            XYChart.Series<Number, Number> series = progressChart.getData().get(0);
            series.getData().clear();
            
            // Add new random data
            for (int i = 0; i < 10; i++) {
                series.getData().add(new XYChart.Data<>(i, random.nextDouble() * 100));
            }
        }
    }

    private void updatePage(int delta) {
        int currentPage = Integer.parseInt(pageLabel.getText().split(" ")[0]);
        int newPage = Math.max(1, currentPage + delta);
        updatePageLabel(newPage);
    }

    private void updatePageLabel(int page) {
        pageLabel.setText(page + " of 10");
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
        progressIndicator.setProgress(progress);
    }

    public void setStudentList(java.util.List<Student> students) {
        this.students.setAll(students);
    }
} 