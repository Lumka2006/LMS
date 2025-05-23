package com.lms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Discussion {
    private int id;
    private String title;
    private String content;
    private User author;
    private Course course;
    private LocalDateTime createdAt;
    private List<Comment> comments;

    public Discussion(int id, String title, String content, User author, Course course) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.course = course;
        this.createdAt = LocalDateTime.now();
        this.comments = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    @Override
    public String toString() {
        return title;
    }
} 