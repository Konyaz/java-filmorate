package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.mapper.EventRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EventDaoImpl implements EventDao {

    JdbcTemplate jdbc;
    EventRowMapper rowMapper;

    @Override
    public Event findById(Long eventId) {
        String sql = "SELECT * FROM events WHERE event_id = ?";

        return jdbc.queryForObject(sql, rowMapper, eventId);
    }

    @Override
    public Event saveEvent(EventDto eventData) {
        String sql = "INSERT INTO events (user_id, entity_id, event_type_id, operation_id, event_time)" +
                " VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, eventData.getUserId());
            stmt.setLong(2, eventData.getEntityId());
            stmt.setLong(3, eventData.getEventTypeId());
            stmt.setLong(4, eventData.getOperationId());
            stmt.setTimestamp(5, Timestamp.from(eventData.getTimestamp()));
            return stmt;
        }, keyHolder);

        return findById(Objects.requireNonNull(keyHolder.getKey()).longValue());
    }

    @Override
    public List<Event> findByUserId(Long userId) {
        String sql = "SELECT * FROM events WHERE user_id = ?";

        return jdbc.query(sql, rowMapper, userId);
    }
}
