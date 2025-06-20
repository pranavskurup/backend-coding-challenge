package com.movie.rating.system.domain.valueobject;

import com.movie.rating.system.domain.exception.InvalidUsernameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Username Value Object Tests")
class UsernameTest {

    @Nested
    @DisplayName("Valid Username Creation Tests")
    class ValidUsernameCreationTests {

        @ParameterizedTest
        @ValueSource(strings = {"user123", "test_user", "user-name", "abc", "a123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"})
        @DisplayName("Should create username with valid values")
        void shouldCreateUsernameWithValidValues(String validUsername) {
            // When
            Username username = new Username(validUsername);

            // Then
            assertNotNull(username);
            assertEquals(validUsername, username.getValue());
        }

        @Test
        @DisplayName("Should create username using of() method")
        void shouldCreateUsernameUsingOfMethod() {
            // Given
            String usernameValue = "testuser";

            // When
            Username username = Username.of(usernameValue);

            // Then
            assertNotNull(username);
            assertEquals(usernameValue, username.getValue());
        }

        @Test
        @DisplayName("Should trim whitespace from username")
        void shouldTrimWhitespaceFromUsername() {
            // Given
            String usernameWithSpaces = "  testuser  ";

            // When
            Username username = new Username(usernameWithSpaces);

            // Then
            assertEquals("testuser", username.getValue());
        }
    }

    @Nested
    @DisplayName("Invalid Username Creation Tests")
    class InvalidUsernameCreationTests {

        @Test
        @DisplayName("Should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            // When & Then
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class, 
                () -> new Username(null)
            );
            assertEquals("Username cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when username is empty")
        void shouldThrowExceptionWhenUsernameIsEmpty() {
            // When & Then
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class, 
                () -> new Username("")
            );
            assertEquals("Username cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when username is only whitespace")
        void shouldThrowExceptionWhenUsernameIsOnlyWhitespace() {
            // When & Then
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class, 
                () -> new Username("   ")
            );
            assertEquals("Username cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when username is too short")
        void shouldThrowExceptionWhenUsernameIsTooShort() {
            // Given
            String shortUsername = "ab"; // 2 characters, minimum is 3

            // When & Then
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class, 
                () -> new Username(shortUsername)
            );
            assertEquals("Username must be between 3 and 100 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when username is too long")
        void shouldThrowExceptionWhenUsernameIsTooLong() {
            // Given
            String longUsername = "a".repeat(101); // 101 characters, maximum is 100

            // When & Then
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class, 
                () -> new Username(longUsername)
            );
            assertEquals("Username must be between 3 and 100 characters", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"user name", "user@test", "user#123", "user!", "user$", "user%", "user&", "user*"})
        @DisplayName("Should throw exception when username contains invalid characters")
        void shouldThrowExceptionWhenUsernameContainsInvalidCharacters(String invalidUsername) {
            // When & Then
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class, 
                () -> new Username(invalidUsername)
            );
            assertEquals("Username can only contain letters, numbers, underscores, and hyphens", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when username values are same")
        void shouldBeEqualWhenUsernameValuesAreSame() {
            // Given
            String usernameValue = "testuser";
            Username username1 = new Username(usernameValue);
            Username username2 = new Username(usernameValue);

            // When & Then
            assertEquals(username1, username2);
            assertEquals(username1.hashCode(), username2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when username values are different")
        void shouldNotBeEqualWhenUsernameValuesAreDifferent() {
            // Given
            Username username1 = new Username("user1");
            Username username2 = new Username("user2");

            // When & Then
            assertNotEquals(username1, username2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            Username username = new Username("testuser");

            // When & Then
            assertNotEquals(username, null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            Username username = new Username("testuser");
            String string = "testuser";

            // When & Then
            assertNotEquals(username, string);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            Username username = new Username("testuser");

            // When & Then
            assertEquals(username, username);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should return username value as string")
        void shouldReturnUsernameValueAsString() {
            // Given
            String usernameValue = "testuser";
            Username username = new Username(usernameValue);

            // When
            String result = username.toString();

            // Then
            assertEquals(usernameValue, result);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle minimum length username")
        void shouldHandleMinimumLengthUsername() {
            // Given
            String minLengthUsername = "abc"; // 3 characters

            // When
            Username username = new Username(minLengthUsername);

            // Then
            assertEquals(minLengthUsername, username.getValue());
        }

        @Test
        @DisplayName("Should handle maximum length username")
        void shouldHandleMaximumLengthUsername() {
            // Given
            String maxLengthUsername = "a".repeat(100); // 100 characters

            // When
            Username username = new Username(maxLengthUsername);

            // Then
            assertEquals(maxLengthUsername, username.getValue());
        }

        @Test
        @DisplayName("Should handle username with all valid special characters")
        void shouldHandleUsernameWithAllValidSpecialCharacters() {
            // Given
            String usernameWithSpecialChars = "user_name-123";

            // When
            Username username = new Username(usernameWithSpecialChars);

            // Then
            assertEquals(usernameWithSpecialChars, username.getValue());
        }
    }
}
