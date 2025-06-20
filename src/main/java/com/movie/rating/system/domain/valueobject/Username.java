package com.movie.rating.system.domain.valueobject;

import com.movie.rating.system.domain.exception.InvalidUsernameException;

import java.util.Objects;

public class Username {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;
    
    private final String value;

    public Username(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUsernameException("Username cannot be null or empty");
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.length() < MIN_LENGTH || trimmedValue.length() > MAX_LENGTH) {
            throw new InvalidUsernameException(
                String.format("Username must be between %d and %d characters", MIN_LENGTH, MAX_LENGTH));
        }
        
        // Allow alphanumeric characters, underscores, and hyphens
        if (!trimmedValue.matches("^[a-zA-Z0-9_-]+$")) {
            throw new InvalidUsernameException("Username can only contain letters, numbers, underscores, and hyphens");
        }
        
        this.value = trimmedValue;
    }

    public static Username of(String value) {
        return new Username(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Username username = (Username) o;
        return Objects.equals(value, username.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
