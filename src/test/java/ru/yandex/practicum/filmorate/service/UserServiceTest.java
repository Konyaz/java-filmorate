package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserStorage userStorage;
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

    @Test
    void addFriend_success() {
        User user1 = new User();
        user1.setId(1L);
        user1.setFriends(new HashSet<>());
        User user2 = new User();
        user2.setId(2L);
        user2.setFriends(new HashSet<>());
        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getById(2L)).thenReturn(Optional.of(user2));

        userService.addFriend(1L, 2L);

        assertTrue(user1.getFriends().contains(2L));
        assertTrue(user2.getFriends().contains(1L));
        verify(userStorage, times(2)).update(any(User.class));
    }

    @Test
    void removeFriend_success() {
        User user1 = new User();
        user1.setId(1L);
        user1.setFriends(new HashSet<>(Set.of(2L)));
        User user2 = new User();
        user2.setId(2L);
        user2.setFriends(new HashSet<>(Set.of(1L)));
        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getById(2L)).thenReturn(Optional.of(user2));

        userService.removeFriend(1L, 2L);

        assertFalse(user1.getFriends().contains(2L));
        assertFalse(user2.getFriends().contains(1L));
        verify(userStorage, times(2)).update(any(User.class));
    }

    @Test
    void removeFriend_notFriend_throwsException() {
        User user1 = new User();
        user1.setId(1L);
        user1.setFriends(new HashSet<>());
        User user2 = new User();
        user2.setId(2L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getById(2L)).thenReturn(Optional.of(user2));

        assertThrows(ValidationException.class, () -> userService.removeFriend(1L, 2L));

        verify(userStorage).getById(1L);
        verify(userStorage).getById(2L);
        verify(userStorage, never()).update(any(User.class));
    }

    @Test
    void getFriends_success() {
        User user = new User();
        user.setId(1L);
        user.setFriends(new HashSet<>(Set.of(2L)));
        User friend = new User();
        friend.setId(2L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getById(2L)).thenReturn(Optional.of(friend));

        List<User> friends = userService.getFriends(1L);

        assertEquals(1, friends.size());
        assertEquals(friend, friends.get(0));
    }

    @Test
    void getCommonFriends_success() {
        User user1 = new User();
        user1.setId(1L);
        user1.setFriends(new HashSet<>(Set.of(3L)));
        User user2 = new User();
        user2.setId(2L);
        user2.setFriends(new HashSet<>(Set.of(3L)));
        User common = new User();
        common.setId(3L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.getById(3L)).thenReturn(Optional.of(common));

        List<User> commonFriends = userService.getCommonFriends(1L, 2L);

        assertEquals(1, commonFriends.size());
        assertEquals(common, commonFriends.get(0));
    }
}