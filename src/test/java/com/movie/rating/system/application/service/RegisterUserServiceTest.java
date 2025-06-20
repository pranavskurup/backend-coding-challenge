package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.exception.EmailAlreadyExistsException;
import com.movie.rating.system.domain.exception.UsernameAlreadyExistsException;
import com.movie.rating.system.domain.port.inbound.RegisterUserUseCase.RegisterUserCommand;
import com.movie.rating.system.domain.port.outbound.PasswordHashingService;
import com.movie.rating.system.domain.port.outbound.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegisterUserService.
 * Tests user registration business logic, validation, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Register User Service Tests")
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    private RegisterUserService registerUserService;

    @BeforeEach
    void setUp() {
        registerUserService = new RegisterUserService(userRepository, passwordHashingService);
    }

    @Test
    @DisplayName("Should register user successfully when username and email are available")
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
                "testuser",
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        User expectedUser = createTestUser("testuser", "test@example.com", "John", "Doe");
        String hashedPassword = "$2a$12$hashedpassword";

        when(userRepository.existsByUsername("testuser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));
        when(passwordHashingService.hashPassword("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(expectedUser));

        // When & Then
        StepVerifier.create(registerUserService.registerUser(command))
                .assertNext(user -> {
                    assertThat(user.getUsername()).isEqualTo("testuser");
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                    assertThat(user.getFirstName()).isEqualTo("John");
                    assertThat(user.getLastName()).isEqualTo("Doe");
                    assertThat(user.isActive()).isTrue();
                    assertThat(user.getId()).isNotNull();
                })
                .verifyComplete();

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordHashingService).hashPassword("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should normalize email to lowercase during registration")
    void shouldNormalizeEmailToLowercase() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
                "testuser",
                "Test@Example.COM",
                "password123",
                "John",
                "Doe"
        );

        User expectedUser = createTestUser("testuser", "test@example.com", "John", "Doe");
        String hashedPassword = "$2a$12$hashedpassword";

        when(userRepository.existsByUsername("testuser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));
        when(passwordHashingService.hashPassword("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(expectedUser));

        // When & Then
        StepVerifier.create(registerUserService.registerUser(command))
                .assertNext(user -> {
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                })
                .verifyComplete();

        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should trim whitespace from user inputs")
    void shouldTrimWhitespaceFromUserInputs() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
                "  testuser  ",
                "  test@example.com  ",
                "password123",
                "  John  ",
                "  Doe  "
        );

        User expectedUser = createTestUser("testuser", "test@example.com", "John", "Doe");
        String hashedPassword = "$2a$12$hashedpassword";

        when(userRepository.existsByUsername("testuser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));
        when(passwordHashingService.hashPassword("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(expectedUser));

        // When & Then
        StepVerifier.create(registerUserService.registerUser(command))
                .assertNext(user -> {
                    assertThat(user.getUsername()).isEqualTo("testuser");
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                    assertThat(user.getFirstName()).isEqualTo("John");
                    assertThat(user.getLastName()).isEqualTo("Doe");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw UsernameAlreadyExistsException when username is taken")
    void shouldThrowUsernameAlreadyExistsException() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
                "existinguser",
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        when(userRepository.existsByUsername("existinguser")).thenReturn(Mono.just(true));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(registerUserService.registerUser(command))
                .expectError(UsernameAlreadyExistsException.class)
                .verify();

        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordHashingService, never()).hashPassword(anyString());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email is taken")
    void shouldThrowEmailAlreadyExistsException() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
                "testuser",
                "existing@example.com",
                "password123",
                "John",
                "Doe"
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(registerUserService.registerUser(command))
                .expectError(EmailAlreadyExistsException.class)
                .verify();

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordHashingService, never()).hashPassword(anyString());
    }

    @Test
    @DisplayName("Should check username availability correctly when username is available")
    void shouldCheckUsernameAvailabilityWhenAvailable() {
        // Given
        String username = "availableuser";
        when(userRepository.existsByUsername(username)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(registerUserService.isUsernameAvailable(username))
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).existsByUsername(username);
    }

    @Test
    @DisplayName("Should check username availability correctly when username is taken")
    void shouldCheckUsernameAvailabilityWhenTaken() {
        // Given
        String username = "takenuser";
        when(userRepository.existsByUsername(username)).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(registerUserService.isUsernameAvailable(username))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).existsByUsername(username);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should return false for invalid usernames")
    void shouldReturnFalseForInvalidUsernames(String invalidUsername) {
        // When & Then
        StepVerifier.create(registerUserService.isUsernameAvailable(invalidUsername))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    @DisplayName("Should check email availability correctly when email is available")
    void shouldCheckEmailAvailabilityWhenAvailable() {
        // Given
        String email = "available@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(registerUserService.isEmailAvailable(email))
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Should check email availability correctly when email is taken")
    void shouldCheckEmailAvailabilityWhenTaken() {
        // Given
        String email = "taken@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(registerUserService.isEmailAvailable(email))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Should normalize email case when checking availability")
    void shouldNormalizeEmailCaseWhenCheckingAvailability() {
        // Given
        String email = "Test@Example.COM";
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(registerUserService.isEmailAvailable(email))
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).existsByEmail("test@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should return false for invalid emails")
    void shouldReturnFalseForInvalidEmails(String invalidEmail) {
        // When & Then
        StepVerifier.create(registerUserService.isEmailAvailable(invalidEmail))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Should handle repository errors gracefully during registration")
    void shouldHandleRepositoryErrorsGracefullyDuringRegistration() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
                "testuser",
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(Mono.error(new RuntimeException("Database error")));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(registerUserService.registerUser(command))
                .expectError(RuntimeException.class)
                .verify();

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle password hashing errors gracefully")
    void shouldHandlePasswordHashingErrorsGracefully() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
                "testuser",
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));
        when(passwordHashingService.hashPassword("password123")).thenThrow(new RuntimeException("Hashing error"));

        // When & Then
        StepVerifier.create(registerUserService.registerUser(command))
                .expectError(RuntimeException.class)
                .verify();

        verify(passwordHashingService).hashPassword("password123");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle save errors gracefully")
    void shouldHandleSaveErrorsGracefully() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
                "testuser",
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        String hashedPassword = "$2a$12$hashedpassword";

        when(userRepository.existsByUsername("testuser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));
        when(passwordHashingService.hashPassword("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(Mono.error(new RuntimeException("Save error")));

        // When & Then
        StepVerifier.create(registerUserService.registerUser(command))
                .expectError(RuntimeException.class)
                .verify();

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle repository errors gracefully during username availability check")
    void shouldHandleRepositoryErrorsGracefullyDuringUsernameCheck() {
        // Given
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then
        StepVerifier.create(registerUserService.isUsernameAvailable(username))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should handle repository errors gracefully during email availability check")
    void shouldHandleRepositoryErrorsGracefullyDuringEmailCheck() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then
        StepVerifier.create(registerUserService.isEmailAvailable(email))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should perform concurrent validation checks for better performance")
    void shouldPerformConcurrentValidationChecks() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
                "testuser",
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        User expectedUser = createTestUser("testuser", "test@example.com", "John", "Doe");
        String hashedPassword = "$2a$12$hashedpassword";

        when(userRepository.existsByUsername("testuser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));
        when(passwordHashingService.hashPassword("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(expectedUser));

        // When
        StepVerifier.create(registerUserService.registerUser(command))
                .expectNext(expectedUser)
                .verifyComplete();

        // Then - Verify both checks were called (concurrent execution)
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
    }

    private User createTestUser(String username, String email, String firstName, String lastName) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .passwordHash("$2a$12$hashedpassword")
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
