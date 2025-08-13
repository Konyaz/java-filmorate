package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MpaDaoImpl implements MpaDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa getMpaById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new MpaRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new EmptyResultDataAccessException("MPA рейтинг с ID " + id + " не найден", 1);
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM mpa_ratings";
        return jdbcTemplate.query(sql, new MpaRowMapper());
    }

    private class MpaRowMapper implements RowMapper<Mpa> {
        @Override
        public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        }
    }
}