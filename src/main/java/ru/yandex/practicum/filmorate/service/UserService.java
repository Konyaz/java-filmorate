package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FriendDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendDao friendDao;

    public User create(User user) {
        validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Создание пользователя: {}", user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validate(user);
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
        getById(id);
        getById(friendId);

        if (friendDao.getFriends(id).contains(friendId)) {
            throw new ValidationException("Пользователь " + friendId + " уже в друзьях у " + id);
        }

        friendDao.addFriend(id, friendId);
        log.info("Пользователь {} добавил в друзья {}", id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        getById(id);
        getById(friendId);

        friendDao.removeFriend(id, friendId);
        log.info("Пользователь {} удалил из друзей {}", id, friendId);
    }

    public List<User> getFriends(Long id) {
        getById(id);
        return friendDao.getFriends(id).stream()
                .map(uid -> userStorage.getById(uid)
                        .orElseThrow(() -> new NotFoundException("Пользователь с ID " + uid + " не найден")))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        getById(id);
        getById(otherId);
        return friendDao.getCommonFriends(id, otherId).stream()
                .map(uid -> userStorage.getById(uid)
                        .orElseThrow(() -> new NotFoundException("Пользователь с ID " + uid + " не найден")))
                .collect(Collectors.toList());
    }

    private void validate(User user) {
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
    }
}
