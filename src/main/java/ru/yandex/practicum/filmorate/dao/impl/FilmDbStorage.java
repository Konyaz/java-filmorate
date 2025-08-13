package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Primary
@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDao genreDao;
    private final MpaDao mpaDao;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreDao genreDao, MpaDao mpaDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDao = genreDao;
        this.mpaDao = mpaDao;
    }

    @Override
    public Film create(Film film) {
        // Проверяем существование MPA
        try {
            mpaDao.getMpaById(film.getMpa().getId());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA рейтинг с ID " + film.getMpa().getId() + " не найден");
        }

        // Проверяем существование жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                try {
                    genreDao.getGenreById(genre.getId());
                } catch (EmptyResultDataAccessException e) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            }
        }

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        // Сохраняем жанры (без дубликатов)
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = new HashSet<>(film.getGenres());
            for (Genre genre : uniqueGenres) {
                jdbcTemplate.update(
                        "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId()
                );
            }
            film.setGenres(new ArrayList<>(uniqueGenres));
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        // Проверяем существование фильма
        String checkSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, film.getId());
        if (count == null || count == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        // Проверяем существование MPA
        try {
            mpaDao.getMpaById(film.getMpa().getId());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA рейтинг с ID " + film.getMpa().getId() + " не найден");
        }

        // Проверяем существование жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                try {
                    genreDao.getGenreById(genre.getId());
                } catch (EmptyResultDataAccessException e) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            }
        }

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE id = ?";

        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        // Обновляем жанры (без дубликатов)
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = new HashSet<>(film.getGenres());
            for (Genre genre : uniqueGenres) {
                jdbcTemplate.update(
                        "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId()
                );
            }
            film.setGenres(new ArrayList<>(uniqueGenres));
        }

        return film;
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id";
        return jdbcTemplate.query(sql, new FilmRowMapper());
    }

    @Override
    public Optional<Film> getById(Long id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), id);
        return films.isEmpty() ? Optional.empty() : Optional.of(films.get(0));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                filmId, userId
        );
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM likes WHERE film_id = ? AND user_id = ?",
                filmId, userId
        );
    }

    private class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            // MPA
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            // Жанры (без дубликатов)
            List<Genre> genres = jdbcTemplate.query(
                    "SELECT DISTINCT g.* FROM film_genre fg JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id = ?",
                    (rs1, rowNum1) -> {
                        Genre genre = new Genre();
                        genre.setId(rs1.getInt("id"));
                        genre.setName(rs1.getString("name"));
                        return genre;
                    },
                    film.getId()
            );
            film.setGenres(genres);

            // Лайки
            Set<Long> likes = new HashSet<>(jdbcTemplate.queryForList(
                    "SELECT user_id FROM likes WHERE film_id = ?",
                    Long.class,
                    film.getId()
            ));
            film.setLikes(likes);

            return film;
        }
    }
}