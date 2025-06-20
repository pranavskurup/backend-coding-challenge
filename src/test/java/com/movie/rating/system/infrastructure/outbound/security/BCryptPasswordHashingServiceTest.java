package com.movie.rating.system.infrastructure.outbound.security;

import com.movie.rating.system.domain.port.outbound.PasswordHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for BCryptPasswordHashingService.
 * Tests password hashing, verification, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BCrypt Password Hashing Service Tests")
class BCryptPasswordHashingServiceTest {

    private PasswordHashingService passwordHashingService;

    @BeforeEach
    void setUp() {
        passwordHashingService = new BCryptPasswordHashingService();
    }

    @Test
    @DisplayName("Should hash password successfully")
    void shouldHashPasswordSuccessfully() {
        // Given
        String plainTextPassword = "testPassword123!";

        // When
        String hashedPassword = passwordHashingService.hashPassword(plainTextPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(hashedPassword).isNotEmpty();
        assertThat(hashedPassword).isNotEqualTo(plainTextPassword);
        assertThat(hashedPassword).startsWith("$2a$12$"); // BCrypt format with strength 12
        assertThat(hashedPassword).hasSize(60); // BCrypt hash is always 60 characters
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    void shouldGenerateDifferentHashesForSamePassword() {
        // Given
        String plainTextPassword = "samePassword123!";

        // When
        String hash1 = passwordHashingService.hashPassword(plainTextPassword);
        String hash2 = passwordHashingService.hashPassword(plainTextPassword);

        // Then
        assertThat(hash1).isNotEqualTo(hash2); // BCrypt uses salt, so hashes should be different
        assertThat(passwordHashingService.verifyPassword(plainTextPassword, hash1)).isTrue();
        assertThat(passwordHashingService.verifyPassword(plainTextPassword, hash2)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should throw exception when hashing null or empty password")
    void shouldThrowExceptionWhenHashingNullOrEmptyPassword(String invalidPassword) {
        // When & Then
        assertThatThrownBy(() -> passwordHashingService.hashPassword(invalidPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty");
    }

    @Test
    @DisplayName("Should verify correct password successfully")
    void shouldVerifyCorrectPasswordSuccessfully() {
        // Given
        String plainTextPassword = "correctPassword123!";
        String hashedPassword = passwordHashingService.hashPassword(plainTextPassword);

        // When
        boolean isValid = passwordHashingService.verifyPassword(plainTextPassword, hashedPassword);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void shouldRejectIncorrectPassword() {
        // Given
        String correctPassword = "correctPassword123!";
        String incorrectPassword = "wrongPassword456!";
        String hashedPassword = passwordHashingService.hashPassword(correctPassword);

        // When
        boolean isValid = passwordHashingService.verifyPassword(incorrectPassword, hashedPassword);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should handle case-sensitive password verification")
    void shouldHandleCaseSensitivePasswordVerification() {
        // Given
        String originalPassword = "CaseSensitivePassword123!";
        String differentCasePassword = "casesensitivepassword123!";
        String hashedPassword = passwordHashingService.hashPassword(originalPassword);

        // When
        boolean originalIsValid = passwordHashingService.verifyPassword(originalPassword, hashedPassword);
        boolean differentCaseIsValid = passwordHashingService.verifyPassword(differentCasePassword, hashedPassword);

        // Then
        assertThat(originalIsValid).isTrue();
        assertThat(differentCaseIsValid).isFalse(); // Should be case-sensitive
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should throw exception when verifying with null or empty plain text password")
    void shouldThrowExceptionWhenVerifyingWithNullOrEmptyPlainTextPassword(String invalidPassword) {
        // Given
        String validHashedPassword = passwordHashingService.hashPassword("validPassword123!");

        // When & Then
        assertThatThrownBy(() -> passwordHashingService.verifyPassword(invalidPassword, validHashedPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Plain text password cannot be null or empty");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should throw exception when verifying with null or empty hashed password")
    void shouldThrowExceptionWhenVerifyingWithNullOrEmptyHashedPassword(String invalidHashedPassword) {
        // Given
        String validPlainTextPassword = "validPassword123!";

        // When & Then
        assertThatThrownBy(() -> passwordHashingService.verifyPassword(validPlainTextPassword, invalidHashedPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hashed password cannot be null or empty");
    }

    @Test
    @DisplayName("Should handle various password lengths")
    void shouldHandleVariousPasswordLengths() {
        // Given
        String shortPassword = "short";
        String mediumPassword = "mediumLengthPassword123!";
        String longPassword = "thisIsALongPasswordWithManyCharacters123!@#$%^&*()_+{}[]"; // 57 bytes - under limit

        // When
        String hashedShort = passwordHashingService.hashPassword(shortPassword);
        String hashedMedium = passwordHashingService.hashPassword(mediumPassword);
        String hashedLong = passwordHashingService.hashPassword(longPassword);

        // Then
        assertThat(passwordHashingService.verifyPassword(shortPassword, hashedShort)).isTrue();
        assertThat(passwordHashingService.verifyPassword(mediumPassword, hashedMedium)).isTrue();
        assertThat(passwordHashingService.verifyPassword(longPassword, hashedLong)).isTrue();

        // All hashes should have the same length (60 characters)
        assertThat(hashedShort).hasSize(60);
        assertThat(hashedMedium).hasSize(60);
        assertThat(hashedLong).hasSize(60);
    }

    @Test
    @DisplayName("Should handle special characters in passwords")
    void shouldHandleSpecialCharactersInPasswords() {
        // Given
        String passwordWithSpecialChars = "p@ssw0rd!#$%^&*(){}[]|\\:;\"'<>,.?/~`";

        // When
        String hashedPassword = passwordHashingService.hashPassword(passwordWithSpecialChars);

        // Then
        assertThat(passwordHashingService.verifyPassword(passwordWithSpecialChars, hashedPassword)).isTrue();
        assertThat(passwordHashingService.verifyPassword("differentPassword", hashedPassword)).isFalse();
    }

    @Test
    @DisplayName("Should handle Unicode characters in passwords")
    void shouldHandleUnicodeCharactersInPasswords() {
        // Given
        String unicodePassword = "пароль测试パスワード🔒";

        // When
        String hashedPassword = passwordHashingService.hashPassword(unicodePassword);

        // Then
        assertThat(passwordHashingService.verifyPassword(unicodePassword, hashedPassword)).isTrue();
        assertThat(passwordHashingService.verifyPassword("differentPassword", hashedPassword)).isFalse();
    }

    @Test
    @DisplayName("Should reject verification with invalid hash format")
    void shouldRejectVerificationWithInvalidHashFormat() {
        // Given
        String plainTextPassword = "validPassword123!";
        String invalidHash = "invalidHashFormat";

        // When
        boolean isValid = passwordHashingService.verifyPassword(plainTextPassword, invalidHash);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should handle password with only whitespace after trimming")
    void shouldHandlePasswordWithOnlyWhitespaceAfterTrimming() {
        // Given
        String whitespacePassword = "  password123!  ";

        // When
        String hashedPassword = passwordHashingService.hashPassword(whitespacePassword);

        // Then
        // Should hash the password as-is (including leading/trailing spaces)
        assertThat(passwordHashingService.verifyPassword(whitespacePassword, hashedPassword)).isTrue();
        assertThat(passwordHashingService.verifyPassword("password123!", hashedPassword)).isFalse();
        assertThat(passwordHashingService.verifyPassword(whitespacePassword.trim(), hashedPassword)).isFalse();
    }

    @Test
    @DisplayName("Should be consistent across multiple instances")
    void shouldBeConsistentAcrossMultipleInstances() {
        // Given
        String password = "consistencyTest123!";
        PasswordHashingService service1 = new BCryptPasswordHashingService();
        PasswordHashingService service2 = new BCryptPasswordHashingService();

        // When
        String hash1 = service1.hashPassword(password);
        String hash2 = service2.hashPassword(password);

        // Then
        // Hashes should be different (due to salt) but both should verify correctly
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(service1.verifyPassword(password, hash1)).isTrue();
        assertThat(service1.verifyPassword(password, hash2)).isTrue();
        assertThat(service2.verifyPassword(password, hash1)).isTrue();
        assertThat(service2.verifyPassword(password, hash2)).isTrue();
    }

    @Test
    @DisplayName("Should handle concurrent password operations")
    void shouldHandleConcurrentPasswordOperations() {
        // Given
        String password = "concurrentTest123!";
        int numberOfThreads = 10;
        String[] hashes = new String[numberOfThreads];
        Thread[] threads = new Thread[numberOfThreads];

        // When - Create multiple threads that hash the same password
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                hashes[index] = passwordHashingService.hashPassword(password);
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        }

        // Then - All hashes should be valid and different
        for (String hash : hashes) {
            assertThat(hash).isNotNull();
            assertThat(passwordHashingService.verifyPassword(password, hash)).isTrue();
        }

        // All hashes should be unique (due to salt)
        for (int i = 0; i < numberOfThreads; i++) {
            for (int j = i + 1; j < numberOfThreads; j++) {
                assertThat(hashes[i]).isNotEqualTo(hashes[j]);
            }
        }
    }

    @Test
    @DisplayName("Should throw exception when password exceeds 72 bytes")
    void shouldThrowExceptionWhenPasswordExceeds72Bytes() {
        // Given - Create a password that exceeds 72 bytes
        String tooLongPassword = "a".repeat(73); // 73 bytes - exceeds BCrypt limit

        // When & Then
        assertThatThrownBy(() -> passwordHashingService.hashPassword(tooLongPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be more than 72 bytes");
    }

    @Test
    @DisplayName("Should handle password at exactly 72 bytes")
    void shouldHandlePasswordAtExactly72Bytes() {
        // Given - Create a password that is exactly 72 bytes
        String maxLengthPassword = "a".repeat(72); // 72 bytes - at BCrypt limit

        // When
        String hashedPassword = passwordHashingService.hashPassword(maxLengthPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(passwordHashingService.verifyPassword(maxLengthPassword, hashedPassword)).isTrue();
    }
}
