package com.project.tasktracker.api;

import java.time.Instant;

public class ApiResponse<T> {
    private final Instant timestamp;
    private final int status;
    private final T data;
    private final Object error;

    public ApiResponse(Instant timestamp, int status, T data, Object error) {
        this.timestamp = timestamp;
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public Object getError() {
        return error;
    }
}

