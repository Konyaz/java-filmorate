package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(GenreDaoImpl.class)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:test-data-genres.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GenreDaoImplTest {
    @Autowired
    private GenreDaoImpl genreDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очистка таблицы genres перед каждым тестом уже выполняется через @Sql
    }

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreDao.getAllGenres();
        assertFalse(genres.isEmpty());
        assertEquals(6, genres.size());
        assertEquals("Комедия", genres.get(0).getName()); // Предполагается, что данные упорядочены по ID
    }

    @Test
    void testGetGenreById() {
        Genre genre = genreDao.getGenreById(1L).orElseThrow();
        assertNotNull(genre);
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void testGetNonExistentGenre() {
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class,
                () -> genreDao.getGenreById(999L));
    }
}