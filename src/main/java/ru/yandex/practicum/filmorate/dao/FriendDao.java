package ru.yandex.practicum.filmorate.dao;

import java.util.List;

public interface FriendDao {
    void addFriend(Long userId, Long friendId, String status);

    void confirmFriendship(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<Long> getFriends(Long userId);

    List<Long> getCommonFriends(Long userId, Long otherId);

    boolean isFriendshipExists(Long userId, Long friendId);
}