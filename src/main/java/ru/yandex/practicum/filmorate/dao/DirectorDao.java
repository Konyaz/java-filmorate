package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorDao {
    Director create(Director director);

    Director update(Director director);

    List<Director> getAll();

    Optional<Director> getById(Long id);

    void deleteById(Long id);

    boolean exists(Long id);
}