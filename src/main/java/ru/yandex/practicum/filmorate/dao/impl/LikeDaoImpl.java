package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeDaoImpl {
    private final JdbcTemplate jdbcTemplate;

    public void addLike(Long filmId, Long userId) {
        // H2-совместимый upsert
        final String sql = "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Like added: filmId={}, userId={}", filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        final String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Like removed: filmId={}, userId={}", filmId, userId);
    }

    public List<Long> getLikes(Long filmId) {
        final String sql = "SELECT user_id FROM likes WHERE film_id = ? ORDER BY user_id";
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong("user_id"), filmId);
    }
}
