package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(GenreDaoImpl.class)
class GenreDaoImplTest {
    @Autowired
    private GenreDaoImpl genreDao;

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreDao.getAllGenres();
        assertFalse(genres.isEmpty());
        assertEquals(6, genres.size());
        assertEquals("Комедия", genres.get(0).getName());
    }

    @Test
    void testGetGenreById() {
        Genre genre = genreDao.getGenreById(1);
        assertNotNull(genre);
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void testGetNonExistentGenre() {
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class,
                () -> genreDao.getGenreById(999));
    }
}