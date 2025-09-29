package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.util.serializer.InstantSerializer;

import java.time.Instant;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class Event {

    Long eventId;

    Long userId;

    Long entityId;

    EventType eventType;

    Operation operation;

    @JsonSerialize(using = InstantSerializer.class)
    Instant timestamp;
}
