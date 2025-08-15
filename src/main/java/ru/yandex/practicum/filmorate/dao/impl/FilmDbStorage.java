package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        final String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpaId());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);

        // Сохраняем жанры
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            final String genreSql = "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)";
            for (Long genreId : film.getGenreIds()) {
                jdbcTemplate.update(genreSql, id, genreId);
            }
        }

        log.info("Film created id={}", id);
        return film;
    }

    @Override
    public Film update(Film film) {
        final String sql = "UPDATE films " +
                "SET name=?, description=?, release_date=?, duration=?, mpa_id=? " +
                "WHERE id=?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpaId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        // Обновляем жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            final String genreSql = "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)";
            for (Long genreId : film.getGenreIds()) {
                jdbcTemplate.update(genreSql, film.getId(), genreId);
            }
        }

        log.info("Film updated id={}", film.getId());
        return film;
    }

    @Override
    public List<Film> getAll() {
        final String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "FROM films f ORDER BY f.id";
        return jdbcTemplate.query(sql, (rs, rn) -> {
            Film f = new Film();
            f.setId(rs.getLong("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            if (rs.getDate("release_date") != null) {
                f.setReleaseDate(rs.getDate("release_date").toLocalDate());
            }
            f.setDuration(rs.getInt("duration"));
            f.setMpaId(rs.getLong("mpa_id"));
            f.setGenreIds(loadGenreIds(f.getId()));
            return f;
        });
    }

    @Override
    public Optional<Film> getById(Long id) {
        final String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "FROM films f WHERE f.id = ?";
        try {
            Film f = jdbcTemplate.queryForObject(sql, (rs, rn) -> {
                Film film = new Film();
                film.setId(rs.getLong("id"));
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                if (rs.getDate("release_date") != null) {
                    film.setReleaseDate(rs.getDate("release_date").toLocalDate());
                }
                film.setDuration(rs.getInt("duration"));
                film.setMpaId(rs.getLong("mpa_id"));
                film.setGenreIds(loadGenreIds(id));
                return film;
            }, id);
            return Optional.ofNullable(f);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Set<Long> loadGenreIds(Long filmId) {
        final String sql = "SELECT genre_id FROM film_genres WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        throw new UnsupportedOperationException("Используйте LikeDaoImpl для операций с лайками");
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        throw new UnsupportedOperationException("Используйте LikeDaoImpl для операций с лайками");
    }
}