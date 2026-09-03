package com.project.tasktracker.dto;

/**
 * Request body for moving a Card to a (possibly different) CardList
 * at a specific position.
 *
 * <ul>
 *   <li>{@code targetListId} — the destination CardList (may equal the source list
 *       for same-list reordering).</li>
 *   <li>{@code position} — zero-based insertion position in the destination list.
 *       Negative values are rejected with INVALID_POSITION.
 *       Values beyond the destination's valid range are clamped to the end.</li>
 * </ul>
 */
import com.fasterxml.jackson.annotation.JsonAlias;

public record MoveCardRequest(
        @JsonAlias("listId") Long targetListId,
        int position
) {}
