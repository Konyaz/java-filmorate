package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewDao {
    Collection<Review> list(int count);

    Collection<Review> filteredList(long filmId, int count);

    Review get(long id);

    Review create(Review obj);

    Review update(Review obj);

    void delete(long id);

    boolean exists(long id);

    void addLike(long id, long userId);

    void addDislike(long id, long userId);

    void removeLike(long id, long userId);

    void removeDislike(long id, long userId);
}
