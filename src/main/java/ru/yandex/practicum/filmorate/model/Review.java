package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    Long reviewId;

    @NotBlank(message = "Контент не может быть пустым")
    String content;

    @NotNull(message = "Поле isPositive обязательно к заполнению")
    Boolean isPositive;

    @NotNull(message = "Поле userId обязательно к заполнению")
    Long userId;

    @NotNull(message = "Поле filmId обязательно к заполнению")
    Long filmId;

    Integer useful;
}
