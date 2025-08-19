package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
    private final GenreDao genreDao;

    @Override
    public List<Genre> getAllGenres() {
        log.info("Получение списка всех жанров");
        return genreDao.getAllGenres();
    }

    @Override
    public Genre getGenreById(Long id) {
        log.info("Получение жанра с ID = {}", id);
        return genreDao.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
    }
}