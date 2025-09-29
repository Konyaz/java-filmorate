package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class Director {
    private Long id;

    @NotBlank(message = "Имя режиссера не может быть пустым")
    @Size(max = 255, message = "Имя режиссера не может превышать 255 символов")
    private String name;
}