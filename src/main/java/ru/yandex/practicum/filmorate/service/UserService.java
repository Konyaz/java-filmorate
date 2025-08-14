package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        if (user.getLogin() == null) {
            log.error("Логин пользователя не указан");
            throw new ValidationException("Логин обязателен");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        try {
            log.info("Создание пользователя: {}", user);
            return userStorage.create(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при создании пользователя: дубликат email {}", user.getEmail(), e);
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }
    }

    public User update(User user) {
        if (user.getLogin() == null) {
            log.error("Логин пользователя не указан");
            throw new ValidationException("Логин обязателен");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (userStorage.getById(user.getId()).isEmpty()) {
            log.error("Пользователь с ID {} не найден", user.getId());
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }
        try {
            log.info("Обновление пользователя: {}", user);
            return userStorage.update(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при обновлении пользователя: дубликат email {}", user.getEmail(), e);
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }
    }

    public List<User> getAll() {
        log.info("Получение всех пользователей");
        return userStorage.getAll();
    }

    public User getById(Long id) {
        log.info("Получение пользователя с ID: {}", id);
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public void addFriend(Long id, Long friendId) {
        User user = getById(id);
        getById(friendId);
        if (user.getFriends() != null && user.getFriends().contains(friendId)) {
            log.warn("Пользователь {} уже является другом пользователя {}", friendId, id);
            throw new ValidationException("Пользователь с ID " + friendId + " уже является другом");
        }
        userStorage.addFriend(id, friendId);
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        user.getFriends().add(friendId);
        userStorage.update(user);
    }

    public void removeFriend(Long id, Long friendId) {
        User user = getById(id);
        getById(friendId);
        if (user.getFriends() != null && user.getFriends().contains(friendId)) {
            userStorage.removeFriend(id, friendId);
            user.getFriends().remove(friendId);
            userStorage.update(user);
        }
    }

    public List<User> getFriends(Long id) {
        getById(id);
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        getById(id);
        getById(otherId);
        return userStorage.getCommonFriends(id, otherId);
    }
}