package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.FilmDirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDaoImpl implements FilmDao {

    private final JdbcTemplate jdbcTemplate;
    private final FilmDirectorDao filmDirectorDao;
    private final DirectorDao directorDao;

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

        saveFilmGenres(film);
        saveFilmDirectors(film);
        log.info("Film created id={}", film.getId());
        return film;
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

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveFilmGenres(film);
        filmDirectorDao.removeDirectorsFromFilm(film.getId());
        saveFilmDirectors(film);
        log.info("Film updated id={}", film.getId());
        return film;
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            final String genreSql = "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)";
            for (Long genreId : uniqueGenreIds) {
                jdbcTemplate.update(genreSql, film.getId(), genreId);
            }
        }
    }

    private void saveFilmDirectors(Film film) {
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                filmDirectorDao.addDirectorToFilm(film.getId(), director.getId());
            }
        }
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id ORDER BY f.id";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs));
        films.forEach(film -> {
            film.setGenres(getGenresForFilm(film.getId()));
            film.setDirectors(getDirectorsForFilm(film.getId()));
        });
        return films;
    }

    @Override
    public Optional<Film> getById(Long id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id=?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), id);

        if (films.isEmpty()) return Optional.empty();
        Film film = films.get(0);
        film.setGenres(getGenresForFilm(film.getId()));
        film.setDirectors(getDirectorsForFilm(film.getId()));
        return Optional.of(film);
    }

    @Override
    public List<Film> searchFilms(String query, Set<String> by) {
        if (query == null || query.isBlank() || by == null || by.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("Searching films with query: '{}' by fields: {}", query, by);

        // Создаем базовый SQL запрос
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name ")
                .append("FROM films f ")
                .append("LEFT JOIN mpa m ON f.mpa_id = m.id ");

        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        String searchPattern = "%" + query.toLowerCase() + "%";

        // Добавляем JOIN для директоров если нужно искать по режиссеру
        if (by.contains("director")) {
            sqlBuilder.append("LEFT JOIN film_directors fd ON f.id = fd.film_id ")
                    .append("LEFT JOIN directors d ON fd.director_id = d.id ");
        }

        sqlBuilder.append("WHERE ");

        // Добавляем условия поиска
        if (by.contains("title")) {
            conditions.add("LOWER(f.name) LIKE ?");
            params.add(searchPattern);
        }

        if (by.contains("director")) {
            conditions.add("LOWER(d.name) LIKE ?");
            params.add(searchPattern);
        }

        // Добавляем условие для поиска по описанию
        if (by.contains("description")) {
            conditions.add("LOWER(f.description) LIKE ?");
            params.add(searchPattern);
        }

        if (conditions.isEmpty()) {
            return Collections.emptyList();
        }

        sqlBuilder.append(String.join(" OR ", conditions));
        sqlBuilder.append(" ORDER BY f.id");

        String sql = sqlBuilder.toString();
        log.info("Executing search SQL: {} with params: {}", sql, params);

        try {
            List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), params.toArray());
            // Заполняем жанры и режиссеров для найденных фильмов
            films.forEach(film -> {
                film.setGenres(getGenresForFilm(film.getId()));
                film.setDirectors(getDirectorsForFilm(film.getId()));
            });
            log.info("Found {} films", films.size());
            return films;
        } catch (Exception e) {
            log.error("Error during search: {}", e.getMessage(), e);
            // Если произошла ошибка, попробуем упрощенный поиск только по названию
            if (by.contains("director") || by.contains("description")) {
                log.info("Trying fallback search without director and description");
                return searchFilmsFallback(query, by);
            }
            return Collections.emptyList();
        }
    }

    private List<Film> searchFilmsFallback(String query, Set<String> by) {
        String sql = "SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id " +
                "WHERE LOWER(f.name) LIKE ? ORDER BY f.id";

        String searchPattern = "%" + query.toLowerCase() + "%";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), searchPattern);
        films.forEach(film -> {
            film.setGenres(getGenresForFilm(film.getId()));
            film.setDirectors(getDirectorsForFilm(film.getId()));
        });
        return films;
    }

    @Override
    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name ")
                .append("FROM films f ")
                .append("LEFT JOIN mpa m ON f.mpa_id = m.id ")
                .append("LEFT JOIN likes l ON f.id = l.film_id ");

        List<Object> params = new ArrayList<>();

        // Добавляем условия фильтрации
        if (genreId != null || year != null) {
            sqlBuilder.append("WHERE ");
            List<String> conditions = new ArrayList<>();

            if (genreId != null) {
                sqlBuilder.append("EXISTS (SELECT 1 FROM film_genres fg WHERE fg.film_id = f.id AND fg.genre_id = ?) ");
                params.add(genreId);
                conditions.add(""); // Добавляем пустую строку для правильного соединения условий
            }

            if (year != null) {
                if (!conditions.isEmpty()) {
                    sqlBuilder.append("AND ");
                }
                sqlBuilder.append("YEAR(f.release_date) = ? ");
                params.add(year);
            }
        }

        sqlBuilder.append("GROUP BY f.id, m.name ")
                .append("ORDER BY COUNT(l.user_id) DESC ")
                .append("LIMIT ?");
        params.add(count);

        String sql = sqlBuilder.toString();
        log.info("Executing popular films SQL: {} with params: {}", sql, params);

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), params.toArray());
        films.forEach(film -> {
            film.setGenres(getGenresForFilm(film.getId()));
            film.setDirectors(getDirectorsForFilm(film.getId()));
        });
        return films;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private Film mapRowToFilm(java.sql.ResultSet rs) throws java.sql.SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        return film;
    }

    private List<Director> getDirectorsForFilm(Long filmId) {
        try {
            List<Long> directorIds = filmDirectorDao.getDirectorIdsByFilmId(filmId);
            return directorIds.stream()
                    .map(id -> directorDao.getById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error getting directors for film {}: {}", filmId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Genre> getGenresForFilm(Long filmId) {
        try {
            final String sql = "SELECT g.id, g.name " +
                    "FROM film_genres fg " +
                    "JOIN genres g ON fg.genre_id = g.id " +
                    "WHERE fg.film_id = ? " +
                    "ORDER BY g.id";

            return jdbcTemplate.query(sql, (rs, rn) -> {
                Genre genre = new Genre();
                genre.setId(rs.getLong("id"));
                genre.setName(rs.getString("name"));
                return genre;
            }, filmId);
        } catch (Exception e) {
            log.warn("Error getting genres for film {}: {}", filmId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public Boolean exists(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}