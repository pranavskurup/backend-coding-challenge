package com.movie.rating.system.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserBuilderValidationTest {

    @Test
    void shouldCreateValidUserWithMinimalRequiredFields() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedpassword", user.getPasswordHash());
        assertTrue(user.isActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNull(user.getDeactivatedAt());
    }

    @Test
    void shouldCreateUserWithAllFields() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();

        User user = User.builder()
                .id(id)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(false)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deactivatedAt(updatedAt)
                .build();

        assertEquals(id, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedpassword", user.getPasswordHash());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertFalse(user.isActive());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(updatedAt, user.getUpdatedAt());
        assertEquals(updatedAt, user.getDeactivatedAt());
    }

    @Test
    void shouldAutoGenerateIdWhenNotProvided() {
        User user1 = User.builder()
                .username("testuser1")
                .email("test1@example.com")
                .passwordHash("hashedpassword")
                .build();

        User user2 = User.builder()
                .username("testuser2")
                .email("test2@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertNotNull(user1.getId());
        assertNotNull(user2.getId());
        assertNotEquals(user1.getId(), user2.getId());
    }

    @Test
    void shouldAutoGenerateTimestampsWhenNotProvided() {
        Instant before = Instant.now().minusSeconds(1);
        
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        Instant after = Instant.now().plusSeconds(1);

        assertTrue(user.getCreatedAt().isAfter(before));
        assertTrue(user.getCreatedAt().isBefore(after));
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    void shouldDefaultToActiveWhenNotSpecified() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertTrue(user.isActive());
    }
}
