package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class FilmServiceTest {

    private AutoCloseable closeable;

    @Mock
    private FilmDao filmStorage;

    @Mock
    private UserDao userStorage;

    @Mock
    private LikeDao likeDao;

    @Mock
    private MpaDao mpaDao;

    @Mock
    private GenreDao genreDao;

    @InjectMocks
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Настройка Mpa
        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        when(mpaDao.getMpaById(anyLong())).thenReturn(Optional.of(mpa));

        // Настройка Genre
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Комедия");
        when(genreDao.getGenreById(anyLong())).thenReturn(Optional.of(genre));

        // Настройка existsById для update
        when(filmStorage.existsById(anyLong())).thenReturn(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void createFilm_success() {
        Film film = createTestFilm();
        when(filmStorage.create(film)).thenReturn(film);

        Film result = filmService.create(film);

        assertEquals(film, result);
        verify(filmStorage, times(1)).create(film);
    }

    @Test
    void updateFilm_success() {
        Film film = createTestFilm();
        when(filmStorage.update(film)).thenReturn(film);
        when(filmStorage.existsById(film.getId())).thenReturn(true);

        Film result = filmService.update(film);

        assertEquals(film, result);
        verify(filmStorage, times(1)).update(film);
    }

    @Test
    void updateFilm_notFound_throwsNotFoundException() {
        Film film = createTestFilm();
        when(filmStorage.existsById(film.getId())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmService.update(film));
        assertEquals("Фильм с ID " + film.getId() + " не найден", exception.getMessage());

        verify(filmStorage, never()).update(film);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenMpaNotFound() {
        Film film = createTestFilm();
        film.getMpa().setId(999L);
        when(mpaDao.getMpaById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.create(film)
        );

        assertEquals("Рейтинг MPA с ID 999 не найден", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGenreNotFound() {
        Film film = createTestFilm();
        Genre genre = new Genre();
        genre.setId(999L);
        film.getGenres().add(genre);

        when(genreDao.getGenreById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.create(film)
        );

        assertEquals("Жанр с ID 999 не найден", exception.getMessage());
    }

    @Test
    void addLike_success() {
        Long filmId = 1L;
        Long userId = 1L;
        Film film = createTestFilm();
        User user = new User();
        user.setId(userId);

        when(filmStorage.getById(filmId)).thenReturn(Optional.of(film));
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));
        doNothing().when(likeDao).addLike(filmId, userId);

        filmService.addLike(filmId, userId);

        verify(filmStorage, times(1)).getById(filmId);
        verify(userStorage, times(1)).getById(userId);
        verify(likeDao, times(1)).addLike(filmId, userId);
    }

    @Test
    void removeLike_success() {
        Long filmId = 1L;
        Long userId = 1L;
        Film film = createTestFilm();
        User user = new User();
        user.setId(userId);

        when(filmStorage.getById(filmId)).thenReturn(Optional.of(film));
        when(userStorage.getById(userId)).thenReturn(Optional.of(user));
        doNothing().when(likeDao).removeLike(filmId, userId);

        filmService.removeLike(filmId, userId);

        verify(filmStorage, times(1)).getById(filmId);
        verify(userStorage, times(1)).getById(userId);
        verify(likeDao, times(1)).removeLike(filmId, userId);
    }

    @Test
    void searchFilmsByName_success() {
        Film film = createTestFilm();
        film.setId(1L);
        film.setName("Властелин колец");

        when(filmStorage.searchFilms("колец", Set.of("title", "director"))).thenReturn(List.of(film));

        List<Film> result = filmService.searchFilms("колец", Set.of("title", "director"));

        assertEquals(1, result.size());
        assertEquals("Властелин колец", result.get(0).getName());
        verify(filmStorage, times(1)).searchFilms("колец", Set.of("title", "director"));
    }

    @Test
    void searchFilmsEmptyQuery_throwsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmService.searchFilms("", Set.of("title", "director"))
        );

        assertEquals("Поисковый запрос не может быть пустым", exception.getMessage());
        verify(filmStorage, never()).searchFilms(anyString(), any());
    }

    @Test
    void searchFilmsNoResults_returnsEmptyList() {
        when(filmStorage.searchFilms("несуществующий", Set.of("title", "director"))).thenReturn(Collections.emptyList());

        List<Film> result = filmService.searchFilms("несуществующий", Set.of("title", "director"));

        assertTrue(result.isEmpty());
        verify(filmStorage, times(1)).searchFilms("несуществующий", Set.of("title", "director"));
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);

        return film;
    }
}
