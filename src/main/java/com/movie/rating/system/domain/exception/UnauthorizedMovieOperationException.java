package com.movie.rating.system.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a user tries to perform an unauthorized operation on a movie.
 */
public class UnauthorizedMovieOperationException extends RuntimeException {

    private final UUID movieId;
    private final UUID userId;
    private final String operation;

    public UnauthorizedMovieOperationException(UUID movieId, UUID userId, String operation) {
        super(String.format("User %s is not authorized to %s movie %s", userId, operation, movieId));
        this.movieId = movieId;
        this.userId = userId;
        this.operation = operation;
    }

    public UnauthorizedMovieOperationException(String message) {
        super(message);
        this.movieId = null;
        this.userId = null;
        this.operation = null;
    }

    public UUID getMovieId() {
        return movieId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getOperation() {
        return operation;
    }
}
