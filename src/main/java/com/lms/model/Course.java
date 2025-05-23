package com.lms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

public class Course {
    private final IntegerProperty courseId;
    private final StringProperty code;
    private final StringProperty title;
    private final StringProperty description;
    private final IntegerProperty teacherId;
    private final StringProperty instructorName;
    private final ObjectProperty<LocalDateTime> startDate;
    private final ObjectProperty<LocalDateTime> endDate;
    private DoubleProperty progress;
    private BooleanProperty isPublished;
    private List<Module> modules;
    private List<Enrollment> enrollments;
    private List<Assignment> assignments;
    private List<Announcement> announcements;

    // Constructor with all fields
    public Course(int courseId, String code, String title, String description, 
                 int teacherId, String instructorName, LocalDateTime startDate, LocalDateTime endDate) {
        this.courseId = new SimpleIntegerProperty(courseId);
        this.code = new SimpleStringProperty(code);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.teacherId = new SimpleIntegerProperty(teacherId);
        this.instructorName = new SimpleStringProperty(instructorName);
        this.startDate = new SimpleObjectProperty<>(startDate);
        this.endDate = new SimpleObjectProperty<>(endDate);
        this.progress = new SimpleDoubleProperty(0.0);
        this.isPublished = new SimpleBooleanProperty(false);
        this.modules = new ArrayList<>();
        this.enrollments = new ArrayList<>();
        this.assignments = new ArrayList<>();
        this.announcements = new ArrayList<>();
    }

    // Constructor without dates
    public Course(int courseId, String code, String title, String description, 
                 int teacherId, String instructorName) {
        this(courseId, code, title, description, teacherId, instructorName, null, null);
    }

    // Getters for properties
    public IntegerProperty courseIdProperty() { return courseId; }
    public StringProperty codeProperty() { return code; }
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public IntegerProperty teacherIdProperty() { return teacherId; }
    public StringProperty instructorNameProperty() { return instructorName; }
    public ObjectProperty<LocalDateTime> startDateProperty() { return startDate; }
    public ObjectProperty<LocalDateTime> endDateProperty() { return endDate; }
    public DoubleProperty progressProperty() { return progress; }

    // Getters for values
    public int getCourseId() { return courseId.get(); }
    public String getCode() { return code.get(); }
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public int getTeacherId() { return teacherId.get(); }
    public String getInstructorName() { return instructorName.get(); }
    public LocalDateTime getStartDate() { return startDate.get(); }
    public LocalDateTime getEndDate() { return endDate.get(); }
    public double getProgress() { return progress.get(); }

    // Setters
    public void setCourseId(int courseId) { this.courseId.set(courseId); }
    public void setCode(String code) { this.code.set(code); }
    public void setTitle(String title) { this.title.set(title); }
    public void setDescription(String description) { this.description.set(description); }
    public void setTeacherId(int teacherId) { this.teacherId.set(teacherId); }
    public void setInstructorName(String instructorName) { this.instructorName.set(instructorName); }
    public void setStartDate(LocalDateTime startDate) { this.startDate.set(startDate); }
    public void setEndDate(LocalDateTime endDate) { this.endDate.set(endDate); }

    public List<Module> getModules() { return modules; }
    public void setModules(List<Module> modules) { this.modules = modules; }
    
    public List<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(List<Enrollment> enrollments) { this.enrollments = enrollments; }
    
    public List<Assignment> getAssignments() { return assignments; }
    public void addAssignment(Assignment assignment) { this.assignments.add(assignment); }
    public void removeAssignment(Assignment assignment) { this.assignments.remove(assignment); }
    
    public List<Announcement> getAnnouncements() { return announcements; }
    public void setAnnouncements(List<Announcement> announcements) { this.announcements = announcements; }

    public void addModule(Module module) {
        modules.add(module);
    }

    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
    }

    public void addAnnouncement(Announcement announcement) {
        announcements.add(announcement);
    }

    public void calculateProgress(User currentUser) {
        if (modules.isEmpty()) {
            progress.set(0.0);
            return;
        }

        double totalProgress = modules.stream()
            .mapToDouble(module -> module.getProgress(currentUser))
            .sum();
        progress.set(totalProgress / modules.size());
    }

    @Override
    public String toString() {
        return title.get();
    }
} 