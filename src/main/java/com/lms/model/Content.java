package com.lms.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Content {
    private IntegerProperty id;
    private StringProperty title;
    private StringProperty description;
    private StringProperty type; // VIDEO, DOCUMENT, LINK, etc.
    private StringProperty url;
    private ObjectProperty<Module> module;
    private IntegerProperty order;
    private BooleanProperty isPublished;
    private ObjectProperty<LocalDateTime> createdAt;
    private ObjectProperty<LocalDateTime> updatedAt;
    private Set<User> completedBy;

    public Content(int id, String title, String description, String type, String url, Module module, int order) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.type = new SimpleStringProperty(type);
        this.url = new SimpleStringProperty(url);
        this.module = new SimpleObjectProperty<>(module);
        this.order = new SimpleIntegerProperty(order);
        this.isPublished = new SimpleBooleanProperty(false);
        this.createdAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.updatedAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.completedBy = new HashSet<>();
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    public void setDescription(String description) { this.description.set(description); }

    public String getType() { return type.get(); }
    public StringProperty typeProperty() { return type; }
    public void setType(String type) { this.type.set(type); }

    public String getUrl() { return url.get(); }
    public StringProperty urlProperty() { return url; }
    public void setUrl(String url) { this.url.set(url); }

    public Module getModule() { return module.get(); }
    public ObjectProperty<Module> moduleProperty() { return module; }
    public void setModule(Module module) { this.module.set(module); }

    public int getOrder() { return order.get(); }
    public IntegerProperty orderProperty() { return order; }
    public void setOrder(int order) { this.order.set(order); }

    public boolean isPublished() { return isPublished.get(); }
    public BooleanProperty isPublishedProperty() { return isPublished; }
    public void setPublished(boolean published) { this.isPublished.set(published); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }

    public void update(String title, String description, String type, String url) {
        this.title.set(title);
        this.description.set(description);
        this.type.set(type);
        this.url.set(url);
        this.updatedAt.set(LocalDateTime.now());
    }

    public boolean isCompleted(User user) {
        return completedBy.contains(user);
    }

    public void markAsCompleted(User user) {
        completedBy.add(user);
    }

    public void markAsIncomplete(User user) {
        completedBy.remove(user);
    }

    public Set<User> getCompletedBy() {
        return new HashSet<>(completedBy);
    }
} 