package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmDao filmDao;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;
    private final LikeDao likeDao;
    private final UserDao userDao;

    // Создание нового фильма
    public Film create(Film film) {
        validateFilmMpa(film);
        validateFilmGenres(film);
        return filmDao.create(film);
    }

    // Обновление существующего фильма
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID фильма обязателен для обновления");
        }
        if (!filmDao.existsById(film.getId())) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        validateFilmMpa(film);
        validateFilmGenres(film);

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


    private void validateFilmMpa(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }
        mpaDao.getMpaById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID " + film.getMpa().getId() + " не найден"));
    }

    private void validateFilmGenres(Film film) {
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreDao.getGenreById(genre.getId())
                    .orElseThrow(() -> new NotFoundException("Жанр с ID " + genre.getId() + " не найден")));
        }
    }
}