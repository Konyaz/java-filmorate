package ru.yandex.practicum.filmorate.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String error;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(String error) {
        this.error = error;
    }
}