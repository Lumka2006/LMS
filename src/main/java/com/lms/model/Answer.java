package com.lms.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;

public class Answer {
    private final StringProperty answerText;
    private final BooleanProperty isCorrect;

    public Answer(String answerText, boolean isCorrect) {
        this.answerText = new SimpleStringProperty(answerText);
        this.isCorrect = new SimpleBooleanProperty(isCorrect);
    }

    // Getters for properties
    public StringProperty answerTextProperty() { return answerText; }
    public BooleanProperty isCorrectProperty() { return isCorrect; }

    // Getters for values
    public String getAnswerText() { return answerText.get(); }
    public boolean isCorrect() { return isCorrect.get(); }

    // Setters
    public void setAnswerText(String answerText) { this.answerText.set(answerText); }
    public void setCorrect(boolean isCorrect) { this.isCorrect.set(isCorrect); }

    @Override
    public String toString() {
        return answerText.get();
    }
} 