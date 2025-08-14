package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = {"classpath:schema.sql", "classpath:test-data-mpa.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MpaDaoImplTest {
    @Autowired
    private MpaDaoImpl mpaDao;

    @Test
    @Order(1)
    void testGetAllMpa() {
        List<Mpa> mpaList = mpaDao.getAll();
        assertNotNull(mpaList);
        assertFalse(mpaList.isEmpty(), "MPA list should not be empty after loading test data");
        assertEquals(5, mpaList.size(), "Expected 5 MPA ratings");
        // Для отладки: вывод содержимого таблицы
        System.out.println("MPA records: " + mpaList);
    }

    @Test
    @Order(2)
    void testGetMpaById() {
        Mpa mpa = new Mpa();
        mpa.setName("G");
        mpa = mpaDao.create(mpa);

        Optional<Mpa> found = mpaDao.getById(mpa.getId());
        assertTrue(found.isPresent());
        assertEquals("G", found.get().getName());
    }

    @Test
    @Order(3)
    void testGetNonExistentMpa() {
        Optional<Mpa> found = mpaDao.getById(999L);
        assertTrue(found.isEmpty());
    }
}