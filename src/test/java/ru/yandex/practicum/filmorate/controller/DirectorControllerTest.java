package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DirectorController.class)
class DirectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DirectorService directorService;

    private Director director;

    @BeforeEach
    void setUp() {
        director = new Director();
        director.setId(1L);
        director.setName("Test Director");
    }

    @Test
    void createDirector_success() throws Exception {
        when(directorService.create(any(Director.class))).thenReturn(director);

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Director"));
    }

    @Test
    void createDirector_withNullName_shouldReturnBadRequest() throws Exception {
        Director invalidDirector = new Director();
        invalidDirector.setName(null);

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDirector)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDirector_withEmptyName_shouldReturnBadRequest() throws Exception {
        Director invalidDirector = new Director();
        invalidDirector.setName("");

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDirector)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDirector_success() throws Exception {
        when(directorService.update(any(Director.class))).thenReturn(director);

        mockMvc.perform(put("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Director"));
    }

    @Test
    void updateDirector_withInvalidId_shouldReturnNotFound() throws Exception {
        Director nonExistentDirector = new Director();
        nonExistentDirector.setId(999L);
        nonExistentDirector.setName("Non-existent");

        when(directorService.update(any(Director.class)))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Режиссер не найден"));

        mockMvc.perform(put("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentDirector)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllDirectors_success() throws Exception {
        Director director2 = new Director();
        director2.setId(2L);
        director2.setName("Second Director");

        when(directorService.getAll()).thenReturn(List.of(director, director2));

        mockMvc.perform(get("/directors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getAllDirectors_emptyList_success() throws Exception {
        when(directorService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/directors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getDirectorById_success() throws Exception {
        when(directorService.getById(1L)).thenReturn(director);

        mockMvc.perform(get("/directors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Director"));
    }

    @Test
    void getDirectorById_notFound() throws Exception {
        when(directorService.getById(999L))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Режиссер не найден"));

        mockMvc.perform(get("/directors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDirector_success() throws Exception {
        doNothing().when(directorService).deleteById(1L);

        mockMvc.perform(delete("/directors/1"))
                .andExpect(status().isOk());

        verify(directorService, times(1)).deleteById(1L);
    }

    @Test
    void deleteDirector_notFound() throws Exception {
        doThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Режиссер не найден"))
                .when(directorService).deleteById(999L);

        mockMvc.perform(delete("/directors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDirector_withVeryLongName_shouldReturnBadRequest() throws Exception {
        Director invalidDirector = new Director();
        invalidDirector.setName("A".repeat(256)); // Слишком длинное имя

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDirector)))
                .andExpect(status().isBadRequest());
    }
}
