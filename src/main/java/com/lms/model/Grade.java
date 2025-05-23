package com.lms.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;

public class Grade {
    private int gradeId;
    private int studentId;
    private int assignmentId;
    private DoubleProperty score;
    private StringProperty feedback;
    private LocalDateTime gradedDate;
    private StringProperty gradedBy;

    public Grade(int gradeId, int studentId, int assignmentId, double score, 
                String feedback, LocalDateTime gradedDate, String gradedBy) {
        this.gradeId = gradeId;
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.score = new SimpleDoubleProperty(score);
        this.feedback = new SimpleStringProperty(feedback);
        this.gradedDate = gradedDate;
        this.gradedBy = new SimpleStringProperty(gradedBy);
    }

    // Getters and Setters
    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public double getScore() {
        return score.get();
    }

    public DoubleProperty scoreProperty() {
        return score;
    }

    public void setScore(double score) {
        this.score.set(score);
    }

    public String getFeedback() {
        return feedback.get();
    }

    public StringProperty feedbackProperty() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback.set(feedback);
    }

    public LocalDateTime getGradedDate() {
        return gradedDate;
    }

    public void setGradedDate(LocalDateTime gradedDate) {
        this.gradedDate = gradedDate;
    }

    public String getGradedBy() {
        return gradedBy.get();
    }

    public StringProperty gradedByProperty() {
        return gradedBy;
    }

    public void setGradedBy(String gradedBy) {
        this.gradedBy.set(gradedBy);
    }
} 