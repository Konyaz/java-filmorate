package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreDao {
    Genre create(Genre genre);

    Optional<Genre> getGenreById(Long id);

    List<Genre> getAllGenres();
}