package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен иметь нарушений");
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName(null); // Явно задаём имя как null

        assertEquals("validLogin", user.getName(), "При пустом имени должен использоваться логин");

        user.setName(""); // Пустая строка
        assertEquals("validLogin", user.getName(), "При пустом имени должен использоваться логин");

        user.setName("   "); // Пробелы
        assertEquals("validLogin", user.getName(), "При пустом имени должен использоваться логин");

        user.setName("Real Name"); // Нормальное имя
        assertEquals("Real Name", user.getName(), "При наличии имени должен использоваться name");
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Некорректный email должен вызывать ошибку");
        assertEquals(1, violations.size());
        assertEquals("Электронная почта должна быть валидной", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Дата рождения в будущем должна вызывать ошибку");
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        User user = new User();
        user.setEmail("");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пустой email должен вызывать ошибку");
        assertEquals(1, violations.size());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пустой логин должен вызывать ошибку");
        assertEquals(1, violations.size());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenBirthdayIsNull() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("validLogin");
        user.setBirthday(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Null дата рождения должна вызывать ошибку");
        assertEquals(1, violations.size());
        assertEquals("Дата рождения обязательна", violations.iterator().next().getMessage());
    }
}