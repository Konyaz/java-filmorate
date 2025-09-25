-- MPA данные
MERGE INTO mpa (id, name) VALUES (1, 'G');
MERGE INTO mpa (id, name) VALUES (2, 'PG');
MERGE INTO mpa (id, name) VALUES (3, 'PG-13');
MERGE INTO mpa (id, name) VALUES (4, 'R');
MERGE INTO mpa (id, name) VALUES (5, 'NC-17');

-- Жанры
MERGE INTO genres (id, name) VALUES (1, 'Комедия');
MERGE INTO genres (id, name) VALUES (2, 'Драма');
MERGE INTO genres (id, name) VALUES (3, 'Мультфильм');
MERGE INTO genres (id, name) VALUES (4, 'Триллер');
MERGE INTO genres (id, name) VALUES (5, 'Документальный');
MERGE INTO genres (id, name) VALUES (6, 'Боевик');

-- Тестовые фильмы (используем ID, которые не конфликтуют с тестовыми данными)
MERGE INTO films (id, name, description, release_date, duration, mpa_id)
VALUES (100, 'Властелин колец', 'Эпическое фэнтези о приключениях Фродо', '2001-12-19', 178, 3);
MERGE INTO films (id, name, description, release_date, duration, mpa_id)
VALUES (200, 'Комедия про любовь', 'Весёлая история о романтике', '2010-03-15', 95, 2);
MERGE INTO films (id, name, description, release_date, duration, mpa_id)
VALUES (300, 'Триллер в ночи', 'Напряжённый сюжет о тайнах', '2015-07-22', 120, 4);

-- Жанры для фильмов
MERGE INTO film_genres (film_id, genre_id) VALUES (100, 2);
MERGE INTO film_genres (film_id, genre_id) VALUES (100, 6);
MERGE INTO film_genres (film_id, genre_id) VALUES (200, 1);
MERGE INTO film_genres (film_id, genre_id) VALUES (300, 4);

-- Тестовые пользователи
-- ПЕРЕНЕСЁНЫ на "высокие" ID, чтобы не конфликтовать с пользователями, которые создаются в тестах (autoincrement).
MERGE INTO users (id, email, login, name, birthday) VALUES (1000, 'user1@example.com', 'user1', 'User One', '1990-01-01');
MERGE INTO users (id, email, login, name, birthday) VALUES (1001, 'user2@example.com', 'user2', 'User Two', '1992-02-02');

-- Лайки для проверки сортировки
-- Обновлены ID пользователей на 1000/1001, чтобы соответствовать изменённым тестовым пользователям выше.
MERGE INTO likes (film_id, user_id) VALUES (100, 1000);
MERGE INTO likes (film_id, user_id) VALUES (100, 1001);
MERGE INTO likes (film_id, user_id) VALUES (200, 1000);

MERGE INTO event_types (id, name) VALUES (1, 'LIKE');
MERGE INTO event_types (id, name) VALUES (2, 'REVIEW');
MERGE INTO event_types (id, name) VALUES (3, 'FRIEND');
MERGE INTO event_types (id, name) VALUES (4, 'DISLIKE');

MERGE INTO operations (id, name) VALUES (1, 'ADD');
MERGE INTO operations (id, name) VALUES (2, 'REMOVE');
MERGE INTO operations (id, name) VALUES (3, 'UPDATE');
