package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        Film created = filmService.create(film);
        log.info("Фильм создан: {}", created);
        return created;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        Film updated = filmService.update(film);
        log.info("Фильм обновлен: {}", updated);
        return updated;
    }

    @GetMapping
    public List<Film> getAll() {
        log.info("Получен запрос на получение всех фильмов");
        List<Film> films = filmService.getAll();
        log.info("Возвращено {} фильмов", films.size());
        return films;
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        log.info("Получен запрос на получение фильма с ID: {}", id);
        Film film = filmService.getById(id);
        log.info("Найден фильм: {}", film);
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка: фильм ID={}, пользователь ID={}", id, userId);
        filmService.addLike(id, userId);
        log.info("Лайк добавлен: фильм ID={}, пользователь ID={}", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка: фильм ID={}, пользователь ID={}", id, userId);
        filmService.removeLike(id, userId);
        log.info("Лайк удален: фильм ID={}, пользователь ID={}", id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count
    ) {
        log.info("Получен запрос на получение {} популярных фильмов", count);
        List<Film> films = filmService.getPopularFilms(count);
        log.info("Возвращено {} популярных фильмов", films.size());
        return films;
    }
}