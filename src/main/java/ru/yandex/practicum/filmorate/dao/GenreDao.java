package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreDao {
    List<Genre> getAllGenres();

    List<Genre> getGenresByFilmId(Long filmId);

    Optional<Genre> getGenreById(Long id);
}