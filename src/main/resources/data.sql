-- Начальные данные для таблицы mpa
MERGE INTO mpa KEY (id) VALUES
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
INSERT INTO friends (user_id, friend_id, status) VALUES
(1, 2, 'подтверждённая'),
(1, 3, 'неподтверждённая'),
(2, 3, 'подтверждённая');

-- Добавляем тестовые фильмы
INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES
('Film 1', 'Description 1', '2000-01-01', 120, 1),
('Film 2', 'Description 2', '2001-01-01', 130, 2);