package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @PostMapping
    public ResponseEntity<Director> create(@Valid @RequestBody Director director) {
        log.info("POST /directors -> {}", director);
        Director createdUser = directorService.create(director);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        log.info("PUT /directors -> {}", director);
        return directorService.update(director);
    }

    @GetMapping
    public List<Director> getAll() {
        log.info("GET /directors");
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable Long id) {
        log.info("GET /directors/{}", id);
        return directorService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        log.info("DELETE /directors/{}", id);
        directorService.deleteById(id);
    }

}