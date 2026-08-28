package com.project.tasktracker.dto;

import java.time.Instant;

public record SessionDto(
        String token,
        Instant expiresAt
) {
}
