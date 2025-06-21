package com.movie.rating.system.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a movie is not found.
 */
public class MovieNotFoundException extends RuntimeException {

    private final UUID movieId;

    public MovieNotFoundException(UUID movieId) {
        super("Movie not found with ID: " + movieId);
        this.movieId = movieId;
    }

    public MovieNotFoundException(String message) {
        super(message);
        this.movieId = null;
    }

    public MovieNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.movieId = null;
    }

    public UUID getMovieId() {
        return movieId;
    }
}
