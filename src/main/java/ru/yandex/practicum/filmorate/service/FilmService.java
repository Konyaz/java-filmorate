package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.util.ActionsId.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmDao filmDao;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;
    private final DirectorDao directorDao;
    private final UserDao userDao;
    private final LikeDao likeDao;
    private final EventDao eventDao;
    private final DirectorService directorService;
    private final FilmDirectorDao filmDirectorDao;

    public Film create(@Valid Film film) {
        validate(film);
        log.info("Создание фильма: {}", film);
        return filmDao.create(film);
    }

    public void delete(Long filmId) {
        if (filmDao.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        filmDao.delete(filmId);
        log.info("Film deleted id={}", filmId);
    }

    public Film update(@Valid Film film) {
        validate(film);
        log.info("Обновление фильма: {}", film);
        return filmDao.update(film);
    }

    public List<Film> getAll() {
        log.info("Получение всех фильмов");
        List<Film> films = filmDao.getAll();
        films.forEach(film -> {
            film.setGenres(genreDao.getGenresByFilmId(film.getId()));
            film.setDirectors(directorService.getDirectorsForFilm(film.getId()));
        });
        log.info("Найдено фильмов: {}", films.size());
        return films;
    }

    public Film getById(Long id) {
        log.info("Получение фильма с ID: {}", id);
        Film film = filmDao.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
        film.setGenres(genreDao.getGenresByFilmId(film.getId()));
        film.setDirectors(directorService.getDirectorsForFilm(film.getId()));
        log.info("Найден фильм: {}", film.getName());
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        getById(filmId);
        userDao.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        List<Long> filmsIds = likeDao.getUserLikedFilmsId(userId);
        if (!filmsIds.contains(filmId)) {
            likeDao.addLike(filmId, userId);
        }
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        eventDao.saveEvent(new EventDto(userId, filmId, LIKE.getId(), ADD.getId(), Instant.now()));

    }

    public void removeLike(Long filmId, Long userId) {
        getById(filmId);
        userDao.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        List<Long> filmsIds = likeDao.getUserLikedFilmsId(userId);
        if (filmsIds.contains(filmId)) {
            likeDao.removeLike(filmId, userId);
        }
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
        eventDao.saveEvent(new EventDto(userId, filmId, LIKE.getId(), REMOVE.getId(), Instant.now()));
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        log.info("Получение {} популярных фильмов с фильтром по жанру {} и году {}", count, genreId, year);
        List<Film> films = filmDao.getPopular(count, genreId, year);
        films.forEach(film -> {
            film.setGenres(genreDao.getGenresByFilmId(film.getId()));
            film.setDirectors(directorService.getDirectorsForFilm(film.getId()));
        });
        log.info("Найдено популярных фильмов: {}", films.size());
        return films;
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        log.info("Получение фильмов режиссера {} с сортировкой по {}", directorId, sortBy);
        directorDao.getById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер с ID " + directorId + " не найден"));

        List<Long> filmIds = filmDirectorDao.getFilmIdsByDirectorId(directorId);
        List<Film> films = filmIds.stream()
                .map(this::getById)
                .collect(Collectors.toList());

        if ("year".equals(sortBy)) {
            films.sort(Comparator.comparing(Film::getReleaseDate));
        } else if ("likes".equals(sortBy)) {
            films.sort(Comparator.comparingInt(f -> -likeDao.getLikes(f.getId()).size()));
        }

        log.info("Найдено фильмов режиссера: {}", films.size());
        return films;
    }

    public List<Film> searchFilms(String query, Set<String> by) {
        log.info("Поиск фильмов по запросу: '{}' в полях: {}", query, by);
        List<Film> films = filmDao.searchFilms(query, by);
        films.forEach(film -> {
            film.setGenres(genreDao.getGenresByFilmId(film.getId()));
            film.setDirectors(directorService.getDirectorsForFilm(film.getId()));
            film.setRate(likeDao.getLikes(film.getId()).size());
        });
        films = films.stream().sorted((f1, f2) -> f2.getRate() - f1.getRate()).collect(Collectors.toList());

        // Логируем найденные фильмы для отладки
        films.forEach(film -> {
            log.info("Найден фильм: ID={}, Name={}, Directors={}",
                    film.getId(), film.getName(),
                    film.getDirectors().stream().map(Director::getName).collect(Collectors.toList()));
        });

        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        log.info("Получение общих фильмов пользователей {} и {}", userId, friendId);
        List<Long> userLikes = likeDao.getUserLikedFilmsId(userId);
        List<Long> friendLikes = likeDao.getUserLikedFilmsId(friendId);

        List<Film> commonFilms = userLikes.stream()
                .filter(friendLikes::contains)
                .map(filmDao::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparingInt(film -> -likeDao.getLikes(film.getId()).size()))
                .toList();

        log.info("Найдено общих фильмов: {}", commonFilms.size());
        return commonFilms;
    }

    private void validate(Film film) {
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