package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Director {
    private Long id;       // уникальный ID режиссёра
    private String name;   // имя режиссёра
}
