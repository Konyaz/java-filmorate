package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.dao.impl.FilmDaoImpl;
import ru.yandex.practicum.filmorate.dao.impl.ReviewDaoImpl;
import ru.yandex.practicum.filmorate.dao.impl.UserDaoImpl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

import static ru.yandex.practicum.filmorate.util.ActionsId.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    public final ReviewDaoImpl storage;
    public final FilmDaoImpl filmStorage;
    public final UserDaoImpl userStorage;
    private final EventDao eventDao;

    public Collection<Review> getListByFilmId(Long filmId, Integer count) {
        if (count <= 0) {
            log.error("Параметр count должен быть больше 0");
            throw new ValidationException("Параметр count должен быть больше 0");
        }
        if (!filmStorage.exists(filmId)) {
            log.warn("Фильм с id = {} не найден. Возвращен пустой список", filmId);
            return Collections.emptyList();
        }
        return storage.filteredList(filmId, count);
    }

    public Collection<Review> getList(Integer count) {
        if (count <= 0) {
            log.error("Параметр count должен быть больше 0");
            throw new ValidationException("Параметр count должен быть больше 0");
        }
        return storage.list(count);
    }

    public Review get(long id) {
        if (!storage.exists(id)) {
            log.error("Отзыв с id = {} не найден", id);
            throw new NotFoundException(String.format("Отзыв с id = %d не найден", id));
        }
        return storage.get(id);
    }

    public Review add(@Valid Review newReview) {
        log.info("POST /reviews -> {}", newReview);

        if (!filmStorage.exists(newReview.getFilmId())) {
            log.error("Ошибка добавления отзыва: фильм с указанным filmId не существует");
            throw new NotFoundException("фильм с указанным filmId не существует");
        }
        if (!userStorage.exists(newReview.getUserId())) {
            log.error("Ошибка добавления отзыва: пользователя с указанным userId не существует");
            throw new NotFoundException("пользователя с указанным userId не существует");
        }

        Review created = storage.create(newReview);

        log.info("Пользователь {} создал отзыв {}", created.getUserId(), created.getReviewId());
        eventDao.saveEvent(
                new EventDto(created.getUserId(), created.getReviewId(), REVIEW.getId(), ADD.getId(), Instant.now())
        );

        return created;
    }

    public Review update(@Valid Review review) {
        log.info("PUT /reviews/id -> {}", review);

        if (!storage.exists(review.getReviewId())) {
            log.error("Ошибка обновления отзыва: отзыва с указанным id не существует");
            throw new NotFoundException("отзыва с указанным id не существует");
        }
        if (!filmStorage.exists(review.getFilmId())) {
            log.error("Ошибка обновления отзыва: фильм с указанным filmId не существует");
            throw new NotFoundException("фильм с указанным filmId не существует");
        }
        if (!userStorage.exists(review.getUserId())) {
            log.error("Ошибка обновления отзыва: пользователя с указанным userId не существует");
            throw new NotFoundException("пользователя с указанным userId не существует");
        }

        Review updated = storage.update(review);

        log.info("Пользователь {} обновил отзыв {}", updated.getUserId(), updated.getReviewId());
        eventDao.saveEvent(
                new EventDto(updated.getUserId(), updated.getReviewId(), REVIEW.getId(), UPDATE.getId(), Instant.now())
        );

        return updated;
    }

    public void delete(long id) {
        log.info("DELETE /reviews/id -> {}", id);

        if (!storage.exists(id)) {
            log.error("Отзыв с указанным id не существует");
            throw new NotFoundException("Отзыв с указанным id не существует");
        }

        Review deleted = get(id);
        storage.delete(id);

        log.info("Пользователь {} удалил отзыв {}", deleted.getUserId(), id);
        eventDao.saveEvent(
                new EventDto(deleted.getUserId(), id, REVIEW.getId(), REMOVE.getId(), Instant.now())
        );
    }

    public void addLike(long id, long userId) {
        if (!storage.exists(id)) {
            log.error("Отзыв с указанным id не существует");
            throw new NotFoundException("Отзыв с указанным id не существует");
        }
        if (!userStorage.exists(userId)) {
            log.error("Пользователь с указанным id не существует");
            throw new NotFoundException("Пользователь с указанным id не существует");
        }
        if (storage.likeExists(id, userId)) {
            log.error("Пользователь уже поставил лайк данному отзыву");
            throw new ValidationException("Пользователь уже поставил лайк данному отзыву");
        }

//        Проверяем дополнительно наличие дизлайка и в случае чего удаляем его
        if (storage.dislikeExists(id, userId)) {
            storage.removeDislike(id, userId);
        }

        storage.addLike(id, userId);
    }

    public void addDislike(long id, long userId) {
        if (!storage.exists(id)) {
            log.error("Отзыв с указанным id не существует");
            throw new NotFoundException("Отзыв с указанным id не существует");
        }
        if (!userStorage.exists(userId)) {
            log.error("Пользователь с указанным id не существует");
            throw new NotFoundException("Пользователь с указанным id не существует");
        }
        if (storage.dislikeExists(id, userId)) {
            log.error("Пользователь уже поставил дизлайк данному отзыву");
            throw new ValidationException("Пользователь уже поставил дизлайк данному отзыву");
        }

//        Проверяем дополнительно наличие лайка и в случае чего удаляем его
        if (storage.likeExists(id, userId)) {
            storage.removeLike(id, userId);
        }

        storage.addDislike(id, userId);
    }

    public void removeLike(long id, long userId) {
        if (!storage.exists(id)) {
            log.error("Отзыв с указанным id не существует");
            throw new NotFoundException("Отзыв с указанным id не существует");
        }
        if (!userStorage.exists(userId)) {
            log.error("Пользователь с указанным id не существует");
            throw new NotFoundException("Пользователь с указанным id не существует");
        }
        if (!storage.likeExists(id, userId)) {
            log.error("Пользователь с указанным id не ставил лайк этому отзыву");
            throw new NotFoundException("Пользователь с указанным id не ставил лайк этому отзыву");
        }

        storage.removeLike(id, userId);
    }

    public void removeDislike(long id, long userId) {
        if (!storage.exists(id)) {
            log.error("Отзыв с указанным id не существует");
            throw new NotFoundException("Отзыв с указанным id не существует");
        }
        if (!userStorage.exists(userId)) {
            log.error("Пользователь с указанным id не существует");
            throw new NotFoundException("Пользователь с указанным id не существует");
        }
        if (!storage.dislikeExists(id, userId)) {
            log.error("Пользователь с указанным id не ставил дизлайк этому отзыву");
            throw new NotFoundException("Пользователь с указанным id не ставил дизлайк этому отзыву");
        }

        storage.removeDislike(id, userId);
    }

}
