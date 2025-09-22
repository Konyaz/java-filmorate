package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository("directorDbStorage")
@RequiredArgsConstructor
public class DirectorDaoImpl implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director create(Director director) {
        final String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        director.setId(id);
        log.info("Director created id={}", id);
        return director;
    }

    @Override
    public Director update(Director director) {
        final String sql = "UPDATE directors SET name=? WHERE id=?";
        int updated = jdbcTemplate.update(sql,
                director.getName(),
                director.getId());

        if (updated == 0) {
            throw new NotFoundException("Director not found: id=" + director.getId());
        }
        log.info("Director updated id={}", director.getId());
        return director;
    }

    @Override
    public List<Director> getAll() {
        final String sql = "SELECT id, name FROM directors ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rn) -> {
            Director u = new Director();
            u.setId(rs.getLong("id"));
            u.setName(rs.getString("name"));
            return u;
        });
    }

    @Override
    public Optional<Director> getById(Long id) {
        final String sql = "SELECT id, name FROM directors WHERE id = ?";
        try {
            Director u = jdbcTemplate.queryForObject(sql, (rs, rn) -> {
                Director director = new Director();
                director.setId(rs.getLong("id"));
                director.setName(rs.getString("name"));
                return director;
            }, id);
            return Optional.ofNullable(u);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long id) {
        final String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean exists(Long id) {
        final String sql = "SELECT COUNT(*) FROM directors WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}