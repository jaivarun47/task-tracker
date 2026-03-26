package com.project.tasktracker.model;

import java.time.LocalDateTime;

public class Card {
    private Long id;
    private String name;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;
    private long listId;

    public Card(){

    }

    public Card(Long id, String name, String description, boolean completed, LocalDateTime createdAt, long listId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.completed = completed;
        this.createdAt = createdAt;
        this.listId = listId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getListId() {
        return listId;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }
}
