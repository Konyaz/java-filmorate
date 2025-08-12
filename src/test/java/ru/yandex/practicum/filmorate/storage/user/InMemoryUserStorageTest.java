package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserStorageTest {
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
    }

    @Test
    void createUser_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        assertNotNull(created.getId());
        assertEquals("test@example.com", created.getEmail());
        assertNotNull(created.getFriends());
        assertTrue(created.getFriends().isEmpty());
    }

    @Test
    void updateUser_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        created.setLogin("updated");
        User updated = userStorage.update(created);

        assertEquals("updated", updated.getLogin());
        assertEquals(created.getId(), updated.getId());
    }

    @Test
    void updateUser_notFound_throwsException() {
        User user = new User();
        user.setId(999L);
        user.setEmail("test@example.com");

        assertThrows(NotFoundException.class, () -> userStorage.update(user));
    }

    @Test
    void getAllUsers_returnsList() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.getAll();

        assertEquals(2, users.size());
    }

    @Test
    void getById_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        User found = userStorage.getById(created.getId()).orElseThrow();

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getById_notFound_returnsEmpty() {
        assertTrue(userStorage.getById(999L).isEmpty());
    }
}