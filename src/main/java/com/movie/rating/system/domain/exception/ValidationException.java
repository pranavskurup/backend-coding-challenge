package com.movie.rating.system.domain.exception;

import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends DomainException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = Map.of();
    }
    
    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors != null ? errors : Map.of();
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
