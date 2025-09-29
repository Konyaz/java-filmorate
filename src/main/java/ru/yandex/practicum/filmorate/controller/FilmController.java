package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
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
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films -> {}", film);
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getMpa().getId() == null) {
            throw new ValidationException("ID рейтинга MPA обязателен");
        }
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("PUT /films -> {}", film);
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getMpa().getId() == null) {
            throw new ValidationException("ID рейтинга MPA обязателен");
        }
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
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        log.info("GET /films/popular?count={}&genreId={}&year={}", count, genreId, year);
        if (count <= 0) {
            throw new ValidationException("Количество популярных фильмов должно быть больше 0");
        }

        if (genreId != null && genreId <= 0) {
            throw new ValidationException("ID жанра должен быть положительным числом");
        }

        if (year != null && (year < MIN_RELEASE_DATE.getYear() || year > LocalDate.now().getYear())) {
            throw new ValidationException(String.format(
                    "Год должен быть в диапазоне от %d до текущего года", MIN_RELEASE_DATE.getYear()));
        }

        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title") String by
    ) {
        log.info("GET /films/search?query={}&by={}", query, by);

        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException("Поисковый запрос не может быть пустым");
        }
        if (by == null || by.isEmpty()) {
            throw new ValidationException("Укажите хотя бы одно поле для поиска");
        }

        query = query.trim();

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

        log.info("Searching for films with query '{}' in fields: {}", query, searchFields);
        return filmService.searchFilms(query, searchFields);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId,
                                         @RequestParam String sortBy) {
        log.info("GET /films/director/{}?sortBy={}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        log.info("GET /films/common?userId={}&friendId={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Void> deleteFilm(@PathVariable Long filmId) {
        try {
            filmService.delete(filmId);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}