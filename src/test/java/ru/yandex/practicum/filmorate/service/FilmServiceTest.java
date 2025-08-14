package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
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
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            // Инициализация моков
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mocks", e);
        }
    }

    @Test
    void createFilm_success() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
        when(filmStorage.create(film)).thenReturn(film);

        Film result = filmService.create(film);

        assertEquals(film, result);
        verify(filmStorage, times(1)).create(film);
    }

    @Test
    void updateFilm_success() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(filmStorage.update(film)).thenReturn(film);

        Film result = filmService.update(film);

        assertEquals(film, result);
        verify(filmStorage, times(1)).getById(1L);
        verify(filmStorage, times(1)).update(film);
    }

    @Test
    void updateFilm_notFound_throwsNotFoundException() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
        when(filmStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.update(film));
        verify(filmStorage, times(1)).getById(1L);
        verify(filmStorage, never()).update(film);
    }

    @Test
    void getAllFilms_success() {
        Film film1 = new Film();
        film1.setId(1L);
        film1.setName("Film 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(new Mpa(1L, "G"));
        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("Film 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        film2.setMpa(new Mpa(2L, "PG"));
        when(filmStorage.getAll()).thenReturn(List.of(film1, film2));

        List<Film> result = filmService.getAll();

        assertEquals(2, result.size());
        assertEquals(film1, result.get(0));
        assertEquals(film2, result.get(1));
        verify(filmStorage, times(1)).getAll();
    }

    @Test
    void getFilmById_success() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));

        Film result = filmService.getById(1L);

        assertEquals(film, result);
        verify(filmStorage, times(1)).getById(1L);
    }

    @Test
    void getFilmById_notFound_throwsNotFoundException() {
        when(filmStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.getById(1L));
        verify(filmStorage, times(1)).getById(1L);
    }

    @Test
    void addLike_success() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
        film.setLikes(new HashSet<>());
        User user = new User();
        user.setId(1L);
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(filmStorage.update(film)).thenReturn(film);

        filmService.addLike(1L, 1L);

        assertTrue(film.getLikes().contains(1L));
        verify(filmStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(1L);
        verify(filmStorage, times(1)).addLike(1L, 1L);
        verify(filmStorage, times(1)).update(film);
    }

    @Test
    void addLike_filmNotFound_throwsNotFoundException() {
        when(filmStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(1L, 1L));
        verify(filmStorage, times(1)).getById(1L);
        verify(userStorage, never()).getById(anyLong());
        verify(filmStorage, never()).addLike(anyLong(), anyLong());
        verify(filmStorage, never()).update(any(Film.class));
    }

    @Test
    void addLike_userNotFound_throwsNotFoundException() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(1L, 1L));
        verify(filmStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(1L);
        verify(filmStorage, never()).addLike(anyLong(), anyLong());
        verify(filmStorage, never()).update(any(Film.class));
    }

    @Test
    void removeLike_success() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
        film.setLikes(new HashSet<>(Set.of(1L)));
        User user = new User();
        user.setId(1L);
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(filmStorage.update(film)).thenReturn(film);

        filmService.removeLike(1L, 1L);

        assertFalse(film.getLikes().contains(1L));
        verify(filmStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(1L);
        verify(filmStorage, times(1)).removeLike(1L, 1L);
        verify(filmStorage, times(1)).update(film);
    }

    @Test
    void removeLike_notLiked_doesNothing() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
        film.setLikes(new HashSet<>()); // Пустой set
        User user = new User();
        user.setId(1L);
        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));

        filmService.removeLike(1L, 1L);

        assertTrue(film.getLikes().isEmpty());
        verify(filmStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(1L);
        verify(filmStorage, never()).removeLike(anyLong(), anyLong());
        verify(filmStorage, never()).update(any(Film.class));
    }

    @Test
    void getPopularFilms_success() {
        Film film1 = new Film();
        film1.setId(1L);
        film1.setName("Film 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(new Mpa(1L, "G"));
        film1.setLikes(new HashSet<>(Set.of(1L, 2L)));
        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("Film 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        film2.setMpa(new Mpa(2L, "PG"));
        film2.setLikes(new HashSet<>(Set.of(1L)));
        when(filmStorage.getAll()).thenReturn(List.of(film1, film2));

        List<Film> result = filmService.getPopularFilms(2);

        assertEquals(2, result.size());
        assertEquals(film1, result.get(0)); // film1 has more likes
        assertEquals(film2, result.get(1));
        verify(filmStorage, times(1)).getAll();
    }
}