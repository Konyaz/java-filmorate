package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FriendDao;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendDao friendDao;

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        validateUserExists(userId, friendId);
        friendDao.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        validateUserExists(userId, friendId);
        friendDao.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        if (!userStorage.getById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        return friendDao.getFriends(userId).stream()
                .map(id -> userStorage.getById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден")))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        validateUserExists(userId, otherId);
        return friendDao.getCommonFriends(userId, otherId).stream()
                .map(id -> userStorage.getById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден")))
                .collect(Collectors.toList());
    }

    private void validateUserExists(Long userId, Long friendId) {
        if (!userStorage.getById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (!userStorage.getById(friendId).isPresent()) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }
    }
}