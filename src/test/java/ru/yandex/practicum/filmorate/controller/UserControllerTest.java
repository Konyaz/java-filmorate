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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setLogin("test");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setFriends(Set.of());
    }

    @Test
    void createUser_success() throws Exception {
        when(userService.create(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void createUser_invalidData_returnsBadRequest() throws Exception {
        User invalidUser = new User();
        invalidUser.setEmail("");
        invalidUser.setLogin("test");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_success() throws Exception {
        when(userService.update(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateUser_notFound_throwsException() throws Exception {
        when(userService.update(any(User.class))).thenThrow(new NotFoundException("Пользователь с ID 1 не найден"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 1 не найден"));
    }

    @Test
    void getAllUsers_success() throws Exception {
        when(userService.getAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getUserById_success() throws Exception {
        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getUserById_notFound_throwsException() throws Exception {
        when(userService.getById(1L)).thenThrow(new NotFoundException("Пользователь с ID 1 не найден"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 1 не найден"));
    }

    @Test
    void addFriend_success() throws Exception {
        doNothing().when(userService).addFriend(1L, 2L);

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    void addFriend_notFound_throwsException() throws Exception {
        doThrow(new NotFoundException("Пользователь с ID 2 не найден")).when(userService).addFriend(1L, 2L);

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 2 не найден"));
    }

    @Test
    void addFriend_alreadyFriend_returnsBadRequest() throws Exception {
        doThrow(new ValidationException("Пользователь уже добавлен в друзья")).when(userService).addFriend(1L, 2L);

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Пользователь уже добавлен в друзья"));
    }

    @Test
    void removeFriend_success() throws Exception {
        doNothing().when(userService).removeFriend(1L, 2L);

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    void removeFriend_notFriend_returnsBadRequest() throws Exception {
        doThrow(new ValidationException("Пользователь не в списке друзей")).when(userService).removeFriend(1L, 2L);

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Пользователь не в списке друзей"));
    }

    @Test
    void getFriends_success() throws Exception {
        when(userService.getFriends(1L)).thenReturn(List.of(user));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getCommonFriends_success() throws Exception {
        when(userService.getCommonFriends(1L, 2L)).thenReturn(List.of(user));

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}