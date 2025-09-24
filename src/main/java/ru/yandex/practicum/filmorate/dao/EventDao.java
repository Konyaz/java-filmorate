package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventDao {

    Event findById(Long eventId);

    Event saveEvent(EventDto eventData);

    List<Event> findByUserId(Long userId);
}
