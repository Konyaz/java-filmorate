package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        filmStorage.getById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        film.getLikes().remove(userId);
        filmStorage.update(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}