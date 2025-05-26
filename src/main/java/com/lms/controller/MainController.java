package com.lms.controller;

import com.lms.model.User;
import javafx.animation.FadeTransition;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.geometry.Insets;

public class MainController {
    private ProgressBar courseProgress;
    private ProgressIndicator progressIndicator;
    private VBox contentBox;
    private Pagination pagination;
    private Button animatedButton;
    private TabPane tabPane;
    private User currentUser;

    private CourseController courseController;
    private AssignmentController assignmentController;
    private static final int ITEMS_PER_PAGE = 10;
    private static final int TOTAL_ITEMS = 20;

    public MainController() {
        initializeControllers();
        initializeComponents();
    }

    private void initializeComponents() {
        // Initialize components
        courseProgress = new ProgressBar(0.0);
        progressIndicator = new ProgressIndicator(0.0);
        contentBox = new VBox(5);
        pagination = new Pagination();
        animatedButton = new Button("Animated Button");
        tabPane = new TabPane();

        // Setup components
        setupPagination();
        setupProgressIndicators();
        setupAnimatedButton();
        setupTabPane();
    }

    private void initializeControllers() {
        courseController = new CourseController();
        assignmentController = new AssignmentController();
    }

    private void setupTabPane() {
        // Courses Tab
        Tab coursesTab = new Tab("Courses");
        coursesTab.setContent(courseController.getCourseView());
        coursesTab.setClosable(false);

        // Assignments Tab
        Tab assignmentsTab = new Tab("Assignments");
        assignmentsTab.setContent(assignmentController.getAssignmentView());
        assignmentsTab.setClosable(false);

        // Add tabs to tab pane
        tabPane.getTabs().addAll(coursesTab, assignmentsTab);
    }

    private void setupPagination() {
        pagination.setPageCount((int) Math.ceil((double) TOTAL_ITEMS / ITEMS_PER_PAGE));
        pagination.setPageFactory(this::createPage);
    }

    private VBox createPage(int pageIndex) {
        VBox page = new VBox(5);
        page.setPadding(new Insets(10));
        page.getChildren().add(tabPane);
        return page;
    }

    private void setupProgressIndicators() {
        // Simulate progress updates
        new Thread(() -> {
            for (double progress = 0.0; progress <= 1.0; progress += 0.1) {
                final double currentProgress = progress;
                javafx.application.Platform.runLater(() -> {
                    courseProgress.setProgress(currentProgress);
                    progressIndicator.setProgress(currentProgress);
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private void setupAnimatedButton() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), animatedButton);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.3);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();

        animatedButton.setOnAction(e -> handleAnimatedButton());
    }

    private void handleAnimatedButton() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Button Clicked");
        alert.setHeaderText(null);
        alert.setContentText("You clicked the animated button!");
        alert.showAndWait();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        courseController.currentUser = user;
        courseController.loadCourses();
    }

    // Getters for components
    public ProgressBar getCourseProgress() {
        return courseProgress;
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public VBox getContentBox() {
        return contentBox;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public Button getAnimatedButton() {
        return animatedButton;
    }

    public TabPane getTabPane() {
        return tabPane;
    }
} 