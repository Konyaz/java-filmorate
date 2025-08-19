-- 1. Создание пользователя
INSERT INTO users (email, login, name, birthday)
VALUES ('user@example.com', 'login123', 'User Name', '1990-01-01');

-- 2. Добавление связи дружбы
INSERT INTO friends (user_id, friend_id)
VALUES (1, 2);

-- 3. Добавление фильма
INSERT INTO films (name, description, release_date, duration, mpa_id)
VALUES ('Inception', 'Dream within a dream', '2010-07-16', 148, 3);

-- 4. Добавление жанра к фильму
INSERT INTO film_genres (film_id, genre_id)
VALUES (1, 4), (1, 6); -- Триллер и Боевик

-- 5. Получение топ-5 фильмов
SELECT f.id, f.name, COUNT(l.user_id) AS likes_count
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id, f.name
ORDER BY likes_count DESC
LIMIT 5;

-- 6. Поиск общих друзей
SELECT u.*
FROM friends f1
JOIN friends f2 ON f1.friend_id = f2.friend_id
JOIN users u ON f1.friend_id = u.id
WHERE f1.user_id = 1
  AND f2.user_id = 2;

-- 7. Получение фильмов по жанру
SELECT f.*
FROM films f
JOIN film_genres fg ON f.id = fg.film_id
WHERE fg.genre_id = 1; -- Комедия