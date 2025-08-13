package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Sql(scripts = "classpath:schema.sql")
class LikeDaoImplTest {
    @Autowired
    private LikeDaoImpl likeDao;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    private Long filmId;
    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() {
        Film film = new Film();
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);

        Film createdFilm = filmStorage.create(film);
        filmId = createdFilm.getId();

        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser1 = userStorage.create(user1);
        userId1 = createdUser1.getId();

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userStorage.create(user2);
        userId2 = createdUser2.getId();
    }

    @Test
    void testAddLike() {
        likeDao.addLike(filmId, userId1);
        List<Long> likes = likeDao.getLikes(filmId);

        assertEquals(1, likes.size());
        assertEquals(userId1, likes.get(0));
    }

    @Test
    void testRemoveLike() {
        likeDao.addLike(filmId, userId1);
        likeDao.removeLike(filmId, userId1);
        List<Long> likes = likeDao.getLikes(filmId);

        assertTrue(likes.isEmpty());
    }

    @Test
    void testGetLikes() {
        likeDao.addLike(filmId, userId1);
        likeDao.addLike(filmId, userId2);
        List<Long> likes = likeDao.getLikes(filmId);

        assertEquals(2, likes.size());
        assertTrue(likes.contains(userId1));
        assertTrue(likes.contains(userId2));
    }
}