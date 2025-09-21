package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmDao filmDao;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;
    private final FilmDirectorDao filmDirectorDao;
    private final DirectorDao directorDao;

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

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
    ublic List<Film> getPopularFilms(int count) {
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

    private void validate(Film film) {
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза обязательна");
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