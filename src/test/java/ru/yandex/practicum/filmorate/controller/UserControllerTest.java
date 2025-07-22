package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void shouldAddValidUser() {
        User user = createValidUser();
        User addedUser = userController.addUser(user);

        assertNotNull(addedUser.getId(), "Пользователь должен получить ID при добавлении");
        assertEquals(1, userController.getAllUsers().size(), "Должен быть ровно один пользователь в списке");
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = createValidUser();
        user.setName(null);
        User addedUser = userController.addUser(user);

        assertEquals(user.getLogin(), addedUser.getName(), "При пустом имени должен использоваться логин");
    }

    @Test
    void shouldUpdateUser() {
        User user = createValidUser();
        User addedUser = userController.addUser(user);

        addedUser.setEmail("updated@email.com");
        User updatedUser = userController.updateUser(addedUser);

        assertEquals("updated@email.com", updatedUser.getEmail(), "Email должен обновиться");
        assertEquals(1, userController.getAllUsers().size(), "Количество пользователей не должно измениться");
    }

    @Test
    void shouldFailWhenUpdateNonExistentUser() {
        User user = createValidUser();
        user.setId(999);

        assertThrows(ValidationException.class, () -> userController.updateUser(user),
                "Обновление несуществующего пользователя должно вызывать исключение");
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = createValidUser();
        User user2 = createValidUser();
        user2.setLogin("anotherLogin");

        userController.addUser(user1);
        userController.addUser(user2);

        Collection<User> users = userController.getAllUsers();
        assertEquals(2, users.size(), "Должно быть 2 пользователя в списке");
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}