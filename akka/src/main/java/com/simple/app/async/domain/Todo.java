package com.simple.app.async.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Todo implements Serializable {

    private final Long id;
    private final String owner;
    private final String title;
    private final String description;
    private final LocalDateTime createdOn;
    private final LocalDateTime finishedOn = null;

    @JsonCreator
    public Todo(@JsonProperty("id") Long id,
                @JsonProperty("owner") String owner,
                @JsonProperty("title") String title,
                @JsonProperty("description") String description,
                @JsonProperty("createdOn") LocalDateTime createdOn) {
        this.id = id;
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.createdOn = createdOn == null ? LocalDateTime.now() : createdOn;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public String getOwner() {
        return owner;
    }

    public LocalDateTime getFinishedOn() {
        return finishedOn;
    }
}