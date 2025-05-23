package com.lms.model;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private String content;
    private User author;
    private LocalDateTime createdAt;

    public Comment(int id, String content, User author) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return content;
    }
} 