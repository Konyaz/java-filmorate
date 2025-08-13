package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FriendDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendDao friendDao;

    @Autowired
    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage,
            FriendDao friendDao) {

        this.userStorage = userStorage;
        this.friendDao = friendDao;
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
        // Проверяем существование пользователей
        User user = getById(id);
        User friend = getById(friendId);

        // Проверяем, что пользователи не пытаются добавить сами себя
        if (id.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }

        // Проверяем, не добавлен ли уже друг
        if (friendDao.getFriends(id).contains(friendId)) {
            throw new ValidationException("Пользователь с ID " + friendId + " уже добавлен в друзья");
        }

        friendDao.addFriend(id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        getById(id); // Проверка существования пользователя
        getById(friendId); // Проверка существования друга

        // Проверяем, существует ли дружба
        if (!friendDao.getFriends(id).contains(friendId)) {
            throw new ValidationException("Пользователь с ID " + friendId + " не найден в друзьях");
        }

        friendDao.removeFriend(id, friendId);
    }

    public List<User> getFriends(Long id) {
        getById(id); // Проверка существования пользователя
        return friendDao.getFriends(id).stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        getById(id); // Проверка существования пользователей
        getById(otherId);

        return friendDao.getCommonFriends(id, otherId).stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }
}