package com.lms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Module {
    private IntegerProperty id;
    private StringProperty title;
    private StringProperty description;
    private ObjectProperty<Course> course;
    private IntegerProperty order;
    private BooleanProperty isPublished;
    private ObjectProperty<LocalDateTime> startDate;
    private ObjectProperty<LocalDateTime> endDate;
    private List<Content> contents;
    private List<Quiz> quizzes;
    private List<Assignment> assignments;

    public Module(int id, String title, String description, Course course, int order) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.course = new SimpleObjectProperty<>(course);
        this.order = new SimpleIntegerProperty(order);
        this.isPublished = new SimpleBooleanProperty(false);
        this.startDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.endDate = new SimpleObjectProperty<>(LocalDateTime.now().plusWeeks(2));
        this.contents = new ArrayList<>();
        this.quizzes = new ArrayList<>();
        this.assignments = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    public void setDescription(String description) { this.description.set(description); }

    public Course getCourse() { return course.get(); }
    public ObjectProperty<Course> courseProperty() { return course; }
    public void setCourse(Course course) { this.course.set(course); }

    public int getOrder() { return order.get(); }
    public IntegerProperty orderProperty() { return order; }
    public void setOrder(int order) { this.order.set(order); }

    public boolean isPublished() { return isPublished.get(); }
    public BooleanProperty isPublishedProperty() { return isPublished; }
    public void setPublished(boolean published) { this.isPublished.set(published); }

    public LocalDateTime getStartDate() { return startDate.get(); }
    public ObjectProperty<LocalDateTime> startDateProperty() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate.set(startDate); }

    public LocalDateTime getEndDate() { return endDate.get(); }
    public ObjectProperty<LocalDateTime> endDateProperty() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate.set(endDate); }

    public List<Content> getContents() { return contents; }
    public void addContent(Content content) { this.contents.add(content); }
    public void removeContent(Content content) { this.contents.remove(content); }

    public List<Quiz> getQuizzes() { return quizzes; }
    public void addQuiz(Quiz quiz) { this.quizzes.add(quiz); }
    public void removeQuiz(Quiz quiz) { this.quizzes.remove(quiz); }

    public List<Assignment> getAssignments() { return assignments; }
    public void addAssignment(Assignment assignment) { this.assignments.add(assignment); }
    public void removeAssignment(Assignment assignment) { this.assignments.remove(assignment); }

    public void update(String title, String description) {
        this.title.set(title);
        this.description.set(description);
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate.get()) && now.isBefore(endDate.get());
    }

    public double getProgress(User student) {
        if (contents.isEmpty() && quizzes.isEmpty() && assignments.isEmpty()) {
            return 0.0;
        }

        int totalItems = contents.size() + quizzes.size() + assignments.size();
        int completedItems = 0;

        // Check content completion
        for (Content content : contents) {
            if (content.isCompleted(student)) {
                completedItems++;
            }
        }

        // Check quiz completion
        for (Quiz quiz : quizzes) {
            if (quiz.isCompleted(student)) {
                completedItems++;
            }
        }

        // Check assignment completion
        for (Assignment assignment : assignments) {
            if (assignment.isSubmitted(student)) {
                completedItems++;
            }
        }

        return (double) completedItems / totalItems * 100;
    }
} 