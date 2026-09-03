package com.project.tasktracker.dto;

/**
 * Request body for moving a CardList to a (possibly different) Board
 * at a specific position.
 *
 * <ul>
 *   <li>{@code targetBoardId} — the destination Board (may equal the source board
 *       for same-board reordering).</li>
 *   <li>{@code position} — zero-based insertion position in the destination board.
 *       Negative values are rejected with INVALID_POSITION.
 *       Values beyond the destination's valid range are clamped to the end.</li>
 * </ul>
 */
import com.fasterxml.jackson.annotation.JsonAlias;

public record MoveCardListRequest(
        @JsonAlias("boardId") Long targetBoardId,
        int position
) {}
