package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        getById(user.getId()); // Проверка существования пользователя
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getById(userId);
        getById(friendId); // Проверка существования друга
        user.getFriends().add(friendId);
        userStorage.update(user);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getById(userId);
        getById(friendId); // Проверка существования друга
        user.getFriends().remove(friendId);
        userStorage.update(user);
    }

    public List<User> getFriends(Long userId) {
        User user = getById(userId);
        return user.getFriends().stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user1 = getById(userId);
        User user2 = getById(otherId);
        return user1.getFriends().stream()
                .filter(friendId -> user2.getFriends().contains(friendId))
                .map(this::getById)
                .collect(Collectors.toList());
    }
}