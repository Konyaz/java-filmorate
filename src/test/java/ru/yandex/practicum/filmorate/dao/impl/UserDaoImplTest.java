package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(UserDaoImpl.class)
class UserDaoImplTest {
    @Autowired
    private UserDaoImpl userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testCreateUser() {
        User created = userStorage.create(testUser);
        assertNotNull(created.getId());
        assertEquals("test@example.com", created.getEmail());
    }

    @Test
    void testUpdateUser() {
        User created = userStorage.create(testUser);
        created.setLogin("updatedlogin");

        User updated = userStorage.update(created);
        assertEquals("updatedlogin", updated.getLogin());
    }

    @Test
    void testGetUserById() {
        User created = userStorage.create(testUser);
        Optional<User> found = userStorage.getById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getEmail(), found.get().getEmail());
    }

    @Test
    void testGetAllUsers() {
        userStorage.create(testUser);
        List<User> users = userStorage.getAll();
        assertFalse(users.isEmpty());
    }

    @Test
    void testUpdateNonExistentUser() {
        testUser.setId(999L);
        assertThrows(NotFoundException.class,
                () -> userStorage.update(testUser));
    }

    @Test
    void testGetNonExistentUser() {
        Optional<User> found = userStorage.getById(999L);
        assertTrue(found.isEmpty());
    }
}