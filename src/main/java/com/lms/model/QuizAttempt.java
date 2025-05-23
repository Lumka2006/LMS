package com.lms.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizAttempt {
    private final IntegerProperty attemptId;
    private final IntegerProperty quizId;
    private final IntegerProperty userId;
    private final ObjectProperty<LocalDateTime> startTime;
    private final ObjectProperty<LocalDateTime> endTime;
    private final DoubleProperty score;
    private Map<Integer, String> answers; // questionId -> answer

    public QuizAttempt(int attemptId, int quizId, int userId) {
        this.attemptId = new SimpleIntegerProperty(attemptId);
        this.quizId = new SimpleIntegerProperty(quizId);
        this.userId = new SimpleIntegerProperty(userId);
        this.startTime = new SimpleObjectProperty<>(LocalDateTime.now());
        this.endTime = new SimpleObjectProperty<>(null);
        this.score = new SimpleDoubleProperty(0.0);
        this.answers = new HashMap<>();
    }

    // Getters for properties
    public IntegerProperty attemptIdProperty() { return attemptId; }
    public IntegerProperty quizIdProperty() { return quizId; }
    public IntegerProperty userIdProperty() { return userId; }
    public ObjectProperty<LocalDateTime> startTimeProperty() { return startTime; }
    public ObjectProperty<LocalDateTime> endTimeProperty() { return endTime; }
    public DoubleProperty scoreProperty() { return score; }

    // Getters for values
    public int getAttemptId() { return attemptId.get(); }
    public int getQuizId() { return quizId.get(); }
    public int getUserId() { return userId.get(); }
    public LocalDateTime getStartTime() { return startTime.get(); }
    public LocalDateTime getEndTime() { return endTime.get(); }
    public double getScore() { return score.get(); }
    public Map<Integer, String> getAnswers() { return answers; }

    // Setters
    public void setAttemptId(int attemptId) { this.attemptId.set(attemptId); }
    public void setQuizId(int quizId) { this.quizId.set(quizId); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setStartTime(LocalDateTime startTime) { this.startTime.set(startTime); }
    public void setEndTime(LocalDateTime endTime) { this.endTime.set(endTime); }
    public void setScore(double score) { this.score.set(score); }
    public void setAnswers(Map<Integer, String> answers) { this.answers = answers; }

    // Helper methods
    public void submitAnswer(int questionId, String answer) {
        answers.put(questionId, answer);
    }

    public String getAnswer(int questionId) {
        return answers.get(questionId);
    }

    public void submit() {
        endTime.set(LocalDateTime.now());
    }

    public boolean isSubmitted() {
        return endTime.get() != null;
    }

    public long getDurationInMinutes() {
        if (endTime.get() == null) {
            return 0;
        }
        return java.time.Duration.between(startTime.get(), endTime.get()).toMinutes();
    }

    @Override
    public String toString() {
        return "Attempt " + attemptId.get() + " for Quiz " + quizId.get();
    }
} 