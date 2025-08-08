package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFilmStorageTest {
    private InMemoryFilmStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
    }

    @Test
    void createFilm_success() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmStorage.create(film);

        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
        assertNotNull(created.getLikes());
        assertTrue(created.getLikes().isEmpty());
    }

    @Test
    void updateFilm_success() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = filmStorage.create(film);

        created.setName("Updated Film");
        Film updated = filmStorage.update(created);

        assertEquals("Updated Film", updated.getName());
        assertEquals(created.getId(), updated.getId());
    }

    @Test
    void updateFilm_notFound_throwsException() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Test Film");

        assertThrows(NotFoundException.class, () -> filmStorage.update(film));
    }

    @Test
    void getAllFilms_returnsList() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        filmStorage.create(film1);
        filmStorage.create(film2);

        List<Film> films = filmStorage.getAll();

        assertEquals(2, films.size());
    }

    @Test
    void getById_success() {
        Film film = new Film();
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = filmStorage.create(film);

        Film found = filmStorage.getById(created.getId()).orElseThrow();

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getById_notFound_returnsEmpty() {
        assertTrue(filmStorage.getById(999L).isEmpty());
    }
}