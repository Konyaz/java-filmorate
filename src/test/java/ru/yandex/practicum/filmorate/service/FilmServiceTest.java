package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.LikeDao;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FilmServiceTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private LikeDao likeDao;

    @InjectMocks
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        when(filmStorage.update(film)).thenReturn(film);

        Film result = filmService.update(film);

        assertEquals(film, result);
        verify(filmStorage, times(1)).update(film);
    }

    @Test
    void updateFilm_notFound_throwsNotFoundException() {
        Film film = new Film();
        film.setId(1L);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        when(filmStorage.update(film)).thenThrow(new NotFoundException("Фильм не найден"));

        assertThrows(NotFoundException.class, () -> filmService.update(film));
        verify(filmStorage, times(1)).update(film);
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
        User user = new User();
        user.setId(1L);

        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        doNothing().when(likeDao).addLike(1L, 1L);

        filmService.addLike(1L, 1L);

        verify(filmStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(1L);
        verify(likeDao, times(1)).addLike(1L, 1L);
    }

    @Test
    void addLike_filmNotFound_throwsNotFoundException() {
        when(filmStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(1L, 1L));
        verify(filmStorage, times(1)).getById(1L);
        verify(userStorage, never()).getById(anyLong());
        verify(likeDao, never()).addLike(anyLong(), anyLong());
    }

    @Test
    void addLike_userNotFound_throwsNotFoundException() {
        Film film = new Film();
        film.setId(1L);

        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(1L, 1L));
        verify(filmStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(1L);
        verify(likeDao, never()).addLike(anyLong(), anyLong());
    }

    @Test
    void removeLike_success() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
        User user = new User();
        user.setId(1L);

        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        doNothing().when(likeDao).removeLike(1L, 1L);

        filmService.removeLike(1L, 1L);

        verify(filmStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(1L);
        verify(likeDao, times(1)).removeLike(1L, 1L);
    }

    @Test
    void getPopularFilms_success() {
        // Создаем тестовые фильмы
        Film film1 = new Film();
        film1.setId(1L);
        film1.setName("Film 1");
        film1.setMpa(new Mpa(1L, "G"));

        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("Film 2");
        film2.setMpa(new Mpa(2L, "PG"));

        Film film3 = new Film();
        film3.setId(3L);
        film3.setName("Film 3");
        film3.setMpa(new Mpa(3L, "PG-13"));

        // Настраиваем моки
        when(filmStorage.getAll()).thenReturn(List.of(film1, film2, film3));
        when(likeDao.getLikes(1L)).thenReturn(List.of(1L, 2L)); // 2 лайка
        when(likeDao.getLikes(2L)).thenReturn(List.of(1L));     // 1 лайк
        when(likeDao.getLikes(3L)).thenReturn(List.of());       // 0 лайков

        // Вызываем метод
        List<Film> popularFilms = filmService.getPopularFilms(2);

        // Проверяем результаты
        assertEquals(2, popularFilms.size());
        assertEquals(1L, popularFilms.get(0).getId()); // Фильм с наибольшим количеством лайков
        assertEquals(2L, popularFilms.get(1).getId()); // Второй по популярности
    }

    @Test
    void shouldThrowValidationExceptionWhenReleaseDateBeforeMinDate() {
        Film film = new Film();
        film.setName("Invalid Film");
        film.setReleaseDate(LocalDate.of(1890, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmService.create(film)
        );

        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenMpaIsNull() {
        Film film = new Film();
        film.setName("Invalid Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmService.create(film)
        );

        assertEquals("Рейтинг MPA обязателен", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenReleaseDateIsNull() {
        Film film = new Film();
        film.setName("Invalid Film");
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmService.create(film)
        );

        assertEquals("Дата релиза обязательна", exception.getMessage());
    }
}