package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.controller.GlobalExceptionHandler;
import ru.yandex.practicum.filmorate.dto.ErrorResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class GlobalExceptionHandlerTest {
    @Autowired
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void shouldHandleNotFoundException() {
        NotFoundException ex = new NotFoundException("Фильм с ID 1 не найден");

        ErrorResponse response = exceptionHandler.handleNotFoundException(ex);

        assertEquals("Фильм с ID 1 не найден", response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldHandleValidationException() {
        ValidationException ex = new ValidationException("Пользователь уже поставил лайк");

        ErrorResponse response = exceptionHandler.handleValidationException(ex);

        assertEquals("Пользователь уже поставил лайк", response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        FieldError fieldError = new FieldError("film", "name", "Название не может быть пустым");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BeanPropertyBindingResult bindingResult = mock(org.springframework.validation.BeanPropertyBindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        Map<String, String> response = exceptionHandler.handleMethodArgumentNotValidException(ex);

        assertEquals("Название не может быть пустым", response.get("name"));
    }

    @Test
    void shouldHandleConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("duration");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Продолжительность должна быть положительной");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        Map<String, String> response = exceptionHandler.handleConstraintViolationException(ex);

        assertEquals("Продолжительность должна быть положительной", response.get("duration"));
    }

    @Test
    void shouldHandleOtherExceptions() {
        Exception ex = new RuntimeException("Unexpected error");

        ErrorResponse response = exceptionHandler.handleOtherExceptions(ex);

        assertEquals("Внутренняя ошибка сервера", response.getError());
        assertNotNull(response.getTimestamp());
    }
}