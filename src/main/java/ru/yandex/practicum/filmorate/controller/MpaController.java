package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaDao mpaDao;

    @Autowired
    public MpaController(MpaDao mpaDao) {
        this.mpaDao = mpaDao;
    }

    @GetMapping
    public List<Mpa> getAllMpa() {
        return mpaDao.getAllMpa();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        try {
            return mpaDao.getMpaById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA рейтинг с ID " + id + " не найден");
        }
    }
}