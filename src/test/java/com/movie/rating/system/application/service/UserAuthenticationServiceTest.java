package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.exception.AuthenticationFailedException;
import com.movie.rating.system.domain.exception.UserAccountInactiveException;
import com.movie.rating.system.domain.exception.UserNotFoundException;
import com.movie.rating.system.domain.port.inbound.UserAuthenticationUseCase.AuthenticationCommand;
import com.movie.rating.system.domain.port.outbound.PasswordHashingService;
import com.movie.rating.system.domain.port.outbound.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserAuthenticationService.
 * Tests user authentication, credential validation, and last login updates.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Authentication Service Tests")
class UserAuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    private UserAuthenticationService userAuthenticationService;

    private final UUID testUserId = UUID.randomUUID();
    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";
    private final String testPasswordHash = "$2a$10$hashedPassword";

    @BeforeEach
    void setUp() {
        userAuthenticationService = new UserAuthenticationService(userRepository, passwordHashingService);
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should authenticate user successfully with username")
        void shouldAuthenticateUserSuccessfullyWithUsername() {
            // Given
            User activeUser = createActiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testUsername, testPassword);
            
            when(userRepository.findByUsername(testUsername)).thenReturn(Mono.just(activeUser));
            when(passwordHashingService.verifyPassword(testPassword, testPasswordHash)).thenReturn(true);

            // When & Then
            StepVerifier.create(userAuthenticationService.authenticate(command))
                    .assertNext(user -> {
                        assertThat(user.getId()).isEqualTo(testUserId);
                        assertThat(user.getUsername()).isEqualTo(testUsername);
                        assertThat(user.getEmail()).isEqualTo(testEmail);
                        assertThat(user.isActive()).isTrue();
                    })
                    .verifyComplete();

            verify(userRepository).findByUsername(testUsername);
            verify(passwordHashingService).verifyPassword(testPassword, testPasswordHash);
        }

        @Test
        @DisplayName("Should authenticate user successfully with email")
        void shouldAuthenticateUserSuccessfullyWithEmail() {
            // Given
            User activeUser = createActiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testEmail, testPassword);
            
            when(userRepository.findByEmail(testEmail)).thenReturn(Mono.just(activeUser));
            when(passwordHashingService.verifyPassword(testPassword, testPasswordHash)).thenReturn(true);

            // When & Then
            StepVerifier.create(userAuthenticationService.authenticate(command))
                    .assertNext(user -> {
                        assertThat(user.getId()).isEqualTo(testUserId);
                        assertThat(user.getUsername()).isEqualTo(testUsername);
                        assertThat(user.getEmail()).isEqualTo(testEmail);
                    })
                    .verifyComplete();

            verify(userRepository).findByEmail(testEmail);
            verify(passwordHashingService).verifyPassword(testPassword, testPasswordHash);
        }

        @Test
        @DisplayName("Should fail authentication when user not found by username")
        void shouldFailAuthenticationWhenUserNotFoundByUsername() {
            // Given
            AuthenticationCommand command = new AuthenticationCommand("nonexistent", testPassword);
            when(userRepository.findByUsername("nonexistent")).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(userAuthenticationService.authenticate(command))
                    .expectErrorMatches(error -> 
                        error instanceof UserNotFoundException &&
                        error.getMessage().contains("User not found with username: nonexistent"))
                    .verify();

            verify(userRepository).findByUsername("nonexistent");
            verify(passwordHashingService, never()).verifyPassword(anyString(), anyString());
        }

        @Test
        @DisplayName("Should fail authentication when user not found by email")
        void shouldFailAuthenticationWhenUserNotFoundByEmail() {
            // Given
            AuthenticationCommand command = new AuthenticationCommand("nonexistent@example.com", testPassword);
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(userAuthenticationService.authenticate(command))
                    .expectErrorMatches(error -> 
                        error instanceof UserNotFoundException &&
                        error.getMessage().contains("User not found with email: nonexistent@example.com"))
                    .verify();

            verify(userRepository).findByEmail("nonexistent@example.com");
            verify(passwordHashingService, never()).verifyPassword(anyString(), anyString());
        }

        @Test
        @DisplayName("Should fail authentication when user account is inactive")
        void shouldFailAuthenticationWhenUserAccountIsInactive() {
            // Given
            User inactiveUser = createInactiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testUsername, testPassword);
            
            when(userRepository.findByUsername(testUsername)).thenReturn(Mono.just(inactiveUser));

            // When & Then
            StepVerifier.create(userAuthenticationService.authenticate(command))
                    .expectErrorMatches(error -> 
                        error instanceof UserAccountInactiveException &&
                        error.getMessage().contains("User account is deactivated: " + testUsername))
                    .verify();

            verify(userRepository).findByUsername(testUsername);
            verify(passwordHashingService, never()).verifyPassword(anyString(), anyString());
        }

        @Test
        @DisplayName("Should fail authentication when password is incorrect")
        void shouldFailAuthenticationWhenPasswordIsIncorrect() {
            // Given
            User activeUser = createActiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testUsername, "wrongpassword");
            
            when(userRepository.findByUsername(testUsername)).thenReturn(Mono.just(activeUser));
            when(passwordHashingService.verifyPassword("wrongpassword", testPasswordHash)).thenReturn(false);

            // When & Then
            StepVerifier.create(userAuthenticationService.authenticate(command))
                    .expectErrorMatches(error -> 
                        error instanceof AuthenticationFailedException &&
                        error.getMessage().contains("Invalid username/email or password"))
                    .verify();

            verify(userRepository).findByUsername(testUsername);
            verify(passwordHashingService).verifyPassword("wrongpassword", testPasswordHash);
        }

        @Test
        @DisplayName("Should handle repository error during authentication")
        void shouldHandleRepositoryErrorDuringAuthentication() {
            // Given
            AuthenticationCommand command = new AuthenticationCommand(testUsername, testPassword);
            when(userRepository.findByUsername(testUsername))
                    .thenReturn(Mono.error(new RuntimeException("Database error")));

            // When & Then
            StepVerifier.create(userAuthenticationService.authenticate(command))
                    .expectError(RuntimeException.class)
                    .verify();

            verify(passwordHashingService, never()).verifyPassword(anyString(), anyString());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should handle invalid username/email in command")
        void shouldHandleInvalidUsernameEmailInCommand(String invalidUsernameOrEmail) {
            // When & Then
            StepVerifier.create(Mono.fromCallable(() -> 
                    new AuthenticationCommand(invalidUsernameOrEmail, testPassword)))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should handle invalid password in command")
        void shouldHandleInvalidPasswordInCommand(String invalidPassword) {
            // When & Then
            StepVerifier.create(Mono.fromCallable(() -> 
                    new AuthenticationCommand(testUsername, invalidPassword)))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Credential Validation Tests")
    class CredentialValidationTests {

        @Test
        @DisplayName("Should validate credentials successfully for active user with username")
        void shouldValidateCredentialsSuccessfullyForActiveUserWithUsername() {
            // Given
            User activeUser = createActiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testUsername, testPassword);
            
            when(userRepository.findByUsername(testUsername)).thenReturn(Mono.just(activeUser));
            when(passwordHashingService.verifyPassword(testPassword, testPasswordHash)).thenReturn(true);

            // When & Then
            StepVerifier.create(userAuthenticationService.validateCredentials(command))
                    .expectNext(true)
                    .verifyComplete();

            verify(userRepository).findByUsername(testUsername);
            verify(passwordHashingService).verifyPassword(testPassword, testPasswordHash);
        }

        @Test
        @DisplayName("Should validate credentials successfully for active user with email")
        void shouldValidateCredentialsSuccessfullyForActiveUserWithEmail() {
            // Given
            User activeUser = createActiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testEmail, testPassword);
            
            when(userRepository.findByEmail(testEmail)).thenReturn(Mono.just(activeUser));
            when(passwordHashingService.verifyPassword(testPassword, testPasswordHash)).thenReturn(true);

            // When & Then
            StepVerifier.create(userAuthenticationService.validateCredentials(command))
                    .expectNext(true)
                    .verifyComplete();

            verify(userRepository).findByEmail(testEmail);
            verify(passwordHashingService).verifyPassword(testPassword, testPasswordHash);
        }

        @Test
        @DisplayName("Should return false when validating credentials for inactive user")
        void shouldReturnFalseWhenValidatingCredentialsForInactiveUser() {
            // Given
            User inactiveUser = createInactiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testUsername, testPassword);
            
            when(userRepository.findByUsername(testUsername)).thenReturn(Mono.just(inactiveUser));

            // When & Then
            StepVerifier.create(userAuthenticationService.validateCredentials(command))
                    .expectNext(false)
                    .verifyComplete();

            verify(userRepository).findByUsername(testUsername);
            verify(passwordHashingService, never()).verifyPassword(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return false when password is incorrect")
        void shouldReturnFalseWhenPasswordIsIncorrect() {
            // Given
            User activeUser = createActiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testUsername, "wrongpassword");
            
            when(userRepository.findByUsername(testUsername)).thenReturn(Mono.just(activeUser));
            when(passwordHashingService.verifyPassword("wrongpassword", testPasswordHash)).thenReturn(false);

            // When & Then
            StepVerifier.create(userAuthenticationService.validateCredentials(command))
                    .expectNext(false)
                    .verifyComplete();

            verify(userRepository).findByUsername(testUsername);
            verify(passwordHashingService).verifyPassword("wrongpassword", testPasswordHash);
        }

        @Test
        @DisplayName("Should return false when user not found")
        void shouldReturnFalseWhenUserNotFound() {
            // Given
            AuthenticationCommand command = new AuthenticationCommand("nonexistent", testPassword);
            when(userRepository.findByUsername("nonexistent")).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(userAuthenticationService.validateCredentials(command))
                    .expectNext(false)
                    .verifyComplete();

            verify(userRepository).findByUsername("nonexistent");
            verify(passwordHashingService, never()).verifyPassword(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return false when repository error occurs")
        void shouldReturnFalseWhenRepositoryErrorOccurs() {
            // Given
            AuthenticationCommand command = new AuthenticationCommand(testUsername, testPassword);
            when(userRepository.findByUsername(testUsername))
                    .thenReturn(Mono.error(new RuntimeException("Database error")));

            // When & Then
            StepVerifier.create(userAuthenticationService.validateCredentials(command))
                    .expectNext(false)
                    .verifyComplete();

            verify(passwordHashingService, never()).verifyPassword(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return false when password hashing service throws exception")
        void shouldReturnFalseWhenPasswordHashingServiceThrowsException() {
            // Given
            User activeUser = createActiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testUsername, testPassword);
            
            when(userRepository.findByUsername(testUsername)).thenReturn(Mono.just(activeUser));
            when(passwordHashingService.verifyPassword(testPassword, testPasswordHash))
                    .thenThrow(new RuntimeException("Hashing error"));

            // When & Then
            StepVerifier.create(userAuthenticationService.validateCredentials(command))
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Last Login Update Tests")
    class LastLoginUpdateTests {

        @Test
        @DisplayName("Should update last login successfully")
        void shouldUpdateLastLoginSuccessfully() {
            // Given
            User existingUser = createActiveUser();
            User updatedUser = existingUser.toBuilder()
                    .updatedAt(Instant.now())
                    .build();
            
            when(userRepository.findById(testUserId)).thenReturn(Mono.just(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

            // When & Then
            StepVerifier.create(userAuthenticationService.updateLastLogin(testUserId))
                    .verifyComplete();

            verify(userRepository).findById(testUserId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should fail to update last login when user not found")
        void shouldFailToUpdateLastLoginWhenUserNotFound() {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();
            when(userRepository.findById(nonExistentUserId)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(userAuthenticationService.updateLastLogin(nonExistentUserId))
                    .expectErrorMatches(error -> 
                        error instanceof UserNotFoundException &&
                        error.getMessage().contains(nonExistentUserId.toString()))
                    .verify();

            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle repository find error during last login update")
        void shouldHandleRepositoryFindErrorDuringLastLoginUpdate() {
            // Given
            when(userRepository.findById(testUserId))
                    .thenReturn(Mono.error(new RuntimeException("Database error")));

            // When & Then
            StepVerifier.create(userAuthenticationService.updateLastLogin(testUserId))
                    .expectError(RuntimeException.class)
                    .verify();

            verify(userRepository).findById(testUserId);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle repository save error during last login update")
        void shouldHandleRepositorySaveErrorDuringLastLoginUpdate() {
            // Given
            User existingUser = createActiveUser();
            
            when(userRepository.findById(testUserId)).thenReturn(Mono.just(existingUser));
            when(userRepository.save(any(User.class)))
                    .thenReturn(Mono.error(new RuntimeException("Save error")));

            // When & Then
            StepVerifier.create(userAuthenticationService.updateLastLogin(testUserId))
                    .expectError(RuntimeException.class)
                    .verify();

            verify(userRepository).findById(testUserId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should update last login for inactive user")
        void shouldUpdateLastLoginForInactiveUser() {
            // Given - Even inactive users can have their last login updated
            User inactiveUser = createInactiveUser();
            User updatedUser = inactiveUser.toBuilder()
                    .updatedAt(Instant.now())
                    .build();
            
            when(userRepository.findById(testUserId)).thenReturn(Mono.just(inactiveUser));
            when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

            // When & Then
            StepVerifier.create(userAuthenticationService.updateLastLogin(testUserId))
                    .verifyComplete();

            verify(userRepository).findById(testUserId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should verify updated timestamp is recent")
        void shouldVerifyUpdatedTimestampIsRecent() {
            // Given
            User existingUser = createActiveUser();
            Instant beforeUpdate = Instant.now().minusSeconds(1);
            
            when(userRepository.findById(testUserId)).thenReturn(Mono.just(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                assertThat(savedUser.getUpdatedAt()).isAfter(beforeUpdate);
                assertThat(savedUser.getUpdatedAt()).isBeforeOrEqualTo(Instant.now());
                return Mono.just(savedUser);
            });

            // When & Then
            StepVerifier.create(userAuthenticationService.updateLastLogin(testUserId))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Username/Email Detection Tests")
    class UsernameEmailDetectionTests {

        @Test
        @DisplayName("Should detect email and use findByEmail")
        void shouldDetectEmailAndUseFindByEmail() {
            // Given
            String[] emailInputs = {
                "user@example.com",
                "test.user+tag@example.com",
                "user123@domain.co.uk",
                "simple@test.org"
            };

            User activeUser = createActiveUser();
            when(passwordHashingService.verifyPassword(testPassword, testPasswordHash)).thenReturn(true);

            for (String email : emailInputs) {
                when(userRepository.findByEmail(email)).thenReturn(Mono.just(activeUser));
                
                AuthenticationCommand command = new AuthenticationCommand(email, testPassword);
                
                // When & Then
                StepVerifier.create(userAuthenticationService.authenticate(command))
                        .expectNextCount(1)
                        .verifyComplete();
                
                verify(userRepository).findByEmail(email);
                verify(userRepository, never()).findByUsername(email);
            }
        }

        @Test
        @DisplayName("Should detect username and use findByUsername")
        void shouldDetectUsernameAndUseFindByUsername() {
            // Given
            String[] usernameInputs = {
                "username",
                "user123",
                "test_user",
                "user-name",
                "123456"
            };

            User activeUser = createActiveUser();
            when(passwordHashingService.verifyPassword(testPassword, testPasswordHash)).thenReturn(true);

            for (String username : usernameInputs) {
                when(userRepository.findByUsername(username)).thenReturn(Mono.just(activeUser));
                
                AuthenticationCommand command = new AuthenticationCommand(username, testPassword);
                
                // When & Then
                StepVerifier.create(userAuthenticationService.authenticate(command))
                        .expectNextCount(1)
                        .verifyComplete();
                
                verify(userRepository).findByUsername(username);
                verify(userRepository, never()).findByEmail(username);
            }
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete authentication flow")
        void shouldHandleCompleteAuthenticationFlow() {
            // Given
            User activeUser = createActiveUser();
            AuthenticationCommand command = new AuthenticationCommand(testEmail, testPassword);
            
            when(userRepository.findByEmail(testEmail)).thenReturn(Mono.just(activeUser));
            when(passwordHashingService.verifyPassword(testPassword, testPasswordHash)).thenReturn(true);
            when(userRepository.findById(testUserId)).thenReturn(Mono.just(activeUser));
            when(userRepository.save(any(User.class))).thenReturn(Mono.just(activeUser));

            // When & Then - Authenticate
            StepVerifier.create(userAuthenticationService.authenticate(command))
                    .assertNext(user -> assertThat(user.getId()).isEqualTo(testUserId))
                    .verifyComplete();

            // When & Then - Validate credentials
            StepVerifier.create(userAuthenticationService.validateCredentials(command))
                    .expectNext(true)
                    .verifyComplete();

            // When & Then - Update last login
            StepVerifier.create(userAuthenticationService.updateLastLogin(testUserId))
                    .verifyComplete();

            // Verify interactions
            verify(userRepository, times(2)).findByEmail(testEmail);
            verify(passwordHashingService, times(2)).verifyPassword(testPassword, testPasswordHash);
            verify(userRepository).findById(testUserId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle mixed success and failure scenarios")
        void shouldHandleMixedSuccessAndFailureScenarios() {
            // Given
            User activeUser = createActiveUser();
            User inactiveUser = createInactiveUser();
            
            // Setup different scenarios
            when(userRepository.findByUsername("activeuser")).thenReturn(Mono.just(activeUser));
            when(userRepository.findByUsername("inactiveuser")).thenReturn(Mono.just(inactiveUser));
            when(userRepository.findByUsername("nonexistent")).thenReturn(Mono.empty());
            when(passwordHashingService.verifyPassword(testPassword, testPasswordHash)).thenReturn(true);
            when(passwordHashingService.verifyPassword("wrongpassword", testPasswordHash)).thenReturn(false);

            // Test 1: Successful authentication
            StepVerifier.create(userAuthenticationService.authenticate(
                    new AuthenticationCommand("activeuser", testPassword)))
                    .expectNextCount(1)
                    .verifyComplete();

            // Test 2: Failed authentication - inactive user
            StepVerifier.create(userAuthenticationService.authenticate(
                    new AuthenticationCommand("inactiveuser", testPassword)))
                    .expectError(UserAccountInactiveException.class)
                    .verify();

            // Test 3: Failed authentication - user not found
            StepVerifier.create(userAuthenticationService.authenticate(
                    new AuthenticationCommand("nonexistent", testPassword)))
                    .expectError(UserNotFoundException.class)
                    .verify();

            // Test 4: Failed authentication - wrong password
            StepVerifier.create(userAuthenticationService.authenticate(
                    new AuthenticationCommand("activeuser", "wrongpassword")))
                    .expectError(AuthenticationFailedException.class)
                    .verify();
        }
    }

    // Helper methods
    private User createActiveUser() {
        return User.builder()
                .id(testUserId)
                .username(testUsername)
                .email(testEmail)
                .passwordHash(testPasswordHash)
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .createdAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
    }

    private User createInactiveUser() {
        return User.builder()
                .id(testUserId)
                .username(testUsername)
                .email(testEmail)
                .passwordHash(testPasswordHash)
                .firstName("Test")
                .lastName("User")
                .isActive(false)
                .createdAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .deactivatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
    }
}
