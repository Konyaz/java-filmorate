package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
        user.setId(1L);
        when(userStorage.create(user)).thenReturn(user);

        User result = userService.create(user);

        assertEquals(user, result);
        verify(userStorage, times(1)).create(user);
    }

    @Test
    void updateUser_success() {
        User user = new User();
        user.setId(1L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.update(user)).thenReturn(user);

        User result = userService.update(user);

        assertEquals(user, result);
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).update(user);
    }

    @Test
    void updateUser_notFound_throwsNotFoundException() {
        User user = new User();
        user.setId(1L);
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(user));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, never()).update(user);
    }

    @Test
    void getAllUsers_success() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        when(userStorage.getAll()).thenReturn(List.of(user1, user2));

        List<User> result = userService.getAll();

        assertEquals(2, result.size());
        assertEquals(user1, result.get(0));
        assertEquals(user2, result.get(1));
        verify(userStorage, times(1)).getAll();
    }

    @Test
    void getUserById_success() {
        User user = new User();
        user.setId(1L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertEquals(user, result);
        verify(userStorage, times(1)).getById(1L);
    }

    @Test
    void getUserById_notFound_throwsNotFoundException() {
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(1L));
        verify(userStorage, times(1)).getById(1L);
    }

    @Test
    void addFriend_success() {
        User user = new User();
        user.setId(1L);
        user.setFriends(new HashSet<>());
        User friend = new User();
        friend.setId(2L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.update(user)).thenReturn(user);

        userService.addFriend(1L, 2L);

        assertTrue(user.getFriends().contains(2L));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(userStorage, times(1)).addFriend(1L, 2L);
        verify(userStorage, times(1)).update(user);
    }

    @Test
    void addFriend_userNotFound_throwsNotFoundException() {
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.addFriend(1L, 2L));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, never()).getById(2L);
        verify(userStorage, never()).addFriend(anyLong(), anyLong());
        verify(userStorage, never()).update(any(User.class));
    }

    @Test
    void addFriend_friendNotFound_throwsNotFoundException() {
        User user = new User();
        user.setId(1L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.addFriend(1L, 2L));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(userStorage, never()).addFriend(anyLong(), anyLong());
        verify(userStorage, never()).update(any(User.class));
    }

    @Test
    void removeFriend_success() {
        User user = new User();
        user.setId(1L);
        user.setFriends(new HashSet<>(Set.of(2L)));
        User friend = new User();
        friend.setId(2L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.update(user)).thenReturn(user);

        userService.removeFriend(1L, 2L);

        assertFalse(user.getFriends().contains(2L));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(userStorage, times(1)).removeFriend(1L, 2L);
        verify(userStorage, times(1)).update(user);
    }

    @Test
    void removeFriend_notFriend_doesNothing() {
        User user = new User();
        user.setId(1L);
        user.setFriends(new HashSet<>()); // Пустой set
        User friend = new User();
        friend.setId(2L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getById(2L)).thenReturn(Optional.of(friend));

        userService.removeFriend(1L, 2L);

        assertTrue(user.getFriends().isEmpty());
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(userStorage, never()).removeFriend(anyLong(), anyLong());
        verify(userStorage, never()).update(any(User.class));
    }

    @Test
    void getFriends_success() {
        User user = new User();
        user.setId(1L);
        User friend = new User();
        friend.setId(2L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getFriends(1L)).thenReturn(List.of(friend));

        List<User> result = userService.getFriends(1L);

        assertEquals(1, result.size());
        assertEquals(friend, result.get(0));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getFriends(1L);
    }

    @Test
    void getCommonFriends_success() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User common = new User();
        common.setId(3L);
        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.getCommonFriends(1L, 2L)).thenReturn(List.of(common));

        List<User> result = userService.getCommonFriends(1L, 2L);

        assertEquals(1, result.size());
        assertEquals(common, result.get(0));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(userStorage, times(1)).getCommonFriends(1L, 2L);
    }
}