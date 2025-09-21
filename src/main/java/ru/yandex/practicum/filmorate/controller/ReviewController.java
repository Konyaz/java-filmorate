package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;
    public static final String DEFAULT_REVIEW_LIST_COUNT = "10";

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Review> findList(@RequestParam(required = false) Long filmId,
                                       @RequestParam(defaultValue = DEFAULT_REVIEW_LIST_COUNT) Integer count) {
        if (filmId != null) {
            return service.getListByFilmId(filmId, count);
        }
        return service.getList(count);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Review findById(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review create(@RequestBody Review newReview) {
        return service.add(newReview);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Review update(@RequestBody Review review) {
        return service.update(review);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        service.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addDislike(@PathVariable long id, @PathVariable long userId) {
        service.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        service.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDislike(@PathVariable long id, @PathVariable long userId) {
        service.removeDislike(id, userId);
    }

}
