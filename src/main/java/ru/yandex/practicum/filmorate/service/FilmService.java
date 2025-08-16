package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    private final LikeDao likeDao;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Film create(@Valid Film film) {
        validate(film);
        validateFilmData(film);
        log.info("Создание фильма: {}", film);
        return filmStorage.create(film);
    }

    public Film update(@Valid Film film) {
        validate(film);
        validateFilmData(film);
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
        getById(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        likeDao.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        getById(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        likeDao.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Получение {} популярных фильмов", count);
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt(f -> -likeDao.getLikes(f.getId()).size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validate(Film film) {
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза обязательна");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getMpa() == null) {
            throw new ValidationException("Рейтинг MPA обязателен");
        }
        if (film.getMpa().getId() == null) {
            throw new ValidationException("ID рейтинга MPA обязателен");
        }
    }

    private void validateFilmData(Film film) {
        // Проверка существования MPA
        Long mpaId = film.getMpa().getId();
        mpaDao.getMpaById(mpaId)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID " + mpaId + " не найден"));

        // Проверка существования жанров
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                Long genreId = genre.getId();
                genreDao.getGenreById(genreId)
                        .orElseThrow(() -> new NotFoundException("Жанр с ID " + genreId + " не найден"));
            }
        }
    }
}