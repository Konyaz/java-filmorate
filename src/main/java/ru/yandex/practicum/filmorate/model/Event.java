package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

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

    Instant timestamp;
}
