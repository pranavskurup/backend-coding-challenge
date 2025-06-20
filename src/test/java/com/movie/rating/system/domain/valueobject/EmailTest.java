package com.movie.rating.system.domain.valueobject;

import com.movie.rating.system.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email Value Object Tests")
class EmailTest {

    @Nested
    @DisplayName("Valid Email Creation Tests")
    class ValidEmailCreationTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user123@example123.com",
            "user_name@example-domain.com",
            "user@subdomain.example.com",
            "a@b.co",
            "test.email.with+symbol@example.co.uk"
        })
        @DisplayName("Should create email with valid formats")
        void shouldCreateEmailWithValidFormats(String validEmail) {
            // When
            Email email = new Email(validEmail);

            // Then
            assertNotNull(email);
            assertEquals(validEmail.toLowerCase().trim(), email.getValue());
        }

        @Test
        @DisplayName("Should create email using of() method")
        void shouldCreateEmailUsingOfMethod() {
            // Given
            String emailValue = "test@example.com";

            // When
            Email email = Email.of(emailValue);

            // Then
            assertNotNull(email);
            assertEquals(emailValue.toLowerCase(), email.getValue());
        }

        @Test
        @DisplayName("Should convert email to lowercase")
        void shouldConvertEmailToLowercase() {
            // Given
            String uppercaseEmail = "TEST@EXAMPLE.COM";

            // When
            Email email = new Email(uppercaseEmail);

            // Then
            assertEquals("test@example.com", email.getValue());
        }

        @Test
        @DisplayName("Should trim whitespace from email")
        void shouldTrimWhitespaceFromEmail() {
            // Given
            String emailWithSpaces = "  test@example.com  ";

            // When
            Email email = new Email(emailWithSpaces);

            // Then
            assertEquals("test@example.com", email.getValue());
        }
    }

    @Nested
    @DisplayName("Invalid Email Creation Tests")
    class InvalidEmailCreationTests {

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // When & Then
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class, 
                () -> new Email(null)
            );
            assertEquals("Email cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email is empty")
        void shouldThrowExceptionWhenEmailIsEmpty() {
            // When & Then
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class, 
                () -> new Email("")
            );
            assertEquals("Email cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email is only whitespace")
        void shouldThrowExceptionWhenEmailIsOnlyWhitespace() {
            // When & Then
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class, 
                () -> new Email("   ")
            );
            assertEquals("Email cannot be null or empty", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid",
            "@example.com",
            "test@",
            "test..test@example.com",
            "test@example",
            "test@.com",
            "test@example.",
            "test space@example.com",
            "test@example .com",
            "test@@example.com",
            "test@example..com",
            ".test@example.com",
            "test.@example.com"
        })
        @DisplayName("Should throw exception for invalid email formats")
        void shouldThrowExceptionForInvalidEmailFormats(String invalidEmail) {
            // When & Then
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class, 
                () -> new Email(invalidEmail)
            );
            assertTrue(exception.getMessage().startsWith("Invalid email format:"));
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when email values are same")
        void shouldBeEqualWhenEmailValuesAreSame() {
            // Given
            String emailValue = "test@example.com";
            Email email1 = new Email(emailValue);
            Email email2 = new Email(emailValue);

            // When & Then
            assertEquals(email1, email2);
            assertEquals(email1.hashCode(), email2.hashCode());
        }

        @Test
        @DisplayName("Should be equal when emails differ only in case")
        void shouldBeEqualWhenEmailsDifferOnlyInCase() {
            // Given
            Email email1 = new Email("test@example.com");
            Email email2 = new Email("TEST@EXAMPLE.COM");

            // When & Then
            assertEquals(email1, email2);
            assertEquals(email1.hashCode(), email2.hashCode());
        }

        @Test
        @DisplayName("Should be equal when emails differ only in whitespace")
        void shouldBeEqualWhenEmailsDifferOnlyInWhitespace() {
            // Given
            Email email1 = new Email("test@example.com");
            Email email2 = new Email("  test@example.com  ");

            // When & Then
            assertEquals(email1, email2);
            assertEquals(email1.hashCode(), email2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when email values are different")
        void shouldNotBeEqualWhenEmailValuesAreDifferent() {
            // Given
            Email email1 = new Email("test1@example.com");
            Email email2 = new Email("test2@example.com");

            // When & Then
            assertNotEquals(email1, email2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            Email email = new Email("test@example.com");

            // When & Then
            assertNotEquals(email, null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            Email email = new Email("test@example.com");
            String string = "test@example.com";

            // When & Then
            assertNotEquals(email, string);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            Email email = new Email("test@example.com");

            // When & Then
            assertEquals(email, email);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should return email value as string")
        void shouldReturnEmailValueAsString() {
            // Given
            String emailValue = "test@example.com";
            Email email = new Email(emailValue);

            // When
            String result = email.toString();

            // Then
            assertEquals(emailValue, result);
        }

        @Test
        @DisplayName("Should return lowercase email as string")
        void shouldReturnLowercaseEmailAsString() {
            // Given
            String uppercaseEmail = "TEST@EXAMPLE.COM";
            Email email = new Email(uppercaseEmail);

            // When
            String result = email.toString();

            // Then
            assertEquals("test@example.com", result);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle minimum valid email")
        void shouldHandleMinimumValidEmail() {
            // Given
            String minEmail = "a@b.co";

            // When
            Email email = new Email(minEmail);

            // Then
            assertEquals(minEmail, email.getValue());
        }

        @Test
        @DisplayName("Should handle complex valid email")
        void shouldHandleComplexValidEmail() {
            // Given
            String complexEmail = "user.name+tag123@subdomain.example-domain.co.uk";

            // When
            Email email = new Email(complexEmail);

            // Then
            assertEquals(complexEmail, email.getValue());
        }
    }
}
