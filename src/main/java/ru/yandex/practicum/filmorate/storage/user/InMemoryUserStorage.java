package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public User create(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.incrementAndGet());
        }
        users.put(user.getId(), user);
        log.info("Created user with ID: {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new org.springframework.dao.EmptyResultDataAccessException("User with ID " + user.getId() + " not found", 1);
        }
        users.put(user.getId(), user);
        log.info("Updated user with ID: {}", user.getId());
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
    public void addFriend(Long id, Long friendId) {
        User user = users.get(id);
        if (user == null) {
            throw new org.springframework.dao.EmptyResultDataAccessException("User with ID " + id + " not found", 1);
        }
        User friend = users.get(friendId);
        if (friend == null) {
            throw new org.springframework.dao.EmptyResultDataAccessException("Friend with ID " + friendId + " not found", 1);
        }
        user.addFriend(friendId); // Use the method from User class
        log.info("Added friend with ID {} to user with ID {}", friendId, id);
    }

    @Override
    public void removeFriend(Long id, Long friendId) {
        User user = users.get(id);
        if (user == null) {
            throw new org.springframework.dao.EmptyResultDataAccessException("User with ID " + id + " not found", 1);
        }
        user.removeFriend(friendId); // Use the method from User class
        log.info("Removed friend with ID {} from user with ID {}", friendId, id);
    }

    @Override
    public List<User> getFriends(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new org.springframework.dao.EmptyResultDataAccessException("User with ID " + id + " not found", 1);
        }
        return user.getFriends().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        User user = users.get(id);
        User otherUser = users.get(otherId);
        if (user == null || otherUser == null) {
            throw new org.springframework.dao.EmptyResultDataAccessException("User not found", 1);
        }
        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());
        return commonFriendIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}