package ru.yandex.practicum.filmorate.util;

import lombok.Getter;

@Getter
public enum ActionsId {

    ADD(1L), REMOVE(2L), UPDATE(3L), LIKE(1L), REVIEW(2L), FRIEND(3L), DISLIKE(4L);

    private final Long id;

    ActionsId(Long id) {
        this.id = id;
    }
}