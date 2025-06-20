package com.movie.rating.system.domain.valueobject;

import com.movie.rating.system.domain.exception.InvalidEmailException;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]([a-zA-Z0-9._+%-]*[a-zA-Z0-9])?@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );
    
    private final String value;

    public Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidEmailException("Email cannot be null or empty");
        }
        
        String trimmedValue = value.trim().toLowerCase();
        
        // Check for consecutive dots
        if (trimmedValue.contains("..")) {
            throw new InvalidEmailException("Invalid email format: " + value);
        }
        
        // Additional check: ensure domain has at least one dot (for TLD)
        int atIndex = trimmedValue.indexOf('@');
        if (atIndex != -1 && atIndex < trimmedValue.length() - 1) {
            String domain = trimmedValue.substring(atIndex + 1);
            if (!domain.contains(".")) {
                throw new InvalidEmailException("Invalid email format: " + value);
            }
        }
        
        if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
            throw new InvalidEmailException("Invalid email format: " + value);
        }
        
        this.value = trimmedValue;
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
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
