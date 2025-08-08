package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void shouldAddValidFilm() {
        Film film = createValidFilm();
        Film addedFilm = filmController.addFilm(film);

        assertNotNull(addedFilm.getId(), "Фильм должен получить ID при добавлении");
        assertEquals(1, filmController.getAllFilms().size(), "Должен быть ровно один фильм в списке");
    }

    @Test
    void shouldFailWhenAddFilmWithInvalidReleaseDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> filmController.addFilm(film),
                "Фильм с датой релиза до 28.12.1895 должен вызывать исключение");
    }

    @Test
    void shouldUpdateFilm() {
        Film film = createValidFilm();
        Film addedFilm = filmController.addFilm(film);

        addedFilm.setName("Updated Name");
        Film updatedFilm = filmController.updateFilm(addedFilm);

        assertEquals("Updated Name", updatedFilm.getName(), "Имя фильма должно обновиться");
        assertEquals(1, filmController.getAllFilms().size(), "Количество фильмов не должно измениться");
    }

    @Test
    void shouldFailWhenUpdateNonExistentFilm() {
        Film film = createValidFilm();
        film.setId(999);

        assertThrows(ValidationException.class, () -> filmController.updateFilm(film),
                "Обновление несуществующего фильма должно вызывать исключение");
    }

    @Test
    void shouldGetAllFilms() {
        Film film1 = createValidFilm();
        Film film2 = createValidFilm();
        film2.setName("Another Film");

        filmController.addFilm(film1);
        filmController.addFilm(film2);

        Collection<Film> films = filmController.getAllFilms();
        assertEquals(2, films.size(), "Должно быть 2 фильма в списке");
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }
}