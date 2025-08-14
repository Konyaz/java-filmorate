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
@Sql(scripts = {"classpath:schema.sql", "classpath:test-data-mpa.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MpaDaoImplTest {
    @Autowired
    private MpaDaoImpl mpaDao;

    @Test
    void testGetAllMpa() {
        List<Mpa> mpaList = mpaDao.getAll();
        assertNotNull(mpaList, "MPA list should not be null");
        assertFalse(mpaList.isEmpty(), "MPA list should not be empty after loading test data");
        assertEquals(5, mpaList.size(), "Expected 5 MPA ratings");
    }

    @Test
    void testGetMpaById() {
        Optional<Mpa> found = mpaDao.getById(1L);
        assertTrue(found.isPresent(), "MPA should be found");
        assertEquals("G", found.get().getName(), "MPA name should be G");
    }

    @Test
    void testGetNonExistentMpa() {
        Optional<Mpa> found = mpaDao.getById(999L);
        assertTrue(found.isEmpty(), "Non-existent MPA should return empty");
    }
}