package ru.yandex.practicum.filmorate.dao;

import java.util.List;

public interface LikeDao {
    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Long> getLikes(Long filmId);

    List<Long> getUserLikedFilmsId(Long userId);

    List<Long> findSimilarUsers(Long userId);
}