package com.movie.rating.system.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a user tries to rate a movie they've already rated.
 */
public class DuplicateRatingException extends RuntimeException {

    private final UUID movieId;
    private final UUID userId;

    public DuplicateRatingException(UUID movieId, UUID userId) {
        super(String.format("User %s has already rated movie %s", userId, movieId));
        this.movieId = movieId;
        this.userId = userId;
    }

    public DuplicateRatingException(String message) {
        super(message);
        this.movieId = null;
        this.userId = null;
    }

    public UUID getMovieId() {
        return movieId;
    }

    public UUID getUserId() {
        return userId;
    }
}
