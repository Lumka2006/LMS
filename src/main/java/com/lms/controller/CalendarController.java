package com.lms.controller;

import com.lms.model.Calendar;
import com.lms.model.Event;
import com.lms.model.EventType;
import com.lms.model.User;
import com.lms.util.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CalendarController {
    private ObservableList<Event> events;
    private ListView<Event> eventListView;
    private DatePicker datePicker;
    private VBox eventDetailsBox;
    private User currentUser;
    private Calendar calendar;

    public CalendarController() {
        events = FXCollections.observableArrayList();
        initializeComponents();
    }

    private void initializeComponents() {
        // Initialize ListView
        eventListView = new ListView<>(events);
        eventListView.setCellFactory(lv -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else {
                    setText(event.getTitle() + " (" + 
                           event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + ")");
                }
            }
        });

        // Initialize date picker
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setOnAction(e -> loadEventsForDate(datePicker.getValue()));

        // Initialize event details box
        eventDetailsBox = new VBox(10);
        eventDetailsBox.setPadding(new Insets(10));
    }

    public VBox getCalendarView(User user) {
        this.currentUser = user;
        this.calendar = new Calendar(1, user); // TODO: Load calendar from database
        events.clear();

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Add date picker
        root.getChildren().add(datePicker);

        // Create split pane for list and details
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(eventListView, eventDetailsBox);
        splitPane.setDividerPositions(0.3);

        root.getChildren().add(splitPane);

        // Add event selection listener
        eventListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showEventDetails(newVal));

        // Add new event button
        Button addEventButton = new Button("Add New Event");
        addEventButton.setOnAction(e -> showAddEventDialog());
        root.getChildren().add(addEventButton);

        return root;
    }

    private void loadEventsForDate(LocalDate date) {
        events.clear();
        if (calendar != null && currentUser != null) {
            events.addAll(calendar.getEventsForDate(date.atStartOfDay()));
            // TODO: Load events from database for currentUser
        }
    }

    private void showEventDetails(Event event) {
        if (event == null) {
            eventDetailsBox.getChildren().clear();
            return;
        }

        eventDetailsBox.getChildren().clear();

        // Event title
        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        eventDetailsBox.getChildren().add(titleLabel);

        // Event description
        Label descriptionLabel = new Label(event.getDescription());
        descriptionLabel.setWrapText(true);
        eventDetailsBox.getChildren().add(descriptionLabel);

        // Event time
        Label timeLabel = new Label("Time: " + 
            event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
            event.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        eventDetailsBox.getChildren().add(timeLabel);

        // Event location
        Label locationLabel = new Label("Location: " + event.getLocation());
        eventDetailsBox.getChildren().add(locationLabel);

        // Event type
        Label typeLabel = new Label("Type: " + event.getType());
        eventDetailsBox.getChildren().add(typeLabel);

        // Add buttons for event actions
        HBox buttonBox = new HBox(10);
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        buttonBox.getChildren().addAll(editButton, deleteButton);
        eventDetailsBox.getChildren().add(buttonBox);

        // Add button actions
        editButton.setOnAction(e -> showEditEventDialog(event));
        deleteButton.setOnAction(e -> deleteEvent(event));
    }

    private void showAddEventDialog() {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Add New Event");
        dialog.setHeaderText("Enter event details");

        // Create the custom dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextField titleField = new TextField();
        titleField.setPromptText("Event Title");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Event Description");
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        ComboBox<EventType> typeComboBox = new ComboBox<>(FXCollections.observableArrayList(EventType.values()));
        typeComboBox.setPromptText("Event Type");

        content.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), descriptionField,
            new Label("Location:"), locationField,
            new Label("Type:"), typeComboBox
        );

        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Convert the result to an event when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (titleField.getText().isEmpty() || typeComboBox.getValue() == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Title and type are required fields.");
                    alert.showAndWait();
                    return null;
                }

                Event newEvent = new Event(
                    0, // ID will be set by database
                    titleField.getText(),
                    descriptionField.getText(),
                    datePicker.getValue().atTime(12, 0), // Default to noon
                    datePicker.getValue().atTime(13, 0), // Default to 1 hour duration
                    locationField.getText(),
                    typeComboBox.getValue()
                );
                addEvent(newEvent);
                return newEvent;
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditEventDialog(Event event) {
        // TODO: Implement edit event dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Event");
        alert.setHeaderText(null);
        alert.setContentText("Event editing functionality will be implemented here.");
        alert.showAndWait();
    }

    private void deleteEvent(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Event");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this event?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            events.remove(event);
            if (calendar != null) {
                calendar.removeEvent(event);
            }
        }
    }

    public void addEvent(Event event) {
        if (event == null || currentUser == null) return;

        try {
            // Add event to database
            String query = "INSERT INTO events (title, description, start_time, end_time, location, type, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, event.getTitle());
                pstmt.setString(2, event.getDescription());
                pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(event.getStartTime()));
                pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(event.getEndTime()));
                pstmt.setString(5, event.getLocation());
                pstmt.setString(6, event.getType().toString());
                pstmt.setInt(7, currentUser.getUserId());
                pstmt.executeUpdate();
            }

            // Add to local list
            events.add(event);
            if (calendar != null) {
                calendar.addEvent(event);
            }

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Event Created");
            alert.setHeaderText(null);
            alert.setContentText("Your event has been created successfully.");
            alert.showAndWait();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Creation Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to create event: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 