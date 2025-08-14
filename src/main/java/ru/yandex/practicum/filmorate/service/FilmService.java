package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Film create(Film film) {
        // Проверка на null перед вызовом методов
        if (film.getReleaseDate() == null) {
            log.error("Дата релиза фильма не указана");
            throw new ValidationException("Дата релиза обязательна");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза фильма раньше 28 декабря 1895 года: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        try {
            log.info("Создание фильма: {}", film);
            return filmStorage.create(film);
        } catch (DataIntegrityViolationException e) {
            log.error("Фильм с названием {} уже существует", film.getName());
            throw new ValidationException("Фильм с таким названием уже существует");
        }
    }

    public Film update(Film film) {
        // Проверка на null перед вызовом методов
        if (film.getReleaseDate() == null) {
            log.error("Дата релиза фильма не указана");
            throw new ValidationException("Дата релиза обязательна");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза фильма раньше 28 декабря 1895 года: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        log.info("Обновление фильма: {}", film);
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        log.info("Получение всех фильмов");
        return filmStorage.getAll();
    }

    public Film getById(Long id) {
        log.info("Получение фильма с ID: {}", id);
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getById(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        // Инициализируем likes, если он null
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        if (film.getLikes().contains(userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь уже поставил лайк фильму");
        }
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getById(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        // Инициализируем likes, если он null
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        if (film.getLikes().contains(userId)) {
            filmStorage.removeLike(filmId, userId);
        }
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Получение {} популярных фильмов", count);
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes() != null ? f2.getLikes().size() : 0,
                        f1.getLikes() != null ? f1.getLikes().size() : 0))
                .limit(count)
                .collect(Collectors.toList());
    }
}
