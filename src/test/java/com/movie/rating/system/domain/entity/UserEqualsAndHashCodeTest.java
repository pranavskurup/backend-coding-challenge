package com.movie.rating.system.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserEqualsAndHashCodeTest {

    @Test
    void shouldBeEqualWhenSameId() {
        UUID id = UUID.randomUUID();
        
        User user1 = User.builder()
                .id(id)
                .username("testuser1")
                .email("test1@example.com")
                .passwordHash("hashedpassword1")
                .build();

        User user2 = User.builder()
                .id(id)
                .username("testuser2")
                .email("test2@example.com")
                .passwordHash("hashedpassword2")
                .build();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        User user1 = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        User user2 = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertNotEquals(user1, user2);
    }

    @Test
    void shouldBeEqualToItself() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertEquals(user, user);
    }

    @Test
    void shouldNotBeEqualToNull() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertNotEquals(user, null);
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertNotEquals(user, "not a user");
    }
}
