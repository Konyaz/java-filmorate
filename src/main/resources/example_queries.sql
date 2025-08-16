-- 1. Создание пользователя
INSERT INTO users (email, login, name, birthday)
VALUES ('user@example.com', 'login123', 'User Name', '1990-01-01');

-- 2. Добавление заявки в друзья
INSERT INTO friendships (user_id, friend_id)
VALUES (1, 2); -- Статус по умолчанию 'pending'

-- 3. Подтверждение дружбы
UPDATE friendships
SET status = 'approved'
WHERE user_id = 2 AND friend_id = 1;

-- 4. Добавление фильма
INSERT INTO films (name, description, release_date, duration, mpa_id)
VALUES ('Inception', 'Dream within a dream', '2010-07-16', 148, 3);

-- 5. Добавление жанра к фильму
INSERT INTO film_genres (film_id, genre_id)
VALUES (1, 4), (1, 6); -- Триллер и Боевик

-- 6. Получение топ-5 фильмов
SELECT f.id, f.name, COUNT(l.user_id) AS likes_count
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id
ORDER BY likes_count DESC
LIMIT 5;

-- 7. Поиск общих друзей
SELECT u.*
FROM friendships f1
JOIN friendships f2 ON f1.friend_id = f2.friend_id
JOIN users u ON f1.friend_id = u.id
WHERE f1.user_id = 1
  AND f2.user_id = 2
  AND f1.status = 'approved'
  AND f2.status = 'approved';

-- 8. Получение фильмов по жанру
SELECT f.*
FROM films f
JOIN film_genres fg ON f.id = fg.film_id
WHERE fg.genre_id = 1; -- Комедия