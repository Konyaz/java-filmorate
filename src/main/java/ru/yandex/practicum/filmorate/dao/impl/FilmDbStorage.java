package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDaoImpl mpaDao;
    private final GenreDaoImpl genreDao;

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

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Сохраняем порядок жанров и убираем дубликаты
            Set<Long> addedGenreIds = new HashSet<>();
            List<Genre> uniqueGenres = new ArrayList<>();

            for (Genre genre : film.getGenres()) {
                if (!addedGenreIds.contains(genre.getId())) {
                    uniqueGenres.add(genre);
                    addedGenreIds.add(genre.getId());
                }
            }

            film.setGenres(uniqueGenres);
            final String genreSql = "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : uniqueGenres) {
                jdbcTemplate.update(genreSql, id, genre.getId());
            }
        }

        film.setMpa(mpaDao.getMpaById(film.getMpa().getId()).orElse(null));
        film.setGenres(genreDao.getGenresByFilmId(film.getId()));
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

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Сохраняем порядок жанров и убираем дубликаты
            Set<Long> addedGenreIds = new HashSet<>();
            List<Genre> uniqueGenres = new ArrayList<>();

            for (Genre genre : film.getGenres()) {
                if (!addedGenreIds.contains(genre.getId())) {
                    uniqueGenres.add(genre);
                    addedGenreIds.add(genre.getId());
                }
            }

            film.setGenres(uniqueGenres);
            final String genreSql = "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : uniqueGenres) {
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
        }

        film.setMpa(mpaDao.getMpaById(film.getMpa().getId()).orElse(null));
        film.setGenres(genreDao.getGenresByFilmId(film.getId()));
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

            Long mpaId = rs.getLong("mpa_id");
            f.setMpa(mpaDao.getMpaById(mpaId).orElse(null));

            f.setGenres(genreDao.getGenresByFilmId(f.getId()));
            return f;
        });
    }

    @Override
    public Optional<Film> getById(Long id) {
        final String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "FROM films f WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, (rs, rn) -> {
                Film f = new Film();
                f.setId(rs.getLong("id"));
                f.setName(rs.getString("name"));
                f.setDescription(rs.getString("description"));
                if (rs.getDate("release_date") != null) {
                    f.setReleaseDate(rs.getDate("release_date").toLocalDate());
                }
                f.setDuration(rs.getInt("duration"));

                Long mpaId = rs.getLong("mpa_id");
                f.setMpa(mpaDao.getMpaById(mpaId).orElse(null));
                return f;
            }, id);

            if (film != null) {
                film.setGenres(genreDao.getGenresByFilmId(film.getId()));
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
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