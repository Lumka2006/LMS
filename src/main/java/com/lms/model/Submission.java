package com.lms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Submission {
    private IntegerProperty id;
    private ObjectProperty<User> student;
    private ObjectProperty<Assignment> assignment;
    private StringProperty content;
    private StringProperty feedback;
    private DoubleProperty score;
    private ObjectProperty<LocalDateTime> submittedAt;
    private ObjectProperty<LocalDateTime> gradedAt;
    private ObjectProperty<User> gradedBy;
    private List<String> attachments;

    public Submission(int id, User student, Assignment assignment, String content) {
        this.id = new SimpleIntegerProperty(id);
        this.student = new SimpleObjectProperty<>(student);
        this.assignment = new SimpleObjectProperty<>(assignment);
        this.content = new SimpleStringProperty(content);
        this.feedback = new SimpleStringProperty("");
        this.score = new SimpleDoubleProperty(0.0);
        this.submittedAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.gradedAt = new SimpleObjectProperty<>(null);
        this.gradedBy = new SimpleObjectProperty<>(null);
        this.attachments = new ArrayList<>();
    }

    // Add a constructor for DB mapping
    public Submission(int submissionId, int assignmentId, int studentId, String studentName, String content, double score) {
        this.id = new SimpleIntegerProperty(submissionId);
        this.assignment = new SimpleObjectProperty<>(null); // Assignment can be set later if needed
        this.student = new SimpleObjectProperty<>(null); // User can be set later if needed
        this.content = new SimpleStringProperty(content);
        this.feedback = new SimpleStringProperty("");
        this.score = new SimpleDoubleProperty(score);
        this.submittedAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.gradedAt = new SimpleObjectProperty<>(null);
        this.gradedBy = new SimpleObjectProperty<>(null);
        this.attachments = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public User getStudent() { return student.get(); }
    public ObjectProperty<User> studentProperty() { return student; }
    public void setStudent(User student) { this.student.set(student); }

    public Assignment getAssignment() { return assignment.get(); }
    public ObjectProperty<Assignment> assignmentProperty() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment.set(assignment); }

    public String getContent() { return content.get(); }
    public StringProperty contentProperty() { return content; }
    public void setContent(String content) { this.content.set(content); }

    public String getFeedback() { return feedback.get(); }
    public StringProperty feedbackProperty() { return feedback; }
    public void setFeedback(String feedback) { this.feedback.set(feedback); }

    public double getScore() { return score.get(); }
    public DoubleProperty scoreProperty() { return score; }
    public void setScore(double score) { this.score.set(score); }

    public LocalDateTime getSubmittedAt() { return submittedAt.get(); }
    public ObjectProperty<LocalDateTime> submittedAtProperty() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt.set(submittedAt); }

    public LocalDateTime getGradedAt() { return gradedAt.get(); }
    public ObjectProperty<LocalDateTime> gradedAtProperty() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt.set(gradedAt); }

    public User getGradedBy() { return gradedBy.get(); }
    public ObjectProperty<User> gradedByProperty() { return gradedBy; }
    public void setGradedBy(User gradedBy) { this.gradedBy.set(gradedBy); }

    public boolean isGraded() {
        return gradedAt.get() != null;
    }

    public boolean isLate() {
        if (submittedAt.get() == null || assignment.get() == null || assignment.get().getDueDate() == null) {
            return false;
        }
        // Set due date to end of day (23:59:59)
        LocalDateTime dueDateTime = assignment.get().getDueDate()
            .withHour(23)
            .withMinute(59)
            .withSecond(59);
        return submittedAt.get().isAfter(dueDateTime);
    }

    public void grade(double score, String feedback, User grader) {
        this.score.set(score);
        this.feedback.set(feedback);
        this.gradedAt.set(LocalDateTime.now());
        this.gradedBy.set(grader);
    }

    public List<String> getAttachments() {
        return new ArrayList<>(attachments);
    }

    public void addAttachment(String filePath) {
        attachments.add(filePath);
    }

    public void removeAttachment(String filePath) {
        attachments.remove(filePath);
    }

    public void clearAttachments() {
        attachments.clear();
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    public String getStudentName() {
        User s = getStudent();
        if (s != null) {
            return s.getFirstName() + " " + s.getLastName();
        }
        return "";
    }
} 