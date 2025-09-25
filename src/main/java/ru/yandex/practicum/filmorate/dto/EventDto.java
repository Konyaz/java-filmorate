package ru.yandex.practicum.filmorate.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventDto {

    Long userId;

    Long entityId;

    Long eventTypeId;

    Long operationId;

    Instant timestamp;
}