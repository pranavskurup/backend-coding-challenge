package com.movie.rating.system.domain.exception;

/**
 * Exception thrown when attempting to register a user with an email that already exists
 */
public class EmailAlreadyExistsException extends DomainException {
    
    public EmailAlreadyExistsException(String email) {
        super("Email '" + email + "' already exists");
    }
    
    public EmailAlreadyExistsException(String email, Throwable cause) {
        super("Email '" + email + "' already exists", cause);
    }
}
