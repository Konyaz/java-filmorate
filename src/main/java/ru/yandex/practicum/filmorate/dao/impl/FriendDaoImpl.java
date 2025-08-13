package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FriendDao;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendDaoImpl implements FriendDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<Long> getFriends(Long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("friend_id"), userId);
    }

    @Override
    public List<Long> getCommonFriends(Long userId, Long otherUserId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ? AND friend_id IN (SELECT friend_id FROM friends WHERE user_id = ?)";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("friend_id"), userId, otherUserId);
    }
}