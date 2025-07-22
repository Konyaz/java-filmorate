package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class GlobalExceptionHandlerTest {

    @Autowired
    private ru.yandex.practicum.filmorate.exception.GlobalExceptionHandler exceptionHandler;

    @Test
    void shouldHandleValidationException() {
        ValidationException ex = new ValidationException("Test error message");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error message", response.getBody().get("error"));
    }

    @Test
    void shouldHandleNotFoundValidationException() {
        ValidationException ex = new ValidationException("Фильм не найден");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Фильм не найден", response.getBody().get("error"));
    }
}