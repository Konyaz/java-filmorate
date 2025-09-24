package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:test-data.sql")
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G"));
    }

    @Test
    void createFilm_success() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    @Test
    void updateFilm_success() throws Exception {
        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated description");
        updatedFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        updatedFilm.setDuration(150);
        updatedFilm.setMpa(new Mpa(2L, "PG"));

        when(filmService.update(any(Film.class))).thenReturn(updatedFilm);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFilm)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedFilm)));
    }

    @Test
    void getFilms_success() throws Exception {
        Film film1 = new Film();
        film1.setId(1L);
        film1.setName("Film 1");
        film1.setMpa(new Mpa(1L, "G"));

        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("Film 2");
        film2.setMpa(new Mpa(2L, "PG"));

        when(filmService.getAll()).thenReturn(List.of(film1, film2));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(film1, film2))));
    }

    @Test
    void createFilm_withDirectors_success() throws Exception {
        Film filmWithDirectors = new Film();
        filmWithDirectors.setId(1L);
        filmWithDirectors.setName("Film with Directors");
        filmWithDirectors.setDuration(123);
        filmWithDirectors.setReleaseDate(LocalDate.of(2001, 1, 1));
        filmWithDirectors.setMpa(new Mpa(1L, "G"));

        Director director1 = new Director();
        director1.setId(1L);
        director1.setName("Director 1");

        Director director2 = new Director();
        director2.setId(2L);
        director2.setName("Director 2");

        filmWithDirectors.setDirectors(List.of(director1, director2));

        when(filmService.create(any(Film.class))).thenReturn(filmWithDirectors);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithDirectors)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.directors.length()").value(2))
                .andExpect(jsonPath("$.directors[0].id").value(1L))
                .andExpect(jsonPath("$.directors[1].id").value(2L));
    }

    @Test
    void updateFilm_withDirectors_success() throws Exception {
        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film with Directors");
        updatedFilm.setDuration(123);
        updatedFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        updatedFilm.setMpa(new Mpa(1L, "G"));

        Director director = new Director();
        director.setId(1L);
        director.setName("Updated Director");
        updatedFilm.setDirectors(List.of(director));

        when(filmService.update(any(Film.class))).thenReturn(updatedFilm);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.directors.length()").value(1))
                .andExpect(jsonPath("$.directors[0].name").value("Updated Director"));
    }

    @Test
    void getFilmsByDirector_sortedByLikes_success() throws Exception {
        Film film1 = new Film();
        film1.setId(1L);
        film1.setName("Popular Film");
        film1.setMpa(new Mpa(1L, "G"));

        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("Less Popular Film");
        film2.setMpa(new Mpa(2L, "PG"));

        when(filmService.getFilmsByDirector(eq(1L), eq("likes")))
                .thenReturn(List.of(film1, film2));

        mockMvc.perform(get("/films/director/1?sortBy=likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getFilmsByDirector_sortedByYear_success() throws Exception {
        Film film1 = new Film();
        film1.setId(1L);
        film1.setName("Old Film");
        film1.setReleaseDate(LocalDate.of(1990, 1, 1));
        film1.setMpa(new Mpa(1L, "G"));

        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("New Film");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setMpa(new Mpa(2L, "PG"));

        when(filmService.getFilmsByDirector(eq(1L), eq("year")))
                .thenReturn(List.of(film1, film2));

        mockMvc.perform(get("/films/director/1?sortBy=year"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getFilmsByDirector_directorNotFound() throws Exception {
        when(filmService.getFilmsByDirector(eq(999L), anyString()))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Режиссер не найден"));

        mockMvc.perform(get("/films/director/999?sortBy=likes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFilmsByDirector_invalidSortByParameter() throws Exception {
        when(filmService.getFilmsByDirector(eq(1L), eq("invalid")))
                .thenReturn(List.of()); // или можно бросить исключение

        mockMvc.perform(get("/films/director/1?sortBy=invalid"))
                .andExpect(status().isOk());
    }

    @Test
    void getFilmsByDirector_emptyResult() throws Exception {
        when(filmService.getFilmsByDirector(eq(2L), eq("likes")))
                .thenReturn(List.of());

        mockMvc.perform(get("/films/director/2?sortBy=likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getFilmById_withDirectors_success() throws Exception {
        Film filmWithDirectors = new Film();
        filmWithDirectors.setId(1L);
        filmWithDirectors.setName("Film with Directors");
        filmWithDirectors.setMpa(new Mpa(1L, "G"));

        Director director = new Director();
        director.setId(1L);
        director.setName("Test Director");
        filmWithDirectors.setDirectors(List.of(director));

        when(filmService.getById(1L)).thenReturn(filmWithDirectors);

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.directors.length()").value(1))
                .andExpect(jsonPath("$.directors[0].name").value("Test Director"));
    }

    @Test
    void addLikeToFilm_success() throws Exception {
        doNothing().when(filmService).addLike(1L, 1L);

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPopularFilms_withCount_success() throws Exception {
        Film popularFilm = new Film();
        popularFilm.setId(1L);
        popularFilm.setName("Popular Film");
        popularFilm.setMpa(new Mpa(1L, "G"));

        // Исправлено: теперь метод принимает три параметра
        when(filmService.getPopularFilms(5, null, null)).thenReturn(List.of(popularFilm));

        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getPopularFilms_withGenreFilter_success() throws Exception {
        Film comedyFilm = new Film();
        comedyFilm.setId(2L);
        comedyFilm.setName("Comedy Film");
        comedyFilm.setMpa(new Mpa(1L, "G"));

        // Тест с фильтром по жанру
        when(filmService.getPopularFilms(10, 1, null)).thenReturn(List.of(comedyFilm));

        mockMvc.perform(get("/films/popular?count=10&genreId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2L));
    }

    @Test
    void getPopularFilms_withYearFilter_success() throws Exception {
        Film yearFilm = new Film();
        yearFilm.setId(3L);
        yearFilm.setName("2020 Film");
        yearFilm.setMpa(new Mpa(1L, "G"));

        // Тест с фильтром по году
        when(filmService.getPopularFilms(5, null, 2020)).thenReturn(List.of(yearFilm));

        mockMvc.perform(get("/films/popular?count=5&year=2020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3L));
    }

    @Test
    void getPopularFilms_withGenreAndYearFilter_success() throws Exception {
        Film filteredFilm = new Film();
        filteredFilm.setId(4L);
        filteredFilm.setName("Filtered Film");
        filteredFilm.setMpa(new Mpa(1L, "G"));

        // Тест с фильтрами по жанру и году
        when(filmService.getPopularFilms(3, 2, 2019)).thenReturn(List.of(filteredFilm));

        mockMvc.perform(get("/films/popular?count=3&genreId=2&year=2019"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(4L));
    }

    @Test
    void getPopularFilms_invalidCountParameter() throws Exception {
        // Тест с невалидным параметром count
        when(filmService.getPopularFilms(-1, null, null))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.ValidationException("Количество популярных фильмов должно быть больше 0"));

        mockMvc.perform(get("/films/popular?count=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPopularFilms_invalidGenreIdParameter() throws Exception {
        // Тест с невалидным параметром genreId
        when(filmService.getPopularFilms(5, -1, null))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.ValidationException("ID жанра должен быть положительным числом"));

        mockMvc.perform(get("/films/popular?count=5&genreId=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPopularFilms_invalidYearParameter() throws Exception {
        // Тест с невалидным параметром year
        when(filmService.getPopularFilms(5, null, 1800))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.ValidationException("Год должен быть в диапазоне от 1895 до текущего года"));

        mockMvc.perform(get("/films/popular?count=5&year=1800"))
                .andExpect(status().isBadRequest());
    }
}