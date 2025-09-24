package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmDao {

    Film create(Film film);

    Film update(Film film);

    List<Film> getAll();

    Optional<Film> getById(Long id);

    List<Film> searchFilms(String query, Set<String> by);

    List<Film> getPopular(int count, Integer genreId, Integer year);

    boolean existsById(Long id);
}