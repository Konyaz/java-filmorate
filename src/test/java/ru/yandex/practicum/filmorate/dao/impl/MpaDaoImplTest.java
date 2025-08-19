package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(MpaDaoImpl.class)
class MpaDaoImplTest {
    @Autowired
    private MpaDao mpaDao;

    @Test
    void testGetAllMpa() {
        List<Mpa> mpaList = mpaDao.getAllMpa();
        assertEquals(5, mpaList.size());
        assertEquals("G", mpaList.get(0).getName());
    }

    @Test
    void testGetMpaById() {
        Optional<Mpa> mpa = mpaDao.getMpaById(1L);
        assertTrue(mpa.isPresent());
        assertEquals("G", mpa.get().getName());
    }

    @Test
    void testGetMpaByInvalidId() {
        Optional<Mpa> mpa = mpaDao.getMpaById(999L);
        assertFalse(mpa.isPresent());
    }
}