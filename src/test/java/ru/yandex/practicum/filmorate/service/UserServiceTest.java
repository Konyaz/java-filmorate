package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.dao.FriendDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserStorage userStorage;

    @Mock
    private FriendDao friendDao;

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
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        when(userStorage.create(user)).thenReturn(user);

        User result = userService.create(user);

        assertEquals(user, result);
        verify(userStorage, times(1)).create(user);
    }

    @Test
    void updateUser_success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
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
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(user));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, never()).update(user);
    }

    @Test
    void getAllUsers_success() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
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
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
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
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@example.com");
        friend.setLogin("friendlogin");
        friend.setBirthday(LocalDate.of(1991, 1, 1));

        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getById(2L)).thenReturn(Optional.of(friend));
        when(friendDao.getFriends(1L)).thenReturn(List.of());
        doNothing().when(friendDao).addFriend(1L, 2L);

        userService.addFriend(1L, 2L);

        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(friendDao, times(1)).getFriends(1L);
        verify(friendDao, times(1)).addFriend(1L, 2L);
    }

    @Test
    void addFriend_userNotFound_throwsNotFoundException() {
        when(userStorage.getById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.addFriend(1L, 2L));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, never()).getById(2L);
        verify(friendDao, never()).addFriend(anyLong(), anyLong());
    }

    @Test
    void addFriend_friendNotFound_throwsNotFoundException() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.addFriend(1L, 2L));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(friendDao, never()).addFriend(anyLong(), anyLong());
    }

    @Test
    void removeFriend_success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@example.com");
        friend.setLogin("friendlogin");
        friend.setBirthday(LocalDate.of(1991, 1, 1));

        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getById(2L)).thenReturn(Optional.of(friend));
        doNothing().when(friendDao).removeFriend(1L, 2L);

        userService.removeFriend(1L, 2L);

        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(friendDao, times(1)).removeFriend(1L, 2L);
    }

    @Test
    void removeFriend_notFriend_doesNothing() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@example.com");
        friend.setLogin("friendlogin");
        friend.setBirthday(LocalDate.of(1991, 1, 1));

        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getById(2L)).thenReturn(Optional.of(friend));
        doNothing().when(friendDao).removeFriend(1L, 2L);

        userService.removeFriend(1L, 2L);

        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(friendDao, times(1)).removeFriend(1L, 2L);
    }

    @Test
    void getFriends_success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@example.com");
        friend.setLogin("friendlogin");
        friend.setBirthday(LocalDate.of(1991, 1, 1));

        when(userStorage.getById(1L)).thenReturn(Optional.of(user));
        when(friendDao.getFriends(1L)).thenReturn(List.of(2L));
        when(userStorage.getById(2L)).thenReturn(Optional.of(friend));

        List<User> result = userService.getFriends(1L);

        assertEquals(1, result.size());
        assertEquals(friend, result.get(0));
        verify(userStorage, times(1)).getById(1L);
        verify(friendDao, times(1)).getFriends(1L);
        verify(userStorage, times(1)).getById(2L);
    }

    @Test
    void getCommonFriends_success() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User common = new User();
        common.setId(3L);
        common.setEmail("common@example.com");
        common.setLogin("common");
        common.setBirthday(LocalDate.of(1992, 1, 1));

        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getById(2L)).thenReturn(Optional.of(user2));
        when(friendDao.getCommonFriends(1L, 2L)).thenReturn(List.of(3L));
        when(userStorage.getById(3L)).thenReturn(Optional.of(common));

        List<User> result = userService.getCommonFriends(1L, 2L);

        assertEquals(1, result.size());
        assertEquals(common, result.get(0));
        verify(userStorage, times(1)).getById(1L);
        verify(userStorage, times(1)).getById(2L);
        verify(friendDao, times(1)).getCommonFriends(1L, 2L);
        verify(userStorage, times(1)).getById(3L);
    }
}