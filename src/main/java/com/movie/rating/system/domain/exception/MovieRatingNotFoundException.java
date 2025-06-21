package com.movie.rating.system.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a movie rating is not found.
 */
public class MovieRatingNotFoundException extends RuntimeException {

    private final UUID ratingId;

    public MovieRatingNotFoundException(UUID ratingId) {
        super("Movie rating not found with ID: " + ratingId);
        this.ratingId = ratingId;
    }

    public MovieRatingNotFoundException(String message) {
        super(message);
        this.ratingId = null;
    }

    public MovieRatingNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.ratingId = null;
    }

    public UUID getRatingId() {
        return ratingId;
    }
}
