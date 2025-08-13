# Проектирование базы данных для Filmorate

## ER-диаграмма
![ER-диаграмма базы данных](src/image/filmorate_er_diagram.png)

## Описание схемы
База данных состоит из 7 таблиц, соответствующих требованиям ТЗ:
1. **users** - информация о пользователях
2. **films** - данные о фильмах
3. **mpa_ratings** - рейтинги MPA (G, PG, PG-13 и т.д.)
4. **genres** - жанры фильмов
5. **friendships** - связи дружбы между пользователями (с подтверждением)
6. **film_genres** - связь фильмов с жанрами
7. **likes** - лайки пользователей

Схема соответствует 3НФ:
- Все атрибуты атомарны
- Нет частичных зависимостей
- Нет транзитивных зависимостей

## Основные бизнес-сценарии
1. **Дружба с подтверждением**: 
   - Пользователь A отправляет запрос → статус "pending"
   - Пользователь B подтверждает → статус "approved"
2. **Множественные жанры**: Один фильм может относиться к нескольким жанрам
3. **Рейтинги MPA**: Фильмы имеют возрастные ограничения
4. **Лайки**: Пользователи могут ставить лайки фильмам

## Примеры запросов
Полные примеры SQL-запросов для основных операций доступны в [example_queries.sql](src/main/resources/example_queries.sql)

### Ключевые запросы:
```sql
-- Топ-5 популярных фильмов
SELECT f.id, f.name, COUNT(l.user_id) AS likes
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id
ORDER BY likes DESC
LIMIT 5;

-- Неподтвержденные заявки в друзья
SELECT u.login, f.created_at 
FROM friendships f
JOIN users u ON f.friend_id = u.id
WHERE f.user_id = 1
AND f.status = 'pending';

-- Фильмы с рейтингом PG-13
SELECT f.name, m.name AS mpa
FROM films f
JOIN mpa_ratings m ON f.mpa_id = m.id
WHERE m.name = 'PG-13';