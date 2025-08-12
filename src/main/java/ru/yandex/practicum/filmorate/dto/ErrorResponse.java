package ru.yandex.practicum.filmorate.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final String error;
    private final LocalDateTime timestamp;

    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public String getError() {
        return error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}