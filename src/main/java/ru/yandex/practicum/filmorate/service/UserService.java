package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        // Проверка на null и пробелы в логине
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            log.error("Логин не может быть пустым или содержать пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы");
        }
        // Если имя не задано, используем логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Создание пользователя: {}", user);
        return userStorage.create(user);
    }

    public User update(User user) {
        // Проверка на null и пробелы в логине
        if (user.getLogin() != null && user.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        // Если имя не задано, используем логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
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
        // Убираем избыточную проверку на null, так как friends инициализирован в User.java
        if (user.getFriends().contains(friendId)) {
            log.warn("Пользователь {} уже является другом пользователя {}", friendId, id);
            throw new ValidationException("Пользователь с ID " + friendId + " уже является другом");
        }
        userStorage.addFriend(id, friendId);
        user.getFriends().add(friendId);
        userStorage.update(user);
    }

    public void removeFriend(Long id, Long friendId) {
        User user = getById(id);
        getById(friendId);
        // Убираем избыточную проверку на null, так как friends инициализирован в User.java
        if (user.getFriends().contains(friendId)) {
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
        User user = getById(id);
        User otherUser = getById(otherId);
        log.info("Получение общих друзей для пользователей {} и {}", id, otherId);
        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(userStorage::getById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
    }
}
