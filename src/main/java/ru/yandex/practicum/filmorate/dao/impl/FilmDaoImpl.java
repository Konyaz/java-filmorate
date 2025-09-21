package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
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
        updateFilmGenres(film);

        saveFilmGenres(film);
        saveFilmDirectors(film);
        log.info("Film created id={}", id);
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

        // Если ни одно условие не добавилось, возвращаем пустой список
        if (first) {
            return Collections.emptyList();
        }

        where.append(" ORDER BY f.id");
        String sql = sqlBase + where;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), params.toArray());
        } catch (Exception e) {
            // Если есть проблемы с таблицей директоров, возвращаем поиск только по названию и описанию
            if (e.getMessage().contains("directors") || e.getMessage().contains("film_directors")) {
                // Перестраиваем запрос без директоров
                where = new StringBuilder("WHERE ");
                params.clear();
                first = true;

                if (by.contains("title")) {
                    where.append("LOWER(f.name) LIKE ?");
                    params.add("%" + query.toLowerCase() + "%");
                    first = false;
                }

                if (by.contains("description")) {
                    if (!first) where.append(" OR ");
                    where.append("LOWER(f.description) LIKE ?");
                    params.add("%" + query.toLowerCase() + "%");
                }

                where.append(" ORDER BY f.id");
                sql = sqlBase + where;

                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), params.toArray());
            }
            throw e;
        }
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

        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        if (loadGenres) {
            List<Genre> genres = getGenresForFilm(film.getId());
            film.setGenres(genres);
            List<Director> directors = getDirectorsForFilm(film.getId());
            film.setDirectors(directors);
        }

        return film;
    }

    private List<Director> getDirectorsForFilm(Long filmId) {
        List<Long> directorIds = filmDirectorDao.getDirectorIdsByFilmId(filmId);
        return directorIds.stream()
                .map(id -> directorDao.getById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Genre> getGenresForFilm(Long filmId) {
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
    }

    public Boolean exists(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != 0;
    }
}