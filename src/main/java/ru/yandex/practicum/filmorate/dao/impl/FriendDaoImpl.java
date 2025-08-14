package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FriendDaoImpl {
    private final JdbcTemplate jdbcTemplate;

    public void addFriend(Long userId, Long friendId) {
        // H2-совместимый upsert
        final String sql = "MERGE INTO friends (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Friend link added: userId={} -> friendId={}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        final String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Friend link removed: userId={} -/-> friendId={}", userId, friendId);
    }

    public List<Long> getFriends(Long userId) {
        final String sql = "SELECT friend_id FROM friends WHERE user_id = ? ORDER BY friend_id";
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong("friend_id"), userId);
    }

    public List<Long> getCommonFriends(Long userId, Long otherUserId) {
        final String sql =
                "SELECT f1.friend_id " +
                        "FROM friends f1 " +
                        "JOIN friends f2 ON f1.friend_id = f2.friend_id " +
                        "WHERE f1.user_id = ? AND f2.user_id = ? " +
                        "ORDER BY f1.friend_id";
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong(1), userId, otherUserId);
    }
}
