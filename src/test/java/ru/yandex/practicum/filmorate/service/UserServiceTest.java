package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private FriendService friendService;

    private Long userId1;
    private Long userId2;
    private Long userId3;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = userService.create(user1);
        userId1 = created1.getId();

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User created2 = userService.create(user2);
        userId2 = created2.getId();

        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setBirthday(LocalDate.of(1992, 1, 1));
        User created3 = userService.create(user3);
        userId3 = created3.getId();
    }

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userService.create(user);
        assertNotNull(createdUser.getId());
        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("update@example.com");
        user.setLogin("updatelogin");
        user.setName("Update User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userService.create(user);
        Long id = createdUser.getId();

        createdUser.setLogin("newlogin");
        User updatedUser = userService.update(createdUser);
        assertEquals("newlogin", updatedUser.getLogin());
    }

    @Test
    void shouldThrowNotFoundExceptionOnUpdateNonExistentUser() {
        User user = new User();
        user.setId(999L);
        user.setEmail("nonexistent@example.com");
        user.setLogin("nonexistent");
        user.setName("Nonexistent");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(NotFoundException.class, () -> userService.update(user));
    }

    @Test
    void shouldGetAllUsers() {
        List<User> users = userService.getAll();
        assertFalse(users.isEmpty());
    }

    @Test
    void shouldGetUserById() {
        User user = userService.getById(userId1);
        assertEquals("user1@example.com", user.getEmail());
    }

    @Test
    void shouldThrowNotFoundExceptionOnGetNonExistentUser() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void shouldAddAndConfirmFriend() {
        friendService.addFriend(userId1, userId2);
        friendService.confirmFriend(userId2, userId1);

        List<User> friends = friendService.getFriends(userId1);
        assertEquals(1, friends.size());
        assertEquals(userId2, friends.get(0).getId());
    }

    @Test
    void shouldThrowValidationExceptionOnSelfFriendship() {
        assertThrows(ValidationException.class, () -> friendService.addFriend(userId1, userId1));
    }

    @Test
    void shouldRemoveFriend() {
        friendService.addFriend(userId1, userId2);
        friendService.confirmFriend(userId2, userId1);
        friendService.removeFriend(userId1, userId2);

        List<User> friends = friendService.getFriends(userId1);
        assertTrue(friends.isEmpty());
    }

    @Test
    void shouldGetCommonFriends() {
        friendService.addFriend(userId1, userId3);
        friendService.confirmFriend(userId3, userId1);
        friendService.addFriend(userId2, userId3);
        friendService.confirmFriend(userId3, userId2);

        List<User> commonFriends = friendService.getCommonFriends(userId1, userId2);
        assertEquals(1, commonFriends.size());
        assertEquals(userId3, commonFriends.get(0).getId());
    }
}