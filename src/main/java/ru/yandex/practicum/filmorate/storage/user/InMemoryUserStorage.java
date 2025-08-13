package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public User create(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null && friend != null) {
            user.addFriend(friendId);
            friend.addFriend(userId); // взаимная дружба
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null && friend != null) {
            user.removeFriend(friendId);
            friend.removeFriend(userId); // удаление взаимной дружбы
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        return user.getFriends().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        User user1 = users.get(userId);
        User user2 = users.get(otherUserId);

        if (user1 == null || user2 == null) {
            return Collections.emptyList();
        }

        Set<Long> commonFriendIds = new HashSet<>(user1.getFriends());
        commonFriendIds.retainAll(user2.getFriends());

        return commonFriendIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}