package com.movie.rating.system.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserId Value Object Tests")
class UserIdTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create UserId with valid UUID")
        void shouldCreateUserIdWithValidUuid() {
            // Given
            UUID uuid = UUID.randomUUID();

            // When
            UserId userId = new UserId(uuid);

            // Then
            assertNotNull(userId);
            assertEquals(uuid, userId.getValue());
        }

        @Test
        @DisplayName("Should generate new UserId")
        void shouldGenerateNewUserId() {
            // When
            UserId userId = UserId.generate();

            // Then
            assertNotNull(userId);
            assertNotNull(userId.getValue());
        }

        @Test
        @DisplayName("Should create UserId from UUID")
        void shouldCreateUserIdFromUuid() {
            // Given
            UUID uuid = UUID.randomUUID();

            // When
            UserId userId = UserId.of(uuid);

            // Then
            assertNotNull(userId);
            assertEquals(uuid, userId.getValue());
        }

        @Test
        @DisplayName("Should create UserId from string")
        void shouldCreateUserIdFromString() {
            // Given
            String uuidString = "123e4567-e89b-12d3-a456-426614174000";

            // When
            UserId userId = UserId.of(uuidString);

            // Then
            assertNotNull(userId);
            assertEquals(UUID.fromString(uuidString), userId.getValue());
        }

        @Test
        @DisplayName("Should throw exception when UUID is null")
        void shouldThrowExceptionWhenUuidIsNull() {
            // When & Then
            assertThrows(NullPointerException.class, () -> new UserId(null));
        }

        @Test
        @DisplayName("Should throw exception when string is invalid UUID")
        void shouldThrowExceptionWhenStringIsInvalidUuid() {
            // Given
            String invalidUuid = "invalid-uuid";

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> UserId.of(invalidUuid));
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when UUIDs are same")
        void shouldBeEqualWhenUuidsAreSame() {
            // Given
            UUID uuid = UUID.randomUUID();
            UserId userId1 = new UserId(uuid);
            UserId userId2 = new UserId(uuid);

            // When & Then
            assertEquals(userId1, userId2);
            assertEquals(userId1.hashCode(), userId2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when UUIDs are different")
        void shouldNotBeEqualWhenUuidsAreDifferent() {
            // Given
            UserId userId1 = UserId.generate();
            UserId userId2 = UserId.generate();

            // When & Then
            assertNotEquals(userId1, userId2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            UserId userId = UserId.generate();

            // When & Then
            assertNotEquals(userId, null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            UserId userId = UserId.generate();
            String string = "test";

            // When & Then
            assertNotEquals(userId, string);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            UserId userId = UserId.generate();

            // When & Then
            assertEquals(userId, userId);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should return UUID string representation")
        void shouldReturnUuidStringRepresentation() {
            // Given
            UUID uuid = UUID.randomUUID();
            UserId userId = new UserId(uuid);

            // When
            String result = userId.toString();

            // Then
            assertEquals(uuid.toString(), result);
        }
    }
}
