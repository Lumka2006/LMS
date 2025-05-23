package com.lms.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import java.util.ArrayList;
import java.util.List;

public class Question {
    public enum QuestionType {
        MULTIPLE_CHOICE,
        TRUE_FALSE,
        SHORT_ANSWER,
        ESSAY
    }

    private final IntegerProperty questionId;
    private final StringProperty questionText;
    private final StringProperty correctAnswer;
    private final DoubleProperty points;
    private final IntegerProperty quizId;
    private final StringProperty type;
    private List<String> options;
    private List<Answer> answers;

    public Question(int questionId, String questionText, String correctAnswer, double points, int quizId) {
        this.questionId = new SimpleIntegerProperty(questionId);
        this.questionText = new SimpleStringProperty(questionText);
        this.correctAnswer = new SimpleStringProperty(correctAnswer);
        this.points = new SimpleDoubleProperty(points);
        this.quizId = new SimpleIntegerProperty(quizId);
        this.type = new SimpleStringProperty(QuestionType.SHORT_ANSWER.name());
        this.options = new ArrayList<>();
        this.answers = new ArrayList<>();
    }

    // Getters for properties
    public IntegerProperty questionIdProperty() { return questionId; }
    public StringProperty questionTextProperty() { return questionText; }
    public StringProperty correctAnswerProperty() { return correctAnswer; }
    public DoubleProperty pointsProperty() { return points; }
    public IntegerProperty quizIdProperty() { return quizId; }
    public StringProperty typeProperty() { return type; }

    // Getters for values
    public int getQuestionId() { return questionId.get(); }
    public String getQuestionText() { return questionText.get(); }
    public String getCorrectAnswer() { return correctAnswer.get(); }
    public double getPoints() { return points.get(); }
    public int getQuizId() { return quizId.get(); }
    public List<String> getOptions() { return options; }
    public List<Answer> getAnswers() { return answers; }
    public QuestionType getType() { return QuestionType.valueOf(type.get()); }

    // Setters
    public void setQuestionId(int questionId) { this.questionId.set(questionId); }
    public void setQuestionText(String questionText) { this.questionText.set(questionText); }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer.set(correctAnswer); }
    public void setPoints(double points) { this.points.set(points); }
    public void setQuizId(int quizId) { this.quizId.set(quizId); }
    public void setOptions(List<String> options) { this.options = options; }
    public void setType(QuestionType type) { this.type.set(type.name()); }

    // Helper methods
    public void addOption(String option) {
        options.add(option);
    }

    public void removeOption(String option) {
        options.remove(option);
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public void removeAnswer(Answer answer) {
        answers.remove(answer);
    }

    public boolean isCorrectAnswer(String answer) {
        return correctAnswer.get().equals(answer);
    }

    @Override
    public String toString() {
        return questionText.get();
    }
} 