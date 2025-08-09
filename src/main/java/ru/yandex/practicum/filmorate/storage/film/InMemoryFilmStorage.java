package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Film create(Film film) {
        film.setId(idCounter++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        }
        throw new RuntimeException("Фильм с ID " + film.getId() + " не найден");
    }

    @Override
    public List<Film> getAll() {
        return List.copyOf(films.values());
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }
}