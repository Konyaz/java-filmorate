package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDaoImpl implements FilmDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        updateFilmGenres(film);

        return getById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + film.getId() + " не найден после создания"));
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE id=?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        // Обновляем жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id=?", film.getId());
        updateFilmGenres(film);

        return getById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + film.getId() + " не найден после обновления"));
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id ORDER BY f.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs));
    }

    @Override
    public Optional<Film> getById(Long id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id=?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), id);

        if (films.isEmpty()) return Optional.empty();
        return Optional.of(films.get(0));
    }

    @Override
    public List<Film> searchFilms(String query, Set<String> by) {
        if (query == null || query.isBlank() || by == null || by.isEmpty()) {
            return Collections.emptyList();
        }

        String sqlBase = "SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id ";
        StringBuilder where = new StringBuilder("WHERE ");
        List<Object> params = new ArrayList<>();
        boolean first = true;

        if (by.contains("title")) {
            where.append("LOWER(f.name) LIKE ?");
            params.add("%" + query.toLowerCase() + "%");
            first = false;
        }

        if (by.contains("description")) {
            if (!first) where.append(" OR ");
            where.append("LOWER(f.description) LIKE ?");
            params.add("%" + query.toLowerCase() + "%");
            first = false;
        }

        if (by.contains("director")) {
            if (!first) where.append(" OR ");
            where.append("EXISTS (SELECT 1 FROM film_directors fd JOIN directors d ON fd.director_id=d.id " +
                    "WHERE fd.film_id=f.id AND LOWER(d.name) LIKE ?)");
            params.add("%" + query.toLowerCase() + "%");
        }

        where.append(" ORDER BY f.id");
        String sql = sqlBase + where;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), params.toArray());
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id=m.id " +
                "LEFT JOIN likes l ON f.id=l.film_id " +
                "GROUP BY f.id, m.name " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), count);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private List<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.id, g.name FROM film_genres fg JOIN genres g ON fg.genre_id=g.id WHERE fg.film_id=?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")), filmId);
    }

    private void updateFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        String sql = "MERGE INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private Film mapRowToFilm(java.sql.ResultSet rs) throws java.sql.SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getLong("mpa_id"), rs.getString("mpa_name")));
        film.setGenres(getGenresByFilmId(film.getId()));
        return film;
    }
}
