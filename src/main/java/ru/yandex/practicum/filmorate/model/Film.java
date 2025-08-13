package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Длительность должна быть положительной")
    private int duration;

    private Mpa mpa;
    private List<Genre> genres;
    private Set<Long> likes = new HashSet<>();

    @AssertTrue(message = "Дата релиза должна быть не ранее 28 декабря 1895 года")
    private boolean isValidReleaseDate() {
        return releaseDate != null && !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}