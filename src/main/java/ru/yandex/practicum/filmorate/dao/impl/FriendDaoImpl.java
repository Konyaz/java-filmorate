package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FriendDao;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FriendDaoImpl implements FriendDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriend(Long userId, Long friendId, String status) {
        final String sql = "MERGE INTO friends (user_id, friend_id, status) KEY (user_id, friend_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, status);
        log.info("Friend link added: userId={} -> friendId={} with status={}", userId, friendId, status);
    }

    @Override
    public void confirmFriend(Long userId, Long friendId) {
        final String sql = "MERGE INTO friends (user_id, friend_id, status) KEY (user_id, friend_id) VALUES (?, ?, 'подтверждённая')";
        jdbcTemplate.update(sql, friendId, userId);
        final String updateSql = "UPDATE friends SET status = 'подтверждённая' WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(updateSql, userId, friendId);
        log.info("Friendship confirmed: userId={} <-> friendId={}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        final String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        jdbcTemplate.update(sql, friendId, userId);
        log.info("Friend link removed: userId={} -/-> friendId={}", userId, friendId);
    }

    @Override
    public List<Long> getFriends(Long userId) {
        final String sql = "SELECT friend_id FROM friends WHERE user_id = ? AND status = 'подтверждённая' ORDER BY friend_id";
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong("friend_id"), userId);
    }

    @Override
    public List<Long> getCommonFriends(Long userId, Long otherUserId) {
        final String sql =
                "SELECT f1.friend_id " +
                        "FROM friends f1 " +
                        "JOIN friends f2 ON f1.friend_id = f2.friend_id " +
                        "WHERE f1.user_id = ? AND f2.user_id = ? AND f1.status = 'подтверждённая' AND f2.status = 'подтверждённая' " +
                        "ORDER BY f1.friend_id";
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong(1), userId, otherUserId);
    }
}