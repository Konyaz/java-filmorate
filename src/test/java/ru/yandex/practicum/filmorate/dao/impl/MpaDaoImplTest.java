package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class MpaDaoImplTest {
    @Autowired
    private MpaDaoImpl mpaDao;

    @Test
    void testGetAllMpa() {
        List<Mpa> mpaList = mpaDao.getAllMpa();
        assertFalse(mpaList.isEmpty());
        assertEquals(5, mpaList.size());
        assertEquals("G", mpaList.get(0).getName());
    }

    @Test
    void testGetMpaById() {
        Mpa mpa = mpaDao.getMpaById(1);
        assertNotNull(mpa);
        assertEquals("G", mpa.getName());
    }

    @Test
    void testGetNonExistentMpa() {
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class,
                () -> mpaDao.getMpaById(999));
    }
}