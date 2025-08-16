package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private FriendService friendService;  // Добавлен FriendService

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
    }

    @Test
    void createUser_success() throws Exception {
        when(userService.create(any(User.class))).thenReturn(user1);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(user1)));
    }

    @Test
    void updateUser_success() throws Exception {
        when(userService.update(any(User.class))).thenReturn(user1);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user1)));
    }

    @Test
    void getUsers_success() throws Exception {
        when(userService.getAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(user1, user2))));
    }

    @Test
    void getUserById_success() throws Exception {
        when(userService.getById(1L)).thenReturn(user1);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user1)));
    }

    @Test
    void addFriend_success() throws Exception {
        doNothing().when(friendService).addFriend(1L, 2L);  // Используем FriendService

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    void removeFriend_success() throws Exception {
        doNothing().when(friendService).removeFriend(1L, 2L);  // Используем FriendService

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    void getFriends_success() throws Exception {
        when(friendService.getFriends(1L)).thenReturn(List.of(user2));  // Используем FriendService

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(user2))));
    }

    @Test
    void getCommonFriends_success() throws Exception {
        User commonFriend = new User();
        commonFriend.setId(3L);
        commonFriend.setEmail("user3@example.com");
        commonFriend.setLogin("user3");
        commonFriend.setBirthday(LocalDate.of(1992, 1, 1));

        when(friendService.getCommonFriends(1L, 2L)).thenReturn(List.of(commonFriend));  // Используем FriendService

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(commonFriend))));
    }
}