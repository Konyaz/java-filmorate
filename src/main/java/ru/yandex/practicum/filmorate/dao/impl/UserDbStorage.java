package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        final String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(id);
        log.info("User created id={}", id);
        return user;
    }

    @Override
    public User update(User user) {
        final String sql = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";
        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());

        if (updated == 0) {
            throw new NotFoundException("User not found: id=" + user.getId());
        }
        log.info("User updated id={}", user.getId());
        return user;
    }

    @Override
    public List<User> getAll() {
        final String sql = "SELECT id, email, login, name, birthday FROM users ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rn) -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            if (rs.getDate("birthday") != null) {
                u.setBirthday(rs.getDate("birthday").toLocalDate());
            }
            return u;
        });
    }

    @Override
    public Optional<User> getById(Long id) {
        final String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        try {
            User u = jdbcTemplate.queryForObject(sql, (rs, rn) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setEmail(rs.getString("email"));
                user.setLogin(rs.getString("login"));
                user.setName(rs.getString("name"));
                if (rs.getDate("birthday") != null) {
                    user.setBirthday(rs.getDate("birthday").toLocalDate());
                }
                return user;
            }, id);
            return Optional.ofNullable(u);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        throw new UnsupportedOperationException("Используйте FriendDaoImpl для операций с друзьями");
    }

    @Override
    public void removeFriend(Long id, Long friendId) {
        throw new UnsupportedOperationException("Используйте FriendDaoImpl для операций с друзьями");
    }

    @Override
    public void confirmFriend(Long id, Long friendId) {
        throw new UnsupportedOperationException("Используйте FriendDaoImpl для операций с друзьями");
    }

    @Override
    public List<User> getFriends(Long id) {
        throw new UnsupportedOperationException("Используйте FriendDaoImpl для операций с друзьями");
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        throw new UnsupportedOperationException("Используйте FriendDaoImpl для операций с друзьями");
    }
}