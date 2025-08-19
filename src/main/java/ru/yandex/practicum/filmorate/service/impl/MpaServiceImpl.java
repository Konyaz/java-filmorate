package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaServiceImpl implements MpaService {
    private final MpaDao mpaDao;

    @Override
    public List<Mpa> getAllMpa() {
        log.info("Получение списка всех рейтингов MPA");
        return mpaDao.getAllMpa();
    }

    @Override
    public Mpa getMpaById(Long id) {
        log.info("Получение рейтинга MPA с ID = {}", id);
        return mpaDao.getMpaById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID " + id + " не найден"));
    }
}