package com.lms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class QuestionResponse {
    private IntegerProperty id;
    private ObjectProperty<Question> question;
    private StringProperty response;
    private DoubleProperty pointsEarned;
    private ObjectProperty<LocalDateTime> submittedAt;
    private ObjectProperty<QuizAttempt> attempt;

    public QuestionResponse(int id, Question question, String response, QuizAttempt attempt) {
        this.id = new SimpleIntegerProperty(id);
        this.question = new SimpleObjectProperty<>(question);
        this.response = new SimpleStringProperty(response);
        this.pointsEarned = new SimpleDoubleProperty(0.0);
        this.submittedAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.attempt = new SimpleObjectProperty<>(attempt);
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public Question getQuestion() { return question.get(); }
    public ObjectProperty<Question> questionProperty() { return question; }
    public void setQuestion(Question question) { this.question.set(question); }

    public String getResponse() { return response.get(); }
    public StringProperty responseProperty() { return response; }
    public void setResponse(String response) { this.response.set(response); }

    public double getPointsEarned() { return pointsEarned.get(); }
    public DoubleProperty pointsEarnedProperty() { return pointsEarned; }
    public void setPointsEarned(double pointsEarned) { this.pointsEarned.set(pointsEarned); }

    public LocalDateTime getSubmittedAt() { return submittedAt.get(); }
    public ObjectProperty<LocalDateTime> submittedAtProperty() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt.set(submittedAt); }

    public QuizAttempt getAttempt() { return attempt.get(); }
    public ObjectProperty<QuizAttempt> attemptProperty() { return attempt; }
    public void setAttempt(QuizAttempt attempt) { this.attempt.set(attempt); }

    public void grade() {
        if (question.get().getType() == Question.QuestionType.MULTIPLE_CHOICE ||
            question.get().getType() == Question.QuestionType.TRUE_FALSE) {
            // For multiple choice and true/false, check if response matches correct answer
            boolean isCorrect = question.get().getAnswers().stream()
                .filter(Answer::isCorrect)
                .anyMatch(a -> a.getAnswerText().equals(response.get()));
            pointsEarned.set(isCorrect ? question.get().getPoints() : 0.0);
        } else {
            // For short answer and essay, manual grading is required
            pointsEarned.set(0.0);
        }
    }
} 