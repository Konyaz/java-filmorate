package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
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
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        log.info("POST /users -> {}", user);
        User createdUser = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("PUT /users -> {}", user);
        return userService.update(user);
    }

    @GetMapping
    public List<User> getAll() {
        log.info("GET /users");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        log.info("GET /users/{}", id);
        return userService.getById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("PUT /users/{}/friends/{}", id, friendId);
        friendService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("DELETE /users/{}/friends/{}", id, friendId);
        friendService.removeFriend(id, friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("GET /users/{}/friends", id);
        return friendService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("GET /users/{}/friends/common/{}", id, otherId);
        return friendService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long id) {
        return userService.getRecommendations(id);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            userService.delete(userId);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}