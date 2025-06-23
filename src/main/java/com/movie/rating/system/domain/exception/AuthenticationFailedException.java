package com.movie.rating.system.domain.exception;

/**
 * Exception thrown when user authentication fails
 */
public class AuthenticationFailedException extends DomainException {
    
    public AuthenticationFailedException() {
        super("Invalid username/email or password");
    }
    
    public AuthenticationFailedException(String message) {
        super(message);
    }
    
    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
