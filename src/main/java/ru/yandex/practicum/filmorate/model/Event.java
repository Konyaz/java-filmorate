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

    Long id;

    Long userId;

    Long entityId;

    String eventType;

    String operation;

    @JsonSerialize(using = InstantSerializer.class)
    Instant timestamp;
}
