package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.mapper.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewDaoImpl implements ReviewDao {
    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper mapper;

    @Override
    public Collection<Review> list(int count) {
        String sql = """
            SELECT r.*,
            (SELECT COUNT(*) FROM review_likes WHERE review_id = r.id) -
             (SELECT COUNT(*) FROM review_dislikes WHERE review_id = r.id) AS useful
            FROM reviews r
            ORDER BY useful DESC
            LIMIT ?
        """;
        return jdbcTemplate.query(sql, mapper, count);
    }

    @Override
    public Collection<Review> filteredList(long filmId, int count) {
        String sql = """
            SELECT r`.*,
            (SELECT COUNT(*) FROM review_likes WHERE review_id = r.id) -
             (SELECT COUNT(*) FROM review_dislikes WHERE review_id = r.id) AS useful
            FROM reviews r
            WHERE film_id = ?
            ORDER BY useful DESC
            LIMIT ?`
        """;
        return jdbcTemplate.query(sql, mapper, filmId, count);
    }

    @Override
    public Review get(long id) {
        String sql = """
            SELECT r.*,
            (SELECT COUNT(*) FROM review_likes WHERE review_id = r.id) -
             (SELECT COUNT(*) FROM review_dislikes WHERE review_id = r.id) AS useful
            FROM reviews r
            WHERE id = ?
            LIMIT 1
        """;
        return jdbcTemplate.queryForObject(sql, mapper, id);
    }

    @Override
    public Review create(Review review) {
        String sql = """
            INSERT INTO reviews (content, is_positive, user_id, film_id)
            VALUES (?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            return stmt;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        review.setReviewId(id);
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = """
            UPDATE reviews SET
            content = ?, is_positive = ?, user_id = ?, film_id = ?
            WHERE id = ?
        """;

        jdbcTemplate.update(
                sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getReviewId()
        );

        return review;
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        jdbcTemplate.update(
                sql,
                id
        );
    }

    @Override
    public boolean exists(long id) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != 0;
    }

    public boolean likeExists(long id, long userId) {
        String sql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id, userId);
        return count != 0;
    }

    public boolean dislikeExists(long id, long userId) {
        String sql = "SELECT COUNT(*) FROM review_dislikes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id, userId);
        return count != 0;
    }

    @Override
    @Transactional
    public void addLike(long id, long userId) {
        String sql = "INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)";

        jdbcTemplate.update(sql, id, userId);
    }

    @Override
    @Transactional
    public void addDislike(long id, long userId) {
        String sql = "INSERT INTO review_dislikes (review_id, user_id) VALUES (?, ?)";

        jdbcTemplate.update(sql, id, userId);
    }

    @Override
    @Transactional
    public void removeLike(long id, long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? ";
        jdbcTemplate.update(sql, id, userId);
    }

    @Override
    @Transactional
    public void removeDislike(long id, long userId) {
        String sql = "DELETE FROM review_dislikes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }
}
