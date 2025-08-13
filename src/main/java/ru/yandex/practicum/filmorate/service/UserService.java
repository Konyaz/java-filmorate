package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
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
        User user = getById(id); // Проверка существования пользователя
        getById(friendId); // Проверка существования друга
        userStorage.addFriend(id, friendId); // Добавляем друга
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        user.getFriends().add(friendId);
        userStorage.update(user); // Обновляем пользователя
    }

    public void removeFriend(Long id, Long friendId) {
        User user = getById(id); // Проверка существования пользователя
        getById(friendId); // Проверка существования друга
        if (user.getFriends() != null && user.getFriends().contains(friendId)) {
            userStorage.removeFriend(id, friendId);
            user.getFriends().remove(friendId);
            userStorage.update(user);
        }
    }

    public List<User> getFriends(Long id) {
        getById(id); // Проверка существования пользователя
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        getById(id); // Проверка существования пользователя
        getById(otherId); // Проверка существования другого пользователя
        return userStorage.getCommonFriends(id, otherId);
    }
}