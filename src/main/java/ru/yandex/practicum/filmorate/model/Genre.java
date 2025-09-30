package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    private Long id;
    private String name;

    // Добавлен конструктор для вызовов new Genre(id, name)
    public Genre(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
