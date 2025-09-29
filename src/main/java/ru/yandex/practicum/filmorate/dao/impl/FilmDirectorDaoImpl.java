package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FilmDirectorDao;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDirectorDaoImpl implements FilmDirectorDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Long> getDirectorIdsByFilmId(Long filmId) {
        String sql = "SELECT director_id FROM film_directors WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, filmId);
    }

    @Override
    public List<Long> getFilmIdsByDirectorId(Long directorId) {
        String sql = "SELECT film_id FROM film_directors WHERE director_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, directorId);
    }
}
