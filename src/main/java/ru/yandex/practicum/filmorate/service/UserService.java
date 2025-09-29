package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.LikeDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserDao;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;
    private final LikeDao likeDao;
    private final FilmDao filmDao;
    private final GenreDao genreDao;
    private final DirectorService directorService;

    public List<Film> getRecommendations(Long userId) {
        List<Long> similarUsers = likeDao.findSimilarUsers(userId);

        if (similarUsers.isEmpty()) {
            return List.of();
        }

        Long similarUserId = similarUsers.getFirst();
        List<Film> films = filmDao.getFilmsLikedByUserButNotAnother(similarUserId, userId);
        films.forEach(film -> {
            film.setGenres(genreDao.getGenresByFilmId(film.getId()));
            film.setDirectors(directorService.getDirectorsForFilm(film.getId()));
        });
        return films;
    }

    public void delete(Long userId) {
        if (userDao.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        userDao.delete(userId);
        log.info("User deleted id={}", userId);
    }

    public User create(@Valid User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Создание пользователя: {}", user);
        return userDao.create(user);
    }

    public User update(@Valid User user) {
        userDao.getById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + user.getId() + " не найден"));
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Обновление пользователя: {}", user);
        return userDao.update(user);
    }

    public List<User> getAll() {
        log.info("Получение всех пользователей");
        return userDao.getAll();
    }

    public User getById(Long id) {
        log.info("Получение пользователя с ID: {}", id);
        return userDao.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }
}