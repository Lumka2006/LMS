package com.lms.model;

import java.time.LocalDateTime;

public class Assignment {
    private int assignmentId;
    private int courseId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private String courseName;
    private String status;
    private String grade;
    private int totalPoints;

    public Assignment(int assignmentId, int courseId, String title, String description, LocalDateTime dueDate, int totalPoints, String courseName) {
        this.assignmentId = assignmentId;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.courseName = courseName;
        this.totalPoints = totalPoints;
    }

    // Getters and setters
    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public boolean isSubmitted(User user) {
        // This would typically check the database
        return false;
    }
} 