package com.movie.rating.system.domain.exception;

/**
 * Exception thrown when JWT token is invalid or expired
 */
public class InvalidTokenException extends DomainException {
    
    public InvalidTokenException() {
        super("Invalid or expired token");
    }
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
