package com.movie.rating.system.domain.exception;

/**
 * Exception thrown when attempting to register a user with a username that already exists
 */
public class UsernameAlreadyExistsException extends DomainException {
    
    public UsernameAlreadyExistsException(String username) {
        super("Username '" + username + "' already exists");
    }
    
    public UsernameAlreadyExistsException(String username, Throwable cause) {
        super("Username '" + username + "' already exists", cause);
    }
}
