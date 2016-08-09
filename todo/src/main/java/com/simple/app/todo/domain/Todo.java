package com.simple.app.todo.domain;

import java.time.LocalDateTime;

public class Todo {

    private Long id;
    private String owner;
    private String title;
    private String description;
    private LocalDateTime createdOn = LocalDateTime.now();

    public Todo() {
    }

    public Todo(Long id, String owner, String title, String description, LocalDateTime createdOn) {
        this.id = id;
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.createdOn = createdOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}