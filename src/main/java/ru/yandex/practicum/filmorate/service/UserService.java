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
        // Проверка на null перед вызовом методов
        if (user.getLogin() == null || user.getLogin().isBlank()) {
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
            log.error("Пользователь с email {} уже существует", user.getEmail());
            throw new ValidationException("Пользователь с таким email уже существует");
        }
    }

    public User update(User user) {
        // Проверка на null перед вызовом методов
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Логин пользователя не указан");
            throw new ValidationException("Логин обязателен");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        log.info("Обновление пользователя: {}", user);
        return userStorage.update(user);
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
        User friend = getById(friendId);

        // Инициализируем friends, если он null
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        if (user.getFriends().contains(friendId)) {
            log.warn("Пользователь {} уже является другом пользователя {}", friendId, id);
            throw new ValidationException("Пользователь с ID " + friendId + " уже является другом");
        }

        userStorage.addFriend(id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        User user = getById(id);
        User friend = getById(friendId);

        // Инициализируем friends, если он null
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        if (user.getFriends().contains(friendId)) {
            userStorage.removeFriend(id, friendId);
        }
    }

    public List<User> getFriends(Long id) {
        getById(id); // Проверка существования пользователя
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        getById(id); // Проверка существования пользователя 1
        getById(otherId); // Проверка существования пользователя 2
        return userStorage.getCommonFriends(id, otherId);
    }
}
