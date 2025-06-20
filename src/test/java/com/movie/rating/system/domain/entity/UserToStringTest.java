package com.movie.rating.system.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserToStringTest {

    @Test
    void shouldNotIncludePasswordHashInToString() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("secretpassword")
                .build();

        String toString = user.toString();

        assertFalse(toString.contains("secretpassword"));
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
    }
}
