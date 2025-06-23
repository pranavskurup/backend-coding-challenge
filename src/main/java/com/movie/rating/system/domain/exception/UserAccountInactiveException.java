package com.movie.rating.system.domain.exception;

/**
 * Exception thrown when attempting to perform operations on an inactive user account.
 */
public class UserAccountInactiveException extends DomainException {

    public UserAccountInactiveException(String userId) {
        super("User account with ID '" + userId + "' is inactive");
    }
    
    public UserAccountInactiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
