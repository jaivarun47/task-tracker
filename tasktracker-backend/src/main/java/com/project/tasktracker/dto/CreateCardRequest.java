package com.project.tasktracker.dto;

/**
 * Request body for creating a card.
 *
 * NOTE: The listId field that previously appeared here has been removed.
 * It was dead code — the list ID is always supplied via the URL path variable
 * ({@code /api/lists/{listId}/cards}) and was never read from this request body.
 */
public class CreateCardRequest {
    private String name;
    private String description;

    public CreateCardRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
