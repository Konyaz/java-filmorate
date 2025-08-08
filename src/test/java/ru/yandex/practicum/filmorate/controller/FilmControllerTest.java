package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FilmService filmService;
    @Autowired
    private ObjectMapper objectMapper;

    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setLikes(Set.of());
    }

    @Test
    void createFilm_success() throws Exception {
        when(filmService.create(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    void createFilm_invalidData_returnsBadRequest() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("");
        invalidFilm.setReleaseDate(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_invalidReleaseDate_returnsBadRequest() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("Test Film");
        invalidFilm.setDescription("Description");
        invalidFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        invalidFilm.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_success() throws Exception {
        when(filmService.update(any(Film.class))).thenReturn(film);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateFilm_notFound_throwsException() throws Exception {
        when(filmService.update(any(Film.class))).thenThrow(new NotFoundException("Фильм с ID 1 не найден"));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с ID 1 не найден"));
    }

    @Test
    void getAllFilms_success() throws Exception {
        when(filmService.getAll()).thenReturn(List.of(film));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getFilmById_success() throws Exception {
        when(filmService.getById(1L)).thenReturn(film);

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getFilmById_notFound_throwsException() throws Exception {
        when(filmService.getById(1L)).thenThrow(new NotFoundException("Фильм с ID 1 не найден"));

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с ID 1 не найден"));
    }

    @Test
    void addLike_success() throws Exception {
        doNothing().when(filmService).addLike(1L, 1L);

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());
    }

    @Test
    void addLike_filmNotFound_returnsNotFound() throws Exception {
        doThrow(new NotFoundException("Фильм с ID 1 не найден")).when(filmService).addLike(1L, 1L);

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с ID 1 не найден"));
    }

    @Test
    void addLike_userNotFound_returnsNotFound() throws Exception {
        doThrow(new NotFoundException("Пользователь с ID 1 не найден")).when(filmService).addLike(1L, 1L);

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 1 не найден"));
    }

    @Test
    void addLike_alreadyLiked_returnsBadRequest() throws Exception {
        doThrow(new ValidationException("Пользователь уже поставил лайк этому фильму")).when(filmService).addLike(1L, 1L);

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Пользователь уже поставил лайк этому фильму"));
    }

    @Test
    void removeLike_success() throws Exception {
        doNothing().when(filmService).removeLike(1L, 1L);

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());
    }

    @Test
    void removeLike_notLiked_returnsBadRequest() throws Exception {
        doThrow(new ValidationException("Лайк не найден")).when(filmService).removeLike(1L, 1L);

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Лайк не найден"));
    }

    @Test
    void getPopularFilms_success() throws Exception {
        when(filmService.getPopularFilms(anyInt())).thenReturn(List.of(film));

        mockMvc.perform(get("/films/popular?count=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}