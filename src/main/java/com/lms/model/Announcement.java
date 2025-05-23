package com.lms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Announcement {
    private IntegerProperty id;
    private StringProperty title;
    private StringProperty content;
    private ObjectProperty<Course> course;
    private ObjectProperty<User> author;
    private ObjectProperty<LocalDateTime> createdAt;
    private BooleanProperty isPublished;

    public Announcement(int id, String title, String content, Course course, User author) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.content = new SimpleStringProperty(content);
        this.course = new SimpleObjectProperty<>(course);
        this.author = new SimpleObjectProperty<>(author);
        this.createdAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.isPublished = new SimpleBooleanProperty(false);
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    public String getContent() { return content.get(); }
    public StringProperty contentProperty() { return content; }
    public void setContent(String content) { this.content.set(content); }

    public Course getCourse() { return course.get(); }
    public ObjectProperty<Course> courseProperty() { return course; }
    public void setCourse(Course course) { this.course.set(course); }

    public User getAuthor() { return author.get(); }
    public ObjectProperty<User> authorProperty() { return author; }
    public void setAuthor(User author) { this.author.set(author); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }

    public boolean isPublished() { return isPublished.get(); }
    public BooleanProperty isPublishedProperty() { return isPublished; }
    public void setPublished(boolean published) { this.isPublished.set(published); }

    @Override
    public String toString() {
        return title.get() + " - " + createdAt.get();
    }
} 