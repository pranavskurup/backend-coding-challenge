package com.movie.rating.system.domain.entity;

import com.movie.rating.system.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class UserEmailValidationTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void shouldThrowExceptionForInvalidEmailNullOrEmpty(String email) {
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            User.builder()
                    .username("testuser")
                    .email(email)
                    .passwordHash("hashedpassword")
                    .build();
        });

        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email",
            "@example.com",
            "user@",
            "user.example.com",
            "user@domain",
            "user..name@example.com",
            "user@example..com",
            "user@.example.com",
            "user@example.",
            "user name@example.com",
            "user@ex ample.com"
    })
    void shouldThrowExceptionForInvalidEmailFormats(String email) {
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            User.builder()
                    .username("testuser")
                    .email(email)
                    .passwordHash("hashedpassword")
                    .build();
        });

        assertTrue(exception.getMessage().contains("Invalid email format: " + email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "test.email@domain.org",
            "user123@test-domain.co.uk",
            "firstname.lastname@company.com",
            "user+tag@example.net",
            "simple@domain.io",
            "123@numbers.com",
            "a@b.co"
    })
    void shouldAcceptValidEmailFormats(String email) {
        assertDoesNotThrow(() -> {
            User user = User.builder()
                    .username("testuser")
                    .email(email)
                    .passwordHash("hashedpassword")
                    .build();

            assertEquals(email, user.getEmail());
        });
    }

    @Test
    void shouldThrowExceptionForEmailWithConsecutiveDots() {
        String email = "user..name@example.com";

        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            User.builder()
                    .username("testuser")
                    .email(email)
                    .passwordHash("hashedpassword")
                    .build();
        });

        assertTrue(exception.getMessage().contains("Invalid email format: " + email));
    }

    @Test
    void shouldThrowExceptionForEmailWithoutDomainTLD() {
        String email = "user@domain";

        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            User.builder()
                    .username("testuser")
                    .email(email)
                    .passwordHash("hashedpassword")
                    .build();
        });

        assertTrue(exception.getMessage().contains("Invalid email format: " + email));
    }
}
