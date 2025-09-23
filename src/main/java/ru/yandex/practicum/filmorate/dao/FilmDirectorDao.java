package ru.yandex.practicum.filmorate.dao;

import java.util.List;

public interface FilmDirectorDao {
    void addDirectorToFilm(Long filmId, Long directorId);

    void removeDirectorsFromFilm(Long filmId);

    List<Long> getDirectorIdsByFilmId(Long filmId);

    List<Long> getFilmIdsByDirectorId(Long directorId);
}
