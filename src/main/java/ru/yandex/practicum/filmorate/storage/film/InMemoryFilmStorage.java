package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

public class InMemoryFilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long idCounter = 1;

    public Film create(Film film) {
        film.setId(idCounter++);
        films.put(film.getId(), film);
        System.out.println("Создан фильм с ID: " + film.getId());
        return film;
    }

    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        }
        throw new RuntimeException("Фильм с ID " + film.getId() + " не найден");
    }

    public List<Film> getAll() {
        return List.copyOf(films.values());
    }

    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }
}
