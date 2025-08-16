package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FriendService friendService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("POST /users");
        try {
            User createdUser = userService.create(user);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("Ошибка в методе createUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        log.info("PUT /users");
        try {
            User updatedUser = userService.update(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Ошибка в методе updateUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        log.info("GET /users");
        try {
            List<User> users = userService.getAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Ошибка в методе getUsers: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("GET /users/{}", id);
        try {
            User user = userService.getById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Ошибка в методе getUserById: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("PUT /users/{}/friends/{}", id, friendId);
        try {
            friendService.addFriend(id, friendId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка в методе addFriend: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("DELETE /users/{}/friends/{}", id, friendId);
        try {
            friendService.removeFriend(id, friendId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Ошибка в методе removeFriend: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable Long id) {
        log.info("GET /users/{}/friends", id);
        try {
            List<User> friends = friendService.getFriends(id);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            log.error("Ошибка в методе getFriends: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(
            @PathVariable Long id,
            @PathVariable Long otherId) {
        log.info("GET /users/{}/friends/common/{}", id, otherId);
        try {
            List<User> commonFriends = friendService.getCommonFriends(id, otherId);
            return ResponseEntity.ok(commonFriends);
        } catch (Exception e) {
            log.error("Ошибка в методе getCommonFriends: {}", e.getMessage(), e);
            throw e;
        }
    }
}