package com.movie.rating.system.domain.entity;

import com.movie.rating.system.domain.exception.InvalidUsernameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class UserUsernameValidationTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void shouldThrowExceptionForInvalidUsernameNullOrEmpty(String username) {
        InvalidUsernameException exception = assertThrows(InvalidUsernameException.class, () -> {
            User.builder()
                    .username(username)
                    .email("test@example.com")
                    .passwordHash("hashedpassword")
                    .build();
        });

        assertEquals("Username cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a", ""})
    void shouldThrowExceptionForUsernameTooShort(String username) {
        if (username.isEmpty()) return; // Handled by null/empty test

        InvalidUsernameException exception = assertThrows(InvalidUsernameException.class, () -> {
            User.builder()
                    .username(username)
                    .email("test@example.com")
                    .passwordHash("hashedpassword")
                    .build();
        });

        assertTrue(exception.getMessage().contains("Username must be between 3 and 100 characters"));
    }

    @Test
    void shouldThrowExceptionForUsernameTooLong() {
        String longUsername = "a".repeat(101);

        InvalidUsernameException exception = assertThrows(InvalidUsernameException.class, () -> {
            User.builder()
                    .username(longUsername)
                    .email("test@example.com")
                    .passwordHash("hashedpassword")
                    .build();
        });

        assertTrue(exception.getMessage().contains("Username must be between 3 and 100 characters"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@name", "user name", "user.name", "user+name", "user#name", "user$name"})
    void shouldThrowExceptionForUsernameWithInvalidCharacters(String username) {
        InvalidUsernameException exception = assertThrows(InvalidUsernameException.class, () -> {
            User.builder()
                    .username(username)
                    .email("test@example.com")
                    .passwordHash("hashedpassword")
                    .build();
        });

        assertTrue(exception.getMessage().contains("Username can only contain letters, numbers, underscores, and hyphens"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user123", "User_Name", "user-name", "USER", "user_123", "123user", "a_b-c"})
    void shouldAcceptValidUsernames(String username) {
        assertDoesNotThrow(() -> {
            User user = User.builder()
                    .username(username)
                    .email("test@example.com")
                    .passwordHash("hashedpassword")
                    .build();

            assertEquals(username, user.getUsername());
        });
    }

    @Test
    void shouldAcceptUsernameAtMinLength() {
        String username = "abc";

        User user = User.builder()
                .username(username)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertEquals(username, user.getUsername());
    }

    @Test
    void shouldAcceptUsernameAtMaxLength() {
        String username = "a".repeat(100);

        User user = User.builder()
                .username(username)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertEquals(username, user.getUsername());
    }
}
