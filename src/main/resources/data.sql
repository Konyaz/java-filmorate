-- Начальные данные для таблицы mpa_ratings
MERGE INTO mpa_ratings KEY (id) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

-- Начальные данные для таблицы genres
MERGE INTO genres KEY (id) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

-- Добавляем тестовых пользователей
INSERT INTO users (email, login, name, birthday) VALUES
('user1@example.com', 'user1', 'User 1', '1990-01-01'),
('user2@example.com', 'user2', 'User 2', '1991-02-02'),
('user3@example.com', 'user3', 'User 3', '1992-03-03');

-- Добавляем дружеские связи для тестов
INSERT INTO friends (user_id, friend_id) VALUES
(1, 2), -- Для getFriends_success
(1, 3), -- Для getCommonFriends_success
(2, 3); -- Для getCommonFriends_success

-- Добавляем тестовые фильмы
INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES
('Film 1', 'Description 1', '2000-01-01', 120, 1),
('Film 2', 'Description 2', '2001-01-01', 130, 2),
('Film 3', 'Description 3', '2002-01-01', 140, 3),
('Film 4', 'Description 4', '2003-01-01', 110, 4),
('Film 5', 'Description 5', '2004-01-01', 100, 5),
('Film 6', 'Description 6', '2005-01-01', 150, 1),
('Film 7', 'Description 7', '2006-01-01', 125, 2);

-- Добавляем жанры для фильмов
INSERT INTO film_genre (film_id, genre_id) VALUES
(1, 1), (1, 2),
(2, 2), (2, 3),
(3, 3), (3, 4),
(4, 4), (4, 5),
(5, 5), (5, 6),
(6, 6), (6, 1),
(7, 1), (7, 2);

-- Добавляем лайки для фильмов
INSERT INTO likes (film_id, user_id) VALUES
(1, 1), (1, 2),
(2, 1),
(3, 1), (3, 2), (3, 3),
(4, 2),
(5, 1), (5, 2),
(6, 1), (6, 2), (6, 3),
(7, 1);