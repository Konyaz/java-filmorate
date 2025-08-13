package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({FilmDbStorage.class, GenreDaoImpl.class, MpaDaoImpl.class})
class FilmDbStorageTest {
    @Autowired
    private FilmDbStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        testFilm.setMpa(mpa);
    }

    @Test
    void testCreateFilm() {
        Film created = filmStorage.create(testFilm);
        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
    }

    @Test
    void testUpdateFilm() {
        Film created = filmStorage.create(testFilm);
        created.setName("Updated Film");

        Film updated = filmStorage.update(created);
        assertEquals("Updated Film", updated.getName());
    }

    @Test
    void testGetFilmById() {
        Film created = filmStorage.create(testFilm);
        Optional<Film> found = filmStorage.getById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getName(), found.get().getName());
    }

    @Test
    void testGetAllFilms() {
        filmStorage.create(testFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setReleaseDate(LocalDate.of(2010, 1, 1));
        anotherFilm.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(2);
        anotherFilm.setMpa(mpa);

        filmStorage.create(anotherFilm);

        List<Film> films = filmStorage.getAll();
        assertEquals(2, films.size());
    }

    @Test
    void testUpdateNonExistentFilm() {
        testFilm.setId(999L);
        assertThrows(NotFoundException.class,
                () -> filmStorage.update(testFilm));
    }

    @Test
    void testGetNonExistentFilm() {
        Optional<Film> found = filmStorage.getById(999L);
        assertTrue(found.isEmpty());
    }
}