package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.LikeDao;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeDaoImpl implements LikeDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addLike(Long filmId, Long userId) {
        final String del_sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(del_sql, filmId, userId);

        final String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Like added: filmId={}, userId={}", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        final String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(sql, filmId, userId);
        if (deleted > 0) {
            log.info("Like removed: filmId={}, userId={}", filmId, userId);
        } else {
            log.info("Like not found: filmId={}, userId={}", filmId, userId);
        }
    }

    @Override
    public List<Long> getLikes(Long filmId) {
        final String sql = "SELECT user_id FROM likes WHERE film_id = ? ORDER BY user_id";
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong("user_id"), filmId);
    }

    @Override
    public List<Long> getUserLikedFilmsId(Long userId) {
        final String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    @Override
    public List<Long> findSimilarUsers(Long userId) {
        final String sql = """
                SELECT l2.user_id
                FROM likes l1
                JOIN likes l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = ? AND l2.user_id != ?
                GROUP BY l2.user_id
                ORDER BY COUNT(*) DESC
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong("user_id"), userId, userId);
    }
}