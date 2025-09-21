package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({FilmDaoImpl.class, GenreDaoImpl.class, MpaDaoImpl.class})
@ActiveProfiles("test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:test-data.sql")
class FilmDaoImplTest {

    @Autowired
    private FilmDaoImpl filmStorage;

    @Autowired
    private MpaDao mpaDao;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        Mpa mpa = mpaDao.getMpaById(1L)
                .orElseThrow(() -> new RuntimeException("MPA с ID 1 не найден"));

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
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
        List<Film> films = filmStorage.getAll();
        assertFalse(films.isEmpty());
    }

    @Test
    void testUpdateNonExistentFilm() {
        testFilm.setId(999L);
        assertThrows(NotFoundException.class, () -> filmStorage.update(testFilm));
    }

    @Test
    void testGetNonExistentFilm() {
        Optional<Film> found = filmStorage.getById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void testSearchFilmsByName_success() {
        List<Film> films = filmStorage.searchFilms("колец", Set.of("title"));
        assertEquals(1, films.size());
        assertEquals("Властелин колец", films.get(0).getName());
    }

    @Test
    void testSearchFilmsByDescription_success() {
        List<Film> films = filmStorage.searchFilms("тайнах", Set.of("description"));
        assertEquals(1, films.size());
        assertEquals("Напряжённый сюжет о тайнах", films.get(0).getDescription());
    }

    @Test
    void testSearchFilmsNoResults_returnsEmptyList() {
        List<Film> films = filmStorage.searchFilms("несуществующий", Set.of("title"));
        assertTrue(films.isEmpty());
    }
}
