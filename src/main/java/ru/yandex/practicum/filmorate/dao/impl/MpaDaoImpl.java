package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
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

    // Метод для создания MPA в базе
    public Mpa create(Mpa mpa) {
        jdbcTemplate.update("INSERT INTO mpa (name) VALUES (?)", mpa.getName());
        // Получаем последний вставленный id
        Long id = jdbcTemplate.queryForObject("SELECT id FROM mpa WHERE name = ? ORDER BY id DESC LIMIT 1",
                Long.class, mpa.getName());
        mpa.setId(id);
        return mpa;
    }

    // Получить MPA по id
    public Optional<Mpa> getById(Long id) {
        List<Mpa> result = jdbcTemplate.query("SELECT * FROM mpa WHERE id = ?", mpaRowMapper, id);
        if (result.isEmpty()) return Optional.empty();
        return Optional.of(result.get(0));
    }

    // Получить всех MPA
    public List<Mpa> getAll() {
        return jdbcTemplate.query("SELECT * FROM mpa ORDER BY id", mpaRowMapper);
    }
}
