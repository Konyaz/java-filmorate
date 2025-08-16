package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDao genreDao;

    /**
     * Получение списка всех жанров
     */
    public List<Genre> getAllGenres() {
        log.info("Получение списка всех жанров");
        return genreDao.getAllGenres();
    }

    /**
     * Получение жанра по ID
     * @throws NotFoundException если жанр не найден
     */
    public Genre getGenreById(long id) {
        log.info("Получение жанра с ID = {}", id);
        return genreDao.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
    }
}