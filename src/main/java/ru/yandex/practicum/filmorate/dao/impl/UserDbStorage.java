package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        user.setId(id);
        updateFriends(user);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        if (rowsAffected == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        updateFriends(user);
        return user;
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper());
    }

    @Override
    public Optional<User> getById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper(), id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    private void updateFriends(User user) {
        String deleteFriendsSql = "DELETE FROM friends WHERE user_id = ?";
        jdbcTemplate.update(deleteFriendsSql, user.getId());
        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            String insertFriendsSql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
            for (Long friendId : user.getFriends()) {
                jdbcTemplate.update(insertFriendsSql, user.getId(), friendId);
            }
        }
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            user.setFriends(getFriends(rs.getLong("id")));
            return user;
        };
    }

    private Set<Long> getFriends(Long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId));
    }
}