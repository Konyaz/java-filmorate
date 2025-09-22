package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films -> {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("PUT /films -> {}", film);
        return filmService.update(film);
    }

    @GetMapping
    public List<Film> getAll() {
        log.info("GET /films");
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        log.info("GET /films/{}", id);
        return filmService.getById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("PUT /films/{}/like/{}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("DELETE /films/{}/like/{}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("GET /films/popular?count={}", count);
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title") String by
    ) {
        log.info("GET /films/search?query={}&by={}", query, by);

        // Проверка пустого запроса
        if (query == null || query.isBlank()) {
            throw new ValidationException("Поисковый запрос не может быть пустым");
        }

        // Валидация параметра by
        Set<String> validFields = Set.of("title", "director", "description");
        Set<String> searchFields = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(validFields::contains)
                .collect(Collectors.toSet());

        if (searchFields.isEmpty()) {
            throw new ValidationException("Параметр 'by' должен содержать одно из значений: title, director, description");
        }

        return filmService.searchFilms(query, searchFields);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId,
                                         @RequestParam String sortBy) {
        log.info("GET /films/director/{}?sortBy={}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
}