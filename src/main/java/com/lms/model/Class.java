package com.lms.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Class {
    private int id;
    private StringProperty name;
    private StringProperty subject;
    private StringProperty description;
    private int instructorId;

    public Class(int id, String name, String subject, String description, int instructorId) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.subject = new SimpleStringProperty(subject);
        this.description = new SimpleStringProperty(description);
        this.instructorId = instructorId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getSubject() { return subject.get(); }
    public void setSubject(String subject) { this.subject.set(subject); }
    public StringProperty subjectProperty() { return subject; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    @Override
    public String toString() {
        return name.get() + " (" + subject.get() + ")";
    }
} 