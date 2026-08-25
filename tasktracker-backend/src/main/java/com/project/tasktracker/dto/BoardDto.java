package com.project.tasktracker.dto;

import java.util.UUID;

public class BoardDto {
    private Long id;
    private String name;
    private UUID userId;

    public BoardDto() {}

    public BoardDto(Long id, String name, UUID userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
