package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FriendDao;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {
    private final UserStorage userStorage;
    private final FriendDao friendDao;

    public void addFriend(Long id, Long friendId) {
        validateUserIds(id, friendId);

        if (friendDao.isFriendshipExists(id, friendId)) {
            throw new ValidationException("Пользователь " + friendId + " уже в друзьях у " + id);
        }

        // Добавляем двустороннюю дружбу
        friendDao.addFriend(id, friendId);
        friendDao.addFriend(friendId, id);

        log.info("Пользователь {} добавил в друзья {}", id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        validateUserIds(id, friendId);

        // Удаляем двустороннюю дружбу
        friendDao.removeFriend(id, friendId);
        friendDao.removeFriend(friendId, id);

        log.info("Пользователь {} удалил из друзей {}", id, friendId);
    }

    public List<User> getFriends(Long id) {
        getUserOrThrow(id);
        return friendDao.getFriends(id).stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        validateUserIds(id, otherId);
        return friendDao.getCommonFriends(id, otherId).stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    private void validateUserIds(Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }
        getUserOrThrow(id);
        getUserOrThrow(friendId);
    }

    private User getUserOrThrow(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }
}