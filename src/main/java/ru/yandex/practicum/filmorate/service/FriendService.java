package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FriendDao;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {
    private final UserService userService; // Используем UserService вместо UserStorage
    private final FriendDao friendDao;

    public void addFriend(Long id, Long friendId) {
        validateUserIds(id, friendId);
        if (friendDao.isFriendshipExists(id, friendId)) {
            throw new ValidationException("Пользователь " + friendId + " уже в друзьях у " + id);
        }
        friendDao.addFriend(id, friendId);
        log.info("Пользователь {} добавил в друзья {}", id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        validateUserIds(id, friendId);
        friendDao.removeFriend(id, friendId);
        log.info("Пользователь {} удалил из друзей {}", id, friendId);
    }

    public List<User> getFriends(Long id) {
        userService.getById(id); // Проверка существования пользователя
        return friendDao.getFriends(id).stream()
                .map(userService::getById) // Получаем пользователя через сервис
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        validateUserIds(id, otherId);
        return friendDao.getCommonFriends(id, otherId).stream()
                .map(userService::getById)
                .collect(Collectors.toList());
    }

    private void validateUserIds(Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }
        // Проверяем существование пользователей через сервис
        userService.getById(id);
        userService.getById(friendId);
    }
}