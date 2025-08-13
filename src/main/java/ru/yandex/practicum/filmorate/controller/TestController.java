package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/notfound")
    public void throwNotFound() {
        throw new NotFoundException("Фильм с ID 1 не найден");
    }

    @GetMapping("/validation")
    public void throwValidation() {
        throw new ValidationException("Пользователь уже поставил лайк");
    }

    @GetMapping("/other")
    public void throwOther() {
        throw new RuntimeException("Unexpected error");
    }
}