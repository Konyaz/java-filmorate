package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Min(value = 1, message = "Продолжительность должна быть положительной")
    private Integer duration;

    private Mpa mpa;

    private List<Genre> genres = new ArrayList<>();

    private List<Director> directors = new ArrayList<>();

    private Integer rate = 0;

    public void addGenre(Genre genre) {
        if (!genres.contains(genre)) {
            genres.add(genre);
        }
    }

    public void addDirector(Director director) {
        if (!directors.contains(director)) {
            directors.add(director);
        }
    }
}
