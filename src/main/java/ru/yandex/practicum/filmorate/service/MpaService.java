package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDao mpaDao;

    /**
     * Получение списка всех рейтингов MPA
     */
    public List<Mpa> getAllMpa() {
        log.info("Получение списка всех рейтингов MPA");
        return mpaDao.getAllMpa();
    }

    /**
     * Получение рейтинга MPA по ID
     * @throws NotFoundException если рейтинг не найден
     */
    public Mpa getMpaById(long id) {
        log.info("Получение рейтинга MPA с ID = {}", id);
        return mpaDao.getMpaById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID " + id + " не найден"));
    }
}