package com.lms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.*;

public class Quiz {
    private IntegerProperty quizId;
    private StringProperty title;
    private StringProperty description;
    private IntegerProperty moduleId;
    private DoubleProperty totalPoints;
    private DoubleProperty passingScore;
    private List<Question> questions;
    private List<QuizAttempt> attempts;

    public Quiz(int quizId, String title, String description, int moduleId, double totalPoints, double passingScore) {
        this.quizId = new SimpleIntegerProperty(quizId);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.moduleId = new SimpleIntegerProperty(moduleId);
        this.totalPoints = new SimpleDoubleProperty(totalPoints);
        this.passingScore = new SimpleDoubleProperty(passingScore);
        this.questions = new ArrayList<>();
        this.attempts = new ArrayList<>();
    }

    // Getters for properties
    public IntegerProperty quizIdProperty() { return quizId; }
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public IntegerProperty moduleIdProperty() { return moduleId; }
    public DoubleProperty totalPointsProperty() { return totalPoints; }
    public DoubleProperty passingScoreProperty() { return passingScore; }

    // Getters for values
    public int getQuizId() { return quizId.get(); }
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public int getModuleId() { return moduleId.get(); }
    public double getTotalPoints() { return totalPoints.get(); }
    public double getPassingScore() { return passingScore.get(); }
    public List<Question> getQuestions() { return questions; }
    public List<QuizAttempt> getAttempts() { return attempts; }

    // Setters
    public void setQuizId(int quizId) { this.quizId.set(quizId); }
    public void setTitle(String title) { this.title.set(title); }
    public void setDescription(String description) { this.description.set(description); }
    public void setModuleId(int moduleId) { this.moduleId.set(moduleId); }
    public void setTotalPoints(double totalPoints) { this.totalPoints.set(totalPoints); }
    public void setPassingScore(double passingScore) { this.passingScore.set(passingScore); }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public void setAttempts(List<QuizAttempt> attempts) { this.attempts = attempts; }

    // Helper methods
    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
    }

    public void addAttempt(QuizAttempt attempt) {
        attempts.add(attempt);
    }

    public double calculateScore(QuizAttempt attempt) {
        double score = 0;
        for (Question question : questions) {
            List<String> answer = attempt.getAnswer(question);
            if (answer != null && !answer.isEmpty() && question.isCorrect(answer)) {
                score += question.getPoints();
            }
        }
        return score;
    }

    public boolean isPassingScore(double score) {
        return score >= passingScore.get();
    }

    public boolean isCompleted(User user) {
        if (user == null) return false;
        
        return attempts.stream()
            .anyMatch(attempt -> 
                attempt.getUser().getUserId() == user.getUserId() && 
                attempt.isSubmitted() && 
                isPassingScore(calculateScore(attempt))
            );
    }

    @Override
    public String toString() {
        return title.get();
    }

    public static class Question {
        private IntegerProperty id;
        private StringProperty text;
        private List<String> options;
        private List<String> correctAnswers;
        private IntegerProperty points;
        private StringProperty type;

        public Question(int id, String text, List<String> options, List<String> correctAnswers, int points) {
            this.id = new SimpleIntegerProperty(id);
            this.text = new SimpleStringProperty(text);
            this.options = new ArrayList<>(options);
            this.correctAnswers = new ArrayList<>(correctAnswers);
            this.points = new SimpleIntegerProperty(points);
            this.type = new SimpleStringProperty(determineQuestionType(options, correctAnswers));
        }

        private String determineQuestionType(List<String> options, List<String> correctAnswers) {
            if (options.isEmpty()) {
                return "TEXT";
            } else if (correctAnswers.size() > 1) {
                return "MULTIPLE_CHOICE";
            } else {
                return "SINGLE_CHOICE";
            }
        }

        public int getId() { return id.get(); }
        public IntegerProperty idProperty() { return id; }
        public void setId(int id) { this.id.set(id); }

        public String getText() { return text.get(); }
        public StringProperty textProperty() { return text; }
        public void setText(String text) { this.text.set(text); }

        public List<String> getOptions() { return new ArrayList<>(options); }
        public void setOptions(List<String> options) { this.options = new ArrayList<>(options); }

        public List<String> getCorrectAnswers() { return new ArrayList<>(correctAnswers); }
        public void setCorrectAnswers(List<String> correctAnswers) { 
            this.correctAnswers = new ArrayList<>(correctAnswers); 
        }

        public int getPoints() { return points.get(); }
        public IntegerProperty pointsProperty() { return points; }
        public void setPoints(int points) { this.points.set(points); }

        public String getType() { return type.get(); }
        public StringProperty typeProperty() { return type; }
        public void setType(String type) { this.type.set(type); }

        public boolean isCorrect(List<String> answers) {
            return new HashSet<>(answers).equals(new HashSet<>(correctAnswers));
        }
    }

    public static class QuizAttempt {
        private Quiz quiz;
        private User user;
        private ObjectProperty<LocalDateTime> startTime;
        private ObjectProperty<LocalDateTime> endTime;
        private Map<Question, List<String>> answers;
        private BooleanProperty submitted;

        public QuizAttempt(Quiz quiz, User user) {
            this.quiz = quiz;
            this.user = user;
            this.startTime = new SimpleObjectProperty<>(LocalDateTime.now());
            this.endTime = new SimpleObjectProperty<>(null);
            this.answers = new HashMap<>();
            this.submitted = new SimpleBooleanProperty(false);
        }

        public Quiz getQuiz() { return quiz; }
        public User getUser() { return user; }

        public LocalDateTime getStartTime() { return startTime.get(); }
        public ObjectProperty<LocalDateTime> startTimeProperty() { return startTime; }

        public LocalDateTime getEndTime() { return endTime.get(); }
        public ObjectProperty<LocalDateTime> endTimeProperty() { return endTime; }

        public boolean isSubmitted() { return submitted.get(); }
        public BooleanProperty submittedProperty() { return submitted; }

        public void answerQuestion(Question question, List<String> answers) {
            if (isSubmitted()) {
                throw new IllegalStateException("Quiz attempt is already submitted");
            }
            this.answers.put(question, new ArrayList<>(answers));
        }

        public List<String> getAnswer(Question question) {
            return new ArrayList<>(answers.getOrDefault(question, Collections.emptyList()));
        }

        public void submit() {
            if (isSubmitted()) {
                throw new IllegalStateException("Quiz attempt is already submitted");
            }
            this.endTime.set(LocalDateTime.now());
            this.submitted.set(true);
        }

        public double getScore() {
            if (!isSubmitted()) {
                return 0.0;
            }
            double totalScore = 0.0;
            for (Map.Entry<Question, List<String>> entry : answers.entrySet()) {
                Question question = entry.getKey();
                List<String> answer = entry.getValue();
                if (question.isCorrect(answer)) {
                    totalScore += question.getPoints();
                }
            }
            return totalScore;
        }

        public double getPercentage() {
            return (getScore() / quiz.getTotalPoints()) * 100;
        }
    }
} 