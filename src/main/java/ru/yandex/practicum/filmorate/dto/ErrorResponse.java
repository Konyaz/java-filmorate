package ru.yandex.practicum.filmorate.dto;

public class ErrorResponse {
    private final String error;
    private final long timestamp;

    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }

    public String getError() {
        return error;
    }

    public long getTimestamp() {
        return timestamp;
    }
}