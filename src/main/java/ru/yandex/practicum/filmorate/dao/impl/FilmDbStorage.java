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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);

        // Сохраняем жанры
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            final String genreSql = "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(genreSql, id, genre.getId());
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
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        // Обновляем жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            final String genreSql = "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
        }

        log.info("Film updated id={}", film.getId());
        return film;
    }

    @Override
    public List<Film> getAll() {
        final String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id ORDER BY f.id";
        return jdbcTemplate.query(sql, (rs, rn) -> {
            Film f = new Film();
            f.setId(rs.getLong("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            if (rs.getDate("release_date") != null) {
                f.setReleaseDate(rs.getDate("release_date").toLocalDate());
            }
            f.setDuration(rs.getInt("duration"));
            Mpa m = new Mpa();
            m.setId(rs.getLong("mpa_id"));
            m.setName(rs.getString("mpa_name"));
            f.setMpa(m);
            f.setGenres(loadGenres(f.getId()));
            return f;
        });
    }

    @Override
    public Optional<Film> getById(Long id) {
        final String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
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
                Mpa m = new Mpa();
                m.setId(rs.getLong("mpa_id"));
                m.setName(rs.getString("mpa_name"));
                film.setMpa(m);
                film.setGenres(loadGenres(id));
                return film;
            }, id);
            return Optional.ofNullable(f);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Set<Genre> loadGenres(Long filmId) {
        final String sql = "SELECT g.id, g.name FROM film_genres fg JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id = ?";
        return jdbcTemplate.query(sql, (rs, rn) -> {
            Genre g = new Genre();
            g.setId(rs.getLong("id"));
            g.setName(rs.getString("name"));
            return g;
        }, filmId).stream().collect(Collectors.toSet());
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