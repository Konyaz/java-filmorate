package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Sql(scripts = "classpath:schema.sql")
class MpaDaoImplTest {
    @Autowired
    private MpaDaoImpl mpaDao;

    @Test
    void testGetAllMpa() {
        List<Mpa> mpaList = mpaDao.getAll();
        assertNotNull(mpaList);
        assertTrue(mpaList.isEmpty()); // Пустой список, так как data.sql не загружается
    }

    @Test
    void testGetMpaById() {
        Mpa mpa = new Mpa();
        mpa.setName("G");
        mpa = mpaDao.create(mpa);

        Optional<Mpa> found = mpaDao.getById(mpa.getId());
        assertTrue(found.isPresent());
        assertEquals("G", found.get().getName());
    }

    @Test
    void testGetNonExistentMpa() {
        Optional<Mpa> found = mpaDao.getById(999L);
        assertTrue(found.isEmpty());
    }
}