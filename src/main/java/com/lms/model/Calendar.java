package com.lms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Calendar {
    private int id;
    private User user;
    private List<Event> events;

    public Calendar(int id, User user) {
        this.id = id;
        this.user = user;
        this.events = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) { this.events = events; }

    public void addEvent(Event event) {
        events.add(event);
    }

    public void removeEvent(Event event) {
        events.remove(event);
    }

    public List<Event> getEventsForDate(LocalDateTime date) {
        List<Event> eventsForDate = new ArrayList<>();
        for (Event event : events) {
            if (event.getStartTime().toLocalDate().equals(date.toLocalDate())) {
                eventsForDate.add(event);
            }
        }
        return eventsForDate;
    }
} 