package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MpaDaoImpl {

    private final JdbcTemplate jdbcTemplate;

    public MpaDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) -> new Mpa(
            rs.getLong("id"),
            rs.getString("name")
    );

    public Mpa create(Mpa mpa) {
        final String sql = "INSERT INTO mpa (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, mpa.getName());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        mpa.setId(id);
        return mpa;
    }

    public Optional<Mpa> getById(Long id) {
        List<Mpa> result = jdbcTemplate.query("SELECT * FROM mpa WHERE id = ?", mpaRowMapper, id);
        if (result.isEmpty()) return Optional.empty();
        return Optional.of(result.get(0));
    }

    public List<Mpa> getAll() {
        return jdbcTemplate.query("SELECT * FROM mpa ORDER BY id", mpaRowMapper);
    }
}