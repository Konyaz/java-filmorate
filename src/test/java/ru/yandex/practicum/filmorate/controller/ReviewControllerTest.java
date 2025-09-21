package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.UserDao;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReviewControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserDao userStorage;

    @Autowired
    private FilmDao filmStorage;

    private User testUser;
    private User testUser2;
    private Film testFilm;
    private Review testReview;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testuser");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser = userStorage.create(testUser);

        // Создаем второго тестового пользователя
        testUser2 = new User();
        testUser2.setEmail("test2@example.com");
        testUser2.setLogin("testuser2");
        testUser2.setName("Test User 2");
        testUser2.setBirthday(LocalDate.of(1990, 1, 1));
        testUser2 = userStorage.create(testUser2);

        // Создаем тестовый фильм
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        testFilm.setMpa(mpa);

        testFilm = filmStorage.create(testFilm);

        // Создаем тестовый отзыв
        testReview = new Review();
        testReview.setContent("Test review content");
        testReview.setIsPositive(true);
        testReview.setUserId(testUser.getId());
        testReview.setFilmId(testFilm.getId());
    }

    @Test
    void shouldCreateReview() {
        // Act
        ResponseEntity<Review> response = restTemplate.postForEntity(
                "/reviews", testReview, Review.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getReviewId());
        assertEquals(testReview.getContent(), response.getBody().getContent());
        assertEquals(0, response.getBody().getUseful()); // Рейтинг должен быть 0 при создании
    }

    @Test
    void shouldGetReviewById() {
        // Arrange
        Review createdReview = restTemplate.postForObject("/reviews", testReview, Review.class);

        // Act
        ResponseEntity<Review> response = restTemplate.getForEntity(
                "/reviews/" + createdReview.getReviewId(), Review.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdReview.getReviewId(), response.getBody().getReviewId());
        assertEquals(createdReview.getContent(), response.getBody().getContent());
    }

    @Test
    void shouldUpdateReview() {
        // Arrange
        Review createdReview = restTemplate.postForObject("/reviews", testReview, Review.class);
        createdReview.setContent("Updated content");
        createdReview.setIsPositive(false);

        // Act
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Review> requestEntity = new HttpEntity<>(createdReview, headers);
        ResponseEntity<Review> response = restTemplate.exchange(
                "/reviews", HttpMethod.PUT, requestEntity, Review.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated content", response.getBody().getContent());
        assertFalse(response.getBody().getIsPositive());
    }

    @Test
    void shouldDeleteReview() {
        // Arrange
        Review createdReview = restTemplate.postForObject("/reviews", testReview, Review.class);

        // Act
        restTemplate.delete("/reviews/" + createdReview.getReviewId());

        // Assert
        ResponseEntity<Review> response = restTemplate.getForEntity(
                "/reviews/" + createdReview.getReviewId(), Review.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetAllReviewsWhenFilmIdNotSpecified() {
        // Arrange
        Review createdReview = restTemplate.postForObject("/reviews", testReview, Review.class);

        // Act
        ResponseEntity<Review[]> response = restTemplate.getForEntity(
                "/reviews?count=10", Review[].class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 1);
    }

    @Test
    void shouldAddLikeToReview() {
        // Arrange
        Review createdReview = restTemplate.postForObject("/reviews", testReview, Review.class);

        // Act
        restTemplate.put("/reviews/" + createdReview.getReviewId() + "/like/" + testUser2.getId(), null);

        // Assert
        ResponseEntity<Review> response = restTemplate.getForEntity(
                "/reviews/" + createdReview.getReviewId(), Review.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getUseful()); // Рейтинг должен увеличиться на 1
    }

    @Test
    void shouldAddDislikeToReview() {
        // Arrange
        Review createdReview = restTemplate.postForObject("/reviews", testReview, Review.class);

        // Act
        restTemplate.put("/reviews/" + createdReview.getReviewId() + "/dislike/" + testUser2.getId(), null);

        // Assert
        ResponseEntity<Review> response = restTemplate.getForEntity(
                "/reviews/" + createdReview.getReviewId(), Review.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(-1, response.getBody().getUseful()); // Рейтинг должен уменьшиться на 1
    }

    @Test
    void shouldRemoveLikeFromReview() {
        // Arrange
        Review createdReview = restTemplate.postForObject("/reviews", testReview, Review.class);
        restTemplate.put("/reviews/" + createdReview.getReviewId() + "/like/" + testUser2.getId(), null);

        // Act
        restTemplate.delete("/reviews/" + createdReview.getReviewId() + "/like/" + testUser2.getId());

        // Assert
        ResponseEntity<Review> response = restTemplate.getForEntity(
                "/reviews/" + createdReview.getReviewId(), Review.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getUseful()); // Рейтинг должен вернуться к 0
    }

    @Test
    void shouldRemoveDislikeFromReview() {
        // Arrange
        Review createdReview = restTemplate.postForObject("/reviews", testReview, Review.class);
        restTemplate.put("/reviews/" + createdReview.getReviewId() + "/dislike/" + testUser2.getId(), null);

        // Act
        restTemplate.delete("/reviews/" + createdReview.getReviewId() + "/dislike/" + testUser2.getId());

        // Assert
        ResponseEntity<Review> response = restTemplate.getForEntity(
                "/reviews/" + createdReview.getReviewId(), Review.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getUseful()); // Рейтинг должен вернуться к 0
    }

    @Test
    void shouldReturnNotFoundForNonExistingReview() {
        // Act
        ResponseEntity<Review> response = restTemplate.getForEntity("/reviews/999", Review.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestForInvalidReview() {
        // Arrange - создаем отзыв без обязательных полей
        Review invalidReview = new Review();
        invalidReview.setIsPositive(true);

        // Act
        ResponseEntity<Review> response = restTemplate.postForEntity("/reviews", invalidReview, Review.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundWhenAddingLikeToNonExistingReview() {
        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                "/reviews/999/like/" + testUser.getId(),
                HttpMethod.PUT,
                null,
                Void.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}