package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(GenreDaoImpl.class)
@Sql(scripts = {"classpath:schema.sql", "classpath:test-data.sql"})
class GenreDaoImplTest {
    @Autowired
    private GenreDaoImpl genreDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreDao.getAllGenres();
        assertFalse(genres.isEmpty());
        assertEquals(6, genres.size());
        assertEquals("Комедия", genres.get(0).getName());
    }

    @Test
    void testGetGenreById() {
        Genre genre = genreDao.getGenreById(1L).orElseThrow();
        assertNotNull(genre);
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void testGetNonExistentGenre() {
        Optional<Genre> genre = genreDao.getGenreById(999L);
        assertTrue(genre.isEmpty(), "Non-existent genre should return empty Optional");
    }
}