package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Sql(scripts = "classpath:schema.sql")
class UserDbStorageTest {
    @Autowired
    private UserDbStorage userStorage;

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

        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setLogin("anotherlogin");
        anotherUser.setBirthday(LocalDate.of(1995, 5, 5));
        userStorage.create(anotherUser);

        List<User> users = userStorage.getAll();
        assertEquals(2, users.size());
    }

    @Test
    void testUpdateNonExistentUser() {
        testUser.setId(999L);
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class,
                () -> userStorage.update(testUser));
    }

    @Test
    void testGetNonExistentUser() {
        Optional<User> found = userStorage.getById(999L);
        assertTrue(found.isEmpty());
    }
}