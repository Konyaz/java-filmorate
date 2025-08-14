package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDaoImpl implements GenreDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Genre> getGenreById(Long id) {
        final String sql = "SELECT id, name FROM genres WHERE id = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Genre g = new Genre();
                g.setId(rs.getLong("id"));
                g.setName(rs.getString("name"));
                return g;
            }, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        final String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre g = new Genre();
            g.setId(rs.getLong("id"));
            g.setName(rs.getString("name"));
            return g;
        });
    }
}
