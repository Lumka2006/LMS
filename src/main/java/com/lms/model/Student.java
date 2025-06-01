package com.lms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Student extends User {
    private IntegerProperty id;
    private StringProperty name;
    private StringProperty email;
    private ObjectProperty<LocalDateTime> enrollmentDate;
    private StringProperty status;
    private ObjectProperty<LocalDateTime> createdAt;
    private ObjectProperty<LocalDateTime> updatedAt;
    private DoubleProperty progress;
    private StringProperty assignments;
    private List<Assignment> completedAssignments;
    private List<Assignment> totalAssignments;

    // Static list of predefined students
    public static final List<Student> PREDEFINED_STUDENTS = new ArrayList<>();

    static {
        PREDEFINED_STUDENTS.add(new Student(1, "John Student", "student1@example.com", "pass123", "John", "Student"));
        PREDEFINED_STUDENTS.add(new Student(2, "Jane Student", "student2@example.com", "pass123", "Jane", "Student"));
    }

    public Student(int id, String name, String email) {
        super(id, name, "pass123", email, "STUDENT", name);
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.enrollmentDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.status = new SimpleStringProperty("ACTIVE");
        this.createdAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.updatedAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.progress = new SimpleDoubleProperty(0.0);
        this.assignments = new SimpleStringProperty("0/0");
        this.completedAssignments = new ArrayList<>();
        this.totalAssignments = new ArrayList<>();
    }

    public Student(int id, String username, String email, String password, String firstName, String lastName) {
        super(id, username, password, email, "STUDENT", firstName + " " + lastName);
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(firstName + " " + lastName);
        this.email = new SimpleStringProperty(email);
        this.enrollmentDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.status = new SimpleStringProperty("ACTIVE");
        this.createdAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.updatedAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.progress = new SimpleDoubleProperty(0.0);
        this.assignments = new SimpleStringProperty("0/0");
        this.completedAssignments = new ArrayList<>();
        this.totalAssignments = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public void setEmail(String email) { this.email.set(email); }

    public LocalDateTime getEnrollmentDate() { return enrollmentDate.get(); }
    public ObjectProperty<LocalDateTime> enrollmentDateProperty() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime date) { this.enrollmentDate.set(date); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public void setCreatedAt(LocalDateTime date) { this.createdAt.set(date); }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime date) { this.updatedAt.set(date); }

    public double getProgress() { return progress.get(); }
    public DoubleProperty progressProperty() { return progress; }
    public void setProgress(double progress) { this.progress.set(progress); }

    public String getAssignments() { return assignments.get(); }
    public StringProperty assignmentsProperty() { return assignments; }
    public void setAssignments(String assignments) { this.assignments.set(assignments); }

    public void addAssignment(Assignment assignment) {
        totalAssignments.add(assignment);
        updateProgress();
    }

    public void completeAssignment(Assignment assignment) {
        if (!completedAssignments.contains(assignment)) {
            completedAssignments.add(assignment);
            updateProgress();
        }
    }

    private void updateProgress() {
        if (totalAssignments.isEmpty()) {
            progress.set(0.0);
            return;
        }

        double totalGrade = 0;
        int gradedAssignments = 0;
        
        for (Assignment assignment : totalAssignments) {
            if (assignment.getGrade() != null && !assignment.getGrade().isEmpty()) {
                try {
                    // Extract numeric grade from string (e.g., "85/100" -> 85)
                    String gradeStr = assignment.getGrade().split("/")[0];
                    totalGrade += Double.parseDouble(gradeStr);
                    gradedAssignments++;
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Skip invalid grade formats
                    continue;
                }
            }
        }
        
        // Calculate average grade as progress
        if (gradedAssignments > 0) {
            progress.set(totalGrade / (gradedAssignments * 100));
        } else {
            progress.set(0.0);
        }
    }

    public int getCompletedAssignments() {
        return completedAssignments.size();
    }

    public int getTotalAssignments() {
        return totalAssignments.size();
    }

    @Override
    public String toString() {
        return name.get();
    }
} 