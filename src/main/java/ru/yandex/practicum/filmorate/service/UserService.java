package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        if (userStorage.getById(user.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public void addFriend(Long id, Long friendId) {
        User user = getById(id);
        User friend = getById(friendId);
        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователь с ID " + friendId + " уже добавлен в друзья");
        }
        // Двусторонняя дружба, как ожидается в sprint.json
        user.addFriend(friendId);
        friend.addFriend(id);
        userStorage.update(user);
        userStorage.update(friend);
    }

    public void removeFriend(Long id, Long friendId) {
        User user = getById(id);
        User friend = getById(friendId);
        // Двусторонняя дружба: удаляем обоих пользователей друг у друга
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
        userStorage.update(user);
        userStorage.update(friend);
    }

    public List<User> getFriends(Long id) {
        User user = getById(id);
        return user.getFriends().stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        User user = getById(id);
        User other = getById(otherId);
        return user.getFriends().stream()
                .filter(friendId -> other.getFriends().contains(friendId))
                .map(this::getById)
                .collect(Collectors.toList());
    }
}