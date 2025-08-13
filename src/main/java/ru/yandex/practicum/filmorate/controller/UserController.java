package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        User created = userService.create(user);
        log.info("Пользователь создан: {}", created);
        return created;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);
        User updated = userService.update(user);
        log.info("Пользователь обновлен: {}", updated);
        return updated;
    }

    @GetMapping
    public List<User> getAll() {
        log.info("Получен запрос на получение всех пользователей");
        List<User> users = userService.getAll();
        log.info("Возвращено {} пользователей", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        log.info("Получен запрос на получение пользователя с ID: {}", id);
        User user = userService.getById(id);
        log.info("Найден пользователь: {}", user);
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос на добавление в друзья: пользователь ID={}, друг ID={}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Пользователи добавлены в друзья: ID={} и ID={}", id, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос на удаление из друзей: пользователь ID={}, друг ID={}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Пользователи удалены из друзей: ID={} и ID={}", id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("Получен запрос на получение друзей пользователя ID={}", id);
        List<User> friends = userService.getFriends(id);
        log.info("Возвращено {} друзей пользователя ID={}", friends.size(), id);
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получен запрос на получение общих друзей пользователей ID={} и ID={}", id, otherId);
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.info("Возвращено {} общих друзей пользователей ID={} и ID={}", commonFriends.size(), id, otherId);
        return commonFriends;
    }
}