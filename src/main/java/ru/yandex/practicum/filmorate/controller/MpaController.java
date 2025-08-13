package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaDao mpaDao;

    @GetMapping
    public List<Mpa> getAll() {
        return mpaDao.getAll();
    }

    @GetMapping("/{id}")
    public Mpa getById(@PathVariable Long id) {
        Optional<Mpa> mpa = mpaDao.getById(id);
        if (mpa.isEmpty()) {
            throw new NotFoundException("MPA рейтинг с ID " + id + " не найден");
        }
        return mpa.get();
    }
}