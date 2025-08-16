package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

public interface FriendDao {
    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId) throws NotFoundException;

    List<Long> getFriends(Long userId);

    List<Long> getCommonFriends(Long userId, Long otherId);

    boolean isFriendshipExists(Long userId, Long friendId);
}