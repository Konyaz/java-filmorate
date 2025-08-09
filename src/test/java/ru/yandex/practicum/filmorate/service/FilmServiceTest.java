package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilmServiceTest {
    @Mock
    private FilmStorage filmStorage;
    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createFilm_success() {
        Film film = new Film();
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        when(filmStorage.create(film)).thenReturn(film);

        Film created = filmService.create(film);

        assertEquals(film, created);
        verify(filmStorage).create(film);
    }

    @Test
    void updateFilm_success() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(filmStorage.update(film)).thenReturn(film);

        Film updated = filmService.update(film);

        assertEquals(film, updated);
        verify(filmStorage).getById(1L);
        verify(filmStorage).update(film);
    }

    @Test
    void updateFilm_notFound_throwsException() {
        Film film = new Film();
        film.setId(1L);
        when(filmStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.update(film));
        verify(filmStorage).getById(1L);
        verify(filmStorage, never()).update(any(Film.class));
    }


    @Test
    void getAllFilms_success() {
        Film film = new Film();
        film.setId(1L);
        when(filmStorage.getAll()).thenReturn(List.of(film));

        List<Film> films = filmService.getAll();

        assertEquals(1, films.size());
        verify(filmStorage).getAll();
    }

    @Test
    void getById_success() {
        Film film = new Film();
        film.setId(1L);
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));

        Film found = filmService.getById(1L);

        assertEquals(film, found);
        verify(filmStorage).getById(1L);
    }

    @Test
    void getById_notFound_throwsException() {
        when(filmStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.getById(1L));
    }

    @Test
    void addLike_success() {
        Film film = new Film();
        film.setId(1L);
        film.setLikes(new HashSet<>());
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.of(new User()));
        when(filmStorage.update(film)).thenReturn(film);

        filmService.addLike(1L, 1L);

        assertTrue(film.getLikes().contains(1L));
        verify(filmStorage).getById(1L);
        verify(userStorage).getById(1L);
        verify(filmStorage).update(film);
    }

    @Test
    void addLike_filmNotFound_throwsException() {
        when(filmStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(1L, 1L));
    }

    @Test
    void addLike_userNotFound_throwsException() {
        when(filmStorage.getById(1L)).thenReturn(Optional.of(new Film()));
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(1L, 1L));
    }

    @Test
    void removeLike_success() {
        Film film = new Film();
        film.setId(1L);
        film.setLikes(new HashSet<>(Set.of(1L)));
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.of(new User()));
        when(filmStorage.update(film)).thenReturn(film);

        filmService.removeLike(1L, 1L);

        assertFalse(film.getLikes().contains(1L));
        verify(filmStorage).getById(1L);
        verify(userStorage).getById(1L);
        verify(filmStorage).update(film);
    }

    @Test
    void removeLike_notLiked_doesNothing() {
        Film film = new Film();
        film.setId(1L);
        film.setLikes(new HashSet<>());
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.of(new User()));

        filmService.removeLike(1L, 1L);

        verify(filmStorage).getById(1L);
        verify(userStorage).getById(1L);
        verify(filmStorage, never()).update(any(Film.class));
    }


    @Test
    void getPopularFilms_success() {
        Film film1 = new Film();
        film1.setId(1L);
        film1.setLikes(new HashSet<>(Set.of(1L, 2L)));
        Film film2 = new Film();
        film2.setId(2L);
        film2.setLikes(new HashSet<>(Set.of(1L)));
        when(filmStorage.getAll()).thenReturn(List.of(film1, film2));

        List<Film> popular = filmService.getPopularFilms(1);

        assertEquals(1, popular.size());
        assertEquals(film1, popular.get(0));
        verify(filmStorage).getAll();
    }
}