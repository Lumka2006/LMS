package com.lms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Enrollment {
    private IntegerProperty id;
    private ObjectProperty<User> student;
    private ObjectProperty<Course> course;
    private DoubleProperty progress;
    private ObjectProperty<LocalDateTime> enrolledAt;
    private ObjectProperty<LocalDateTime> completedAt;
    private EnrollmentStatus status;

    public enum EnrollmentStatus {
        ACTIVE,
        COMPLETED,
        DROPPED,
        ON_HOLD
    }

    public Enrollment(int id, User student, Course course) {
        this.id = new SimpleIntegerProperty(id);
        this.student = new SimpleObjectProperty<>(student);
        this.course = new SimpleObjectProperty<>(course);
        this.progress = new SimpleDoubleProperty(0.0);
        this.enrolledAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.completedAt = new SimpleObjectProperty<>(null);
        this.status = EnrollmentStatus.ACTIVE;
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public User getStudent() { return student.get(); }
    public ObjectProperty<User> studentProperty() { return student; }
    public void setStudent(User student) { this.student.set(student); }

    public Course getCourse() { return course.get(); }
    public ObjectProperty<Course> courseProperty() { return course; }
    public void setCourse(Course course) { this.course.set(course); }

    public double getProgress() { return progress.get(); }
    public DoubleProperty progressProperty() { return progress; }
    public void setProgress(double progress) { this.progress.set(progress); }

    public LocalDateTime getEnrolledAt() { return enrolledAt.get(); }
    public ObjectProperty<LocalDateTime> enrolledAtProperty() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt.set(enrolledAt); }

    public LocalDateTime getCompletedAt() { return completedAt.get(); }
    public ObjectProperty<LocalDateTime> completedAtProperty() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt.set(completedAt); }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public void complete() {
        status = EnrollmentStatus.COMPLETED;
        completedAt.set(LocalDateTime.now());
        progress.set(100.0);
    }

    public void drop() {
        status = EnrollmentStatus.DROPPED;
    }

    public void putOnHold() {
        status = EnrollmentStatus.ON_HOLD;
    }

    public void reactivate() {
        status = EnrollmentStatus.ACTIVE;
    }

    public void updateProgress() {
        course.get().calculateProgress(student.get());
        progress.set(course.get().getProgress());
    }
} 