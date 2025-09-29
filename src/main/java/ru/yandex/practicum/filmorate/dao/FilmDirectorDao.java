package ru.yandex.practicum.filmorate.dao;

import java.util.List;

public interface FilmDirectorDao {
    List<Long> getDirectorIdsByFilmId(Long filmId);

    List<Long> getFilmIdsByDirectorId(Long directorId);
}
