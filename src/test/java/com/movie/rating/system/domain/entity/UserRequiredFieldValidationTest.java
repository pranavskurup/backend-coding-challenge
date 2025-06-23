package com.movie.rating.system.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRequiredFieldValidationTest {

    @Test
    void shouldThrowExceptionForNullPasswordHash() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            User.builder()
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash(null)
                    .build();
        });

        assertEquals("Password hash cannot be null", exception.getMessage());
    }
}
