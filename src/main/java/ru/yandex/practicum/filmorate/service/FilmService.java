package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.util.ActionsId.*;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FilmService {
    FilmDao filmDao;
    UserDao userDao;
    LikeDao likeDao;
    MpaDao mpaDao;
    GenreDao genreDao;
    FilmDirectorDao filmDirectorDao;
    DirectorDao directorDao;
    EventDao eventDao;

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Film create(@Valid Film film) {
        validate(film);
        validateFilmData(film);
        log.info("Создание фильма: {}", film);
        return filmDao.create(film);
    }

    public Film update(@Valid Film film) {
        validate(film);
        validateFilmData(film);
        log.info("Обновление фильма: {}", film);
        return filmDao.update(film);
    }

    public List<Film> getAll() {
        log.info("Получение всех фильмов");
        return filmDao.getAll();
    }

    public Film getById(Long id) {
        log.info("Получение фильма с ID: {}", id);
        return filmDao.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getById(filmId);
        userDao.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        likeDao.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);

        eventDao.saveEvent(new EventDto(userId, filmId, LIKE.getId(), ADD.getId(), Instant.now()));
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getById(filmId);
        userDao.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        likeDao.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);

        eventDao.saveEvent(new EventDto(userId, filmId, LIKE.getId(), REMOVE.getId(), Instant.now()));
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Получение {} популярных фильмов", count);
        return filmDao.getAll().stream()
                .sorted(Comparator.comparingInt(f -> -likeDao.getLikes(f.getId()).size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        // Проверка существования режиссера
        directorDao.getById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер с ID " + directorId + " не найден"));

        List<Long> filmIds = filmDirectorDao.getFilmIdsByDirectorId(directorId);
        List<Film> films = filmIds.stream()
                .map(this::getById)
                .collect(Collectors.toList());

        // Сортировка
        if ("year".equals(sortBy)) {
            films.sort(Comparator.comparing(Film::getReleaseDate));
        } else if ("likes".equals(sortBy)) {
            films.sort(Comparator.comparingInt(f -> -likeDao.getLikes(f.getId()).size()));
        }

        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        log.info("Получение общих фильмов пользователей {} и {}", userId, friendId);
        List<Long> userLikes = likeDao.getUserLikedFilmsId(userId);
        List<Long> friendLikes = likeDao.getUserLikedFilmsId(friendId);

        return userLikes.stream()
                .filter(friendLikes::contains)
                .map(filmDao::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                // используется та же сортировка по популярности что и в методе выше
                .sorted(Comparator.comparingInt(film -> -likeDao.getLikes(film.getId()).size()))
                .toList();
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
        Long mpaId = film.getMpa().getId();
        mpaDao.getMpaById(mpaId)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID " + mpaId + " не найден"));

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                Long genreId = genre.getId();
                genreDao.getGenreById(genreId)
                        .orElseThrow(() -> new NotFoundException("Жанр с ID " + genreId + " не найден"));
            }
        }

        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                Long directorId = director.getId();
                directorDao.getById(directorId)
                        .orElseThrow(() -> new NotFoundException("Режиссер с ID " + directorId + " не найден"));
            }
        }
    }
}