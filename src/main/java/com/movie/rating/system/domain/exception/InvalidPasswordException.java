package com.movie.rating.system.domain.exception;

/**
 * Exception thrown when attempting to change password with an invalid current password.
 */
public class InvalidPasswordException extends DomainException {

    public InvalidPasswordException() {
        super("Current password is incorrect");
    }
    
    public InvalidPasswordException(String message) {
        super(message);
    }
    
    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
