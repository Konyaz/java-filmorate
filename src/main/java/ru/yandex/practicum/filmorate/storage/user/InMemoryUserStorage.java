package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    public User create(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public List<User> getAll() {
        return List.copyOf(users.values());
    }

    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
}