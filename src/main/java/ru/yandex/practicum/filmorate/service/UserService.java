package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
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
    @Qualifier("userDbStorage")
    private final UserDao userStorage;
    private final LikeDao likeDao;
    private final FilmDao filmDao;

    public List<Film> getRecommendations(Long userId) {
        List<Long> similarUsers = likeDao.findSimilarUsers(userId);

        if (similarUsers.isEmpty()) {
            return List.of();
        }

        Long similarUserId = similarUsers.get(0);
        return filmDao.getFilmsLikedByUserButNotAnother(similarUserId, userId);
    }

    public void delete(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        userStorage.delete(userId);
        log.info("User deleted id={}", userId);
    }


    public User create(@Valid User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Создание пользователя: {}", user);
        return userStorage.create(user);
    }

    public User update(@Valid User user) {
        userStorage.getById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + user.getId() + " не найден"));
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Обновление пользователя: {}", user);
        return userStorage.update(user);
    }

    public List<User> getAll() {
        log.info("Получение всех пользователей");
        return userStorage.getAll();
    }

    public User getById(Long id) {
        log.info("Получение пользователя с ID: {}", id);
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }
}