package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    List<User> getAll();

    Optional<User> getById(Long id);

    void addFriend(Long id, Long friendId);

    void removeFriend(Long id, Long friendId);

    void confirmFriend(Long id, Long friendId); // Added missing method

    List<User> getFriends(Long id);

    List<User> getCommonFriends(Long id, Long otherId);
}