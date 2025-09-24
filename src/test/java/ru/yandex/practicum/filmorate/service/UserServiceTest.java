package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserDao userStorage;
    @Mock
    private EventDao eventStorage;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        when(userStorage.create(user)).thenReturn(user);

        User created = userService.create(user);

        assertEquals(user, created);
        verify(userStorage).create(user);
    }

    @Test
    void updateUser_success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.update(user)).thenReturn(user);

        User updated = userService.update(user);

        assertEquals(user, updated);
        verify(userStorage).getById(1L);
        verify(userStorage).update(user);
    }

    @Test
    void updateUser_notFound_throwsException() {
        User user = new User();
        user.setId(1L);
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(user));
        verify(userStorage).getById(1L);
        verify(userStorage, never()).update(any(User.class));
    }

    @Test
    void getAllUsers_success() {
        User user = new User();
        user.setId(1L);
        when(userStorage.getAll()).thenReturn(List.of(user));

        List<User> users = userService.getAll();

        assertEquals(1, users.size());
        verify(userStorage).getAll();
    }

    @Test
    void getById_success() {
        User user = new User();
        user.setId(1L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));

        User found = userService.getById(1L);

        assertEquals(user, found);
        verify(userStorage).getById(1L);
    }

    @Test
    void getById_notFound_throwsException() {
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(1L));
    }


}