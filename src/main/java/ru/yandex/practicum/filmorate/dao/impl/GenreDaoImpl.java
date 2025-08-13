package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreDaoImpl implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    @Override
    public Genre getGenreById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToGenre, id);
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }
}