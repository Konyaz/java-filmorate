package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private Long id;

    // Аннотация @NotBlank проверяет, что поле не пустое и не состоит из пробелов.
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна быть валидной")
    private String email;

    // Аннотация @NotBlank обеспечивает, что логин не будет null, пустым или состоять только из пробелов.
    @NotBlank(message = "Логин не может быть пустым и не должен содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения обязательна")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Long> friends = new HashSet<>();

    /**
     * Возвращает имя пользователя. Если имя не указано (null или пустое), используется логин.
     *
     * @return имя пользователя или логин
     */
    public String getName() {
        return (name == null || name.isBlank()) ? login : name;
    }

    public void addFriend(Long friendId) {
        friends.add(friendId);
    }

    public void removeFriend(Long friendId) {
        friends.remove(friendId);
    }
}
