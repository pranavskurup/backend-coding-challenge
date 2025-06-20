package com.movie.rating.system.domain.exception;

/**
 * Exception thrown when a requested user is not found.
 */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String userId) {
        super("User with ID '" + userId + "' was not found");
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
