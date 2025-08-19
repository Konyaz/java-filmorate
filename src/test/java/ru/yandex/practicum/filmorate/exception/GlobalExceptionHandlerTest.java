package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.controller.TestController;
import ru.yandex.practicum.filmorate.dto.ErrorResponse;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GlobalExceptionHandler.class, TestController.class})
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldHandleNotFoundException() throws Exception {
        mockMvc.perform(get("/test/notfound")
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с ID 1 не найден"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(get("/test/validation")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Пользователь уже поставил лайк"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        FieldError fieldError = new FieldError("film", "name", "Название не может быть пустым");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BeanPropertyBindingResult bindingResult = mock(org.springframework.validation.BeanPropertyBindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        ErrorResponse response = exceptionHandler.handleMethodArgumentNotValidException(ex);

        assertEquals("name: Название не может быть пустым", response.getError());
    }

    @Test
    void shouldHandleConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("duration");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Продолжительность должна быть положительной");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        ErrorResponse response = exceptionHandler.handleConstraintViolationException(ex);

        assertEquals("duration: Продолжительность должна быть положительной", response.getError());
    }

    @Test
    void shouldHandleOtherExceptions() throws Exception {
        mockMvc.perform(get("/test/other")
                        .contentType("application/json"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}