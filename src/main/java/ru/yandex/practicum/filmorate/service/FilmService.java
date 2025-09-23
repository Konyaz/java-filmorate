package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.LikeDao;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.FilmDirectorDao;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmDao filmDao;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;
    private final FilmDirectorDao filmDirectorDao;
    private final DirectorDao directorDao;
    private final UserDao userDao;
    private final LikeDao likeDao;

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    // Создание нового фильма
    public Film create(@Valid Film film) {
        validate(film);
        validateFilmData(film);
        log.info("Создание фильма: {}", film);
        return filmDao.create(film);
    }

    // Обновление существующего фильма
    public Film update(@Valid Film film) {
        validate(film);
        validateFilmData(film);
        log.info("Обновление фильма: {}", film);
        return filmDao.update(film);

    }

    // Получение всех фильмов
    public List<Film> getAll() {
        return filmDao.getAll();
    }

    // Получение фильма по ID
    public Film getById(Long id) {
        return filmDao.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    // Добавление лайка пользователем фильму
    public void addLike(Long filmId, Long userId) {
        getById(filmId); // Проверка существования фильма
        userDao.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        likeDao.addLike(filmId, userId);
    }

    // Удаление лайка пользователем у фильма
    public void removeLike(Long filmId, Long userId) {
        getById(filmId); // Проверка существования фильма
        userDao.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        likeDao.removeLike(filmId, userId);
    }

    // Получение популярных фильмов
    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество популярных фильмов должно быть больше 0");
        }
        return filmDao.getPopular(count);
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

    // Поиск фильмов по названию, описанию или режиссеру
    public List<Film> searchFilms(String query, Set<String> by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Поисковый запрос не может быть пустым");
        }
        if (by == null || by.isEmpty()) {
            throw new ValidationException("Укажите хотя бы одно поле для поиска");
        }
        return filmDao.searchFilms(query, by);
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
