package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Mpa {
    private Long id;
    private String name;

    // Конструктор без параметров
    public Mpa() {
    }

    // Конструктор с параметрами
    public Mpa(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
