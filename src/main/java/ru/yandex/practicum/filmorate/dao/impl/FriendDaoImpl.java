package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FriendDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FriendDaoImpl implements FriendDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriend(Long userId, Long friendId, String status) {
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, status);
    }

    @Override
    public void confirmFriendship(Long userId, Long friendId) {
        String sql = "UPDATE friends SET status = 'confirmed' WHERE user_id = ? AND friend_id = ?";
        int updated = jdbcTemplate.update(sql, friendId, userId);
        if (updated == 0) {
            throw new NotFoundException("Запрос на дружбу не найден");
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sql, userId, friendId, friendId, userId);
    }

    @Override
    public List<Long> getFriends(Long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ? AND status = 'confirmed'";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    @Override
    public List<Long> getCommonFriends(Long userId, Long otherId) {
        String sql = "SELECT f.friend_id " +
                "FROM friends f " +
                "JOIN friends f2 ON f.friend_id = f2.friend_id " +
                "WHERE f.user_id = ? AND f2.user_id = ? AND f.status = 'confirmed' AND f2.status = 'confirmed'";
        return jdbcTemplate.queryForList(sql, Long.class, userId, otherId);
    }

    @Override
    public boolean isFriendshipExists(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}