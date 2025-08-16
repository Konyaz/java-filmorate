package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FriendDao;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    private final FriendDao friendDao;

    public User create(@Valid User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Создание пользователя: {}", user);
        return userStorage.create(user);
    }

    public User update(@Valid User user) {
        userStorage.getById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + user.getId() + " не найден"));
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
        validateUserIds(id, friendId);
        if (friendDao.isFriendshipExists(id, friendId)) {
            throw new ValidationException("Пользователь " + friendId + " уже в друзьях у " + id);
        }

        friendDao.addFriend(id, friendId, "unconfirmed");
        log.info("Пользователь {} добавил в друзья {} (неподтверждённая)", id, friendId);
    }

    public void confirmFriend(Long id, Long friendId) {
        validateUserIds(id, friendId);
        friendDao.confirmFriendship(id, friendId);
        log.info("Пользователь {} подтвердил дружбу с {}", id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        validateUserIds(id, friendId);
        friendDao.removeFriend(id, friendId);
        log.info("Пользователь {} удалил из друзей {}", id, friendId);
    }

    public List<User> getFriends(Long id) {
        getById(id);
        return friendDao.getFriends(id).stream()
                .map(this::getById)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        validateUserIds(id, otherId);
        return friendDao.getCommonFriends(id, otherId).stream()
                .map(this::getById)
                .collect(java.util.stream.Collectors.toList());
    }

    private void validateUserIds(Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }
        getById(id);
        getById(friendId);
    }
}