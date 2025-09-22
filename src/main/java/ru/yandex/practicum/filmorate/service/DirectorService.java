package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDao directorStorage;

    public Director create(@Valid Director director) {
        log.info("Создание пользователя: {}", director);
        return directorStorage.create(director);
    }

    public Director update(@Valid Director director) {
        directorStorage.getById(director.getId())
                .orElseThrow(() -> new NotFoundException("Режиссер с ID " + director.getId() + " не найден"));
        log.info("Обновление пользователя: {}", director);
        return directorStorage.update(director);
    }

    public List<Director> getAll() {
        log.info("Получение всех режиссеров");
        return directorStorage.getAll();
    }

    public Director getById(Long id) {
        log.info("Получение режиссера с ID: {}", id);
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с ID " + id + " не найден"));
    }

    public void deleteById(Long id) {
        log.info("Удаление режиссера с ID: {}", id);
        if (!directorStorage.exists(id)) {
            throw new NotFoundException("Режиссер с ID " + id + " не найден");
        }
        directorStorage.deleteById(id);
    }
}