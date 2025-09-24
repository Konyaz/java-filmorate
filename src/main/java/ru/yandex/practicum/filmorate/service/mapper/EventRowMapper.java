package ru.yandex.practicum.filmorate.service.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EventRowMapper implements RowMapper<Event> {

    JdbcTemplate jdbc;

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        String eventType = jdbc.queryForObject(
                "SELECT name FROM event_types WHERE id = ?",
                String.class,
                rs.getLong("event_type_id")
        );

        String operation = jdbc.queryForObject(
                "SELECT name FROM operations WHERE id = ?",
                String.class,
                rs.getLong("operation_id")
        );

        return Event.builder()
                .id(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .entityId(rs.getLong("entity_id"))
                .eventType(eventType)
                .operation(operation)
                .timestamp(rs.getTimestamp("event_time").toInstant())
                .build();
    }
}