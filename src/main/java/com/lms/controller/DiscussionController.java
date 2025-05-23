package com.lms.controller;

import com.lms.model.Comment;
import com.lms.model.Course;
import com.lms.model.Discussion;
import com.lms.model.User;
import com.lms.util.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DiscussionController {
    private ObservableList<Discussion> discussions;
    private ListView<Discussion> discussionListView;
    private TextField searchField;
    private VBox discussionDetailsBox;
    private Course currentCourse;
    private User currentUser;

    public DiscussionController() {
        discussions = FXCollections.observableArrayList();
        initializeComponents();
    }

    private void initializeComponents() {
        // Initialize ListView
        discussionListView = new ListView<>(discussions);
        discussionListView.setCellFactory(lv -> new ListCell<Discussion>() {
            @Override
            protected void updateItem(Discussion discussion, boolean empty) {
                super.updateItem(discussion, empty);
                if (empty || discussion == null) {
                    setText(null);
                } else {
                    setText(discussion.getTitle() + " (by " + discussion.getAuthor().getUsername() + ")");
                }
            }
        });

        // Initialize search field
        searchField = new TextField();
        searchField.setPromptText("Search discussions...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterDiscussions(newVal));

        // Initialize discussion details box
        discussionDetailsBox = new VBox(10);
        discussionDetailsBox.setPadding(new Insets(10));
    }

    public VBox getDiscussionView(Course course, User user) {
        this.currentCourse = course;
        this.currentUser = user;
        discussions.clear();
        if (currentCourse != null) {
            // TODO: Load discussions from database for currentCourse
        }

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Add search field
        root.getChildren().add(searchField);

        // Create split pane for list and details
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(discussionListView, discussionDetailsBox);
        splitPane.setDividerPositions(0.3);

        root.getChildren().add(splitPane);

        // Add discussion selection listener
        discussionListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showDiscussionDetails(newVal));

        return root;
    }

    private void filterDiscussions(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            discussionListView.setItems(discussions);
        } else {
            ObservableList<Discussion> filteredList = FXCollections.observableArrayList();
            for (Discussion discussion : discussions) {
                if (discussion.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                    discussion.getContent().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(discussion);
                }
            }
            discussionListView.setItems(filteredList);
        }
    }

    private void showDiscussionDetails(Discussion discussion) {
        if (discussion == null) {
            discussionDetailsBox.getChildren().clear();
            return;
        }

        discussionDetailsBox.getChildren().clear();

        // Discussion title
        Label titleLabel = new Label(discussion.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        discussionDetailsBox.getChildren().add(titleLabel);

        // Author and date
        Label authorLabel = new Label("Posted by " + discussion.getAuthor().getUsername() + " on " +
            discussion.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        discussionDetailsBox.getChildren().add(authorLabel);

        // Discussion content
        Label contentLabel = new Label(discussion.getContent());
        contentLabel.setWrapText(true);
        discussionDetailsBox.getChildren().add(contentLabel);

        // Comments section
        Label commentsLabel = new Label("Comments (" + discussion.getComments().size() + ")");
        commentsLabel.setStyle("-fx-font-weight: bold;");
        discussionDetailsBox.getChildren().add(commentsLabel);

        // Comments list
        VBox commentsBox = new VBox(5);
        for (var comment : discussion.getComments()) {
            HBox commentBox = new HBox(10);
            Label commentAuthor = new Label(comment.getAuthor().getUsername() + ": ");
            commentAuthor.setStyle("-fx-font-weight: bold;");
            Label commentContent = new Label(comment.getContent());
            commentContent.setWrapText(true);
            commentBox.getChildren().addAll(commentAuthor, commentContent);
            commentsBox.getChildren().add(commentBox);
        }
        discussionDetailsBox.getChildren().add(commentsBox);

        // Add comment section
        HBox addCommentBox = new HBox(10);
        TextField commentField = new TextField();
        commentField.setPromptText("Add a comment...");
        Button postButton = new Button("Post");
        addCommentBox.getChildren().addAll(commentField, postButton);
        discussionDetailsBox.getChildren().add(addCommentBox);

        // Add button action
        postButton.setOnAction(e -> addComment(discussion, commentField.getText()));
    }

    private void addComment(Discussion discussion, String content) {
        if (content == null || content.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Comment");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a comment before posting.");
            alert.showAndWait();
            return;
        }

        if (currentUser != null) {
            Comment comment = new Comment(0, content, currentUser);
            discussion.addComment(comment);
            showDiscussionDetails(discussion); // Refresh the view
        }
    }

    public void addDiscussion(Discussion discussion) {
        if (discussion == null || currentUser == null || currentCourse == null) return;

        try {
            // Add discussion to database
            String query = "INSERT INTO discussions (title, content, author_id, course_id) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, discussion.getTitle());
                pstmt.setString(2, discussion.getContent());
                pstmt.setInt(3, currentUser.getUserId());
                pstmt.setInt(4, currentCourse.getCourseId());
                pstmt.executeUpdate();
            }

            // Add to local list
            discussions.add(discussion);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Discussion Created");
            alert.setHeaderText(null);
            alert.setContentText("Your discussion has been created successfully.");
            alert.showAndWait();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Creation Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to create discussion: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void removeDiscussion(Discussion discussion) {
        discussions.remove(discussion);
    }
} 