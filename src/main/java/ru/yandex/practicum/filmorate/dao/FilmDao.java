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

    void delete(Long filmId);
}