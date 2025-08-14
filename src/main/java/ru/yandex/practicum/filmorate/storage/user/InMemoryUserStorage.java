package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public User create(User user) {
        long id = idGenerator.incrementAndGet();
        user.setId(id);
        users.put(id, user);
        log.info("Created user with ID: {}", id);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
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
        User friend = users.get(friendId);
        if (user == null || friend == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(id);
        log.info("User {} and {} are now friends", id, friendId);
    }

    @Override
    public void removeFriend(Long id, Long friendId) {
        User user = users.get(id);
        User friend = users.get(friendId);
        if (user == null || friend == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
        log.info("User {} and {} are no longer friends", id, friendId);
    }

    @Override
    public List<User> getFriends(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        List<User> friends = new ArrayList<>();
        for (Long friendId : user.getFriends()) {
            Optional<User> friend = getById(friendId);
            friend.ifPresent(friends::add);
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        User user = users.get(id);
        User otherUser = users.get(otherId);
        if (user == null || otherUser == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        Set<Long> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(otherUser.getFriends());
        List<User> commonFriends = new ArrayList<>();
        for (Long fid : commonIds) {
            Optional<User> u = getById(fid);
            u.ifPresent(commonFriends::add);
        }
        return commonFriends;
    }
}
