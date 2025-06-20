package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.exception.*;
import com.movie.rating.system.domain.port.inbound.ManageUserProfileUseCase.ChangePasswordCommand;
import com.movie.rating.system.domain.port.inbound.ManageUserProfileUseCase.UpdateUserProfileCommand;
import com.movie.rating.system.domain.port.outbound.PasswordHashingService;
import com.movie.rating.system.domain.port.outbound.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ManageUserProfileService.
 * Tests all profile management operations including edge cases and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Manage User Profile Service Tests")
class ManageUserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    private ManageUserProfileService service;

    @BeforeEach
    void setUp() {
        service = new ManageUserProfileService(userRepository, passwordHashingService);
    }

    @Nested
    @DisplayName("Get User Profile Tests")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should get user profile successfully")
        void shouldGetUserProfileSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "testuser", "test@example.com");

            when(userRepository.findById(userId)).thenReturn(Mono.just(user));

            // When & Then
            StepVerifier.create(service.getUserProfile(userId))
                    .expectNext(user)
                    .verifyComplete();

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(service.getUserProfile(userId))
                    .expectError(UserNotFoundException.class)
                    .verify();

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should handle repository error")
        void shouldHandleRepositoryError() {
            // Given
            UUID userId = UUID.randomUUID();
            RuntimeException error = new RuntimeException("Database error");
            when(userRepository.findById(userId)).thenReturn(Mono.error(error));

            // When & Then
            StepVerifier.create(service.getUserProfile(userId))
                    .expectError(RuntimeException.class)
                    .verify();

            verify(userRepository).findById(userId);
        }
    }

    @Nested
    @DisplayName("Update User Profile Tests")
    class UpdateUserProfileTests {

        @Test
        @DisplayName("Should update user profile with all fields")
        void shouldUpdateUserProfileWithAllFields() {
            // Given
            UUID userId = UUID.randomUUID();
            User existingUser = createTestUser(userId, "olduser", "old@example.com");
            UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                    userId, "new@example.com", "NewFirst", "NewLast"
            );
            User updatedUser = existingUser.toBuilder()
                    .email("new@example.com")
                    .firstName("NewFirst")
                    .lastName("NewLast")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Mono.just(existingUser));
            when(userRepository.existsByEmail("new@example.com")).thenReturn(Mono.just(false));
            when(userRepository.update(any(User.class))).thenReturn(Mono.just(updatedUser));

            // When & Then
            StepVerifier.create(service.updateUserProfile(command))
                    .expectNext(updatedUser)
                    .verifyComplete();

            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail("new@example.com");
            verify(userRepository).update(any(User.class));
        }

        @Test
        @DisplayName("Should update only email when other fields are null")
        void shouldUpdateOnlyEmailWhenOtherFieldsAreNull() {
            // Given
            UUID userId = UUID.randomUUID();
            User existingUser = createTestUser(userId, "testuser", "old@example.com");
            UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                    userId, "new@example.com", null, null
            );

            when(userRepository.findById(userId)).thenReturn(Mono.just(existingUser));
            when(userRepository.existsByEmail("new@example.com")).thenReturn(Mono.just(false));
            when(userRepository.update(any(User.class))).thenReturn(Mono.just(existingUser));

            // When & Then
            StepVerifier.create(service.updateUserProfile(command))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail("new@example.com");
            verify(userRepository).update(argThat(user -> 
                user.getEmail().equals("new@example.com") &&
                user.getFirstName().equals(existingUser.getFirstName()) &&
                user.getLastName().equals(existingUser.getLastName())
            ));
        }

        @Test
        @DisplayName("Should not validate email when updating with same email")
        void shouldNotValidateEmailWhenUpdatingWithSameEmail() {
            // Given
            UUID userId = UUID.randomUUID();
            String existingEmail = "same@example.com";
            User existingUser = createTestUser(userId, "testuser", existingEmail);
            UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                    userId, existingEmail, "NewFirst", "NewLast"
            );

            when(userRepository.findById(userId)).thenReturn(Mono.just(existingUser));
            when(userRepository.update(any(User.class))).thenReturn(Mono.just(existingUser));

            // When & Then
            StepVerifier.create(service.updateUserProfile(command))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(userRepository).findById(userId);
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository).update(any(User.class));
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email is taken")
        void shouldThrowEmailAlreadyExistsExceptionWhenEmailIsTaken() {
            // Given
            UUID userId = UUID.randomUUID();
            User existingUser = createTestUser(userId, "testuser", "old@example.com");
            UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                    userId, "taken@example.com", "NewFirst", "NewLast"
            );

            when(userRepository.findById(userId)).thenReturn(Mono.just(existingUser));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(Mono.just(true));

            // When & Then
            StepVerifier.create(service.updateUserProfile(command))
                    .expectError(EmailAlreadyExistsException.class)
                    .verify();

            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail("taken@example.com");
            verify(userRepository, never()).update(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                    userId, "new@example.com", "NewFirst", "NewLast"
            );

            when(userRepository.findById(userId)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(service.updateUserProfile(command))
                    .expectError(UserNotFoundException.class)
                    .verify();

            verify(userRepository).findById(userId);
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).update(any(User.class));
        }

        @Test
        @DisplayName("Should handle case insensitive email normalization")
        void shouldHandleCaseInsensitiveEmailNormalization() {
            // Given
            UUID userId = UUID.randomUUID();
            User existingUser = createTestUser(userId, "testuser", "old@example.com");
            UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                    userId, "NEW@EXAMPLE.COM", "NewFirst", "NewLast"
            );

            when(userRepository.findById(userId)).thenReturn(Mono.just(existingUser));
            when(userRepository.existsByEmail("new@example.com")).thenReturn(Mono.just(false));
            when(userRepository.update(any(User.class))).thenReturn(Mono.just(existingUser));

            // When & Then
            StepVerifier.create(service.updateUserProfile(command))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail("new@example.com");
            verify(userRepository).update(argThat(user -> 
                user.getEmail().equals("new@example.com") // Normalized to lowercase
            ));
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "testuser", "test@example.com");
            ChangePasswordCommand command = new ChangePasswordCommand(
                    userId, "currentPassword", "newPassword"
            );
            String newHashedPassword = "newHashedPassword";

            when(userRepository.findById(userId)).thenReturn(Mono.just(user));
            when(passwordHashingService.verifyPassword("currentPassword", user.getPasswordHash()))
                    .thenReturn(true);
            when(passwordHashingService.hashPassword("newPassword"))
                    .thenReturn(newHashedPassword);
            when(userRepository.update(any(User.class))).thenReturn(Mono.just(user));

            // When & Then
            StepVerifier.create(service.changePassword(command))
                    .verifyComplete();

            verify(userRepository).findById(userId);
            verify(passwordHashingService).verifyPassword("currentPassword", user.getPasswordHash());
            verify(passwordHashingService).hashPassword("newPassword");
            verify(userRepository).update(argThat(updatedUser -> 
                updatedUser.getPasswordHash().equals(newHashedPassword)
            ));
        }

        @Test
        @DisplayName("Should throw InvalidPasswordException when current password is wrong")
        void shouldThrowInvalidPasswordExceptionWhenCurrentPasswordIsWrong() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "testuser", "test@example.com");
            ChangePasswordCommand command = new ChangePasswordCommand(
                    userId, "wrongPassword", "newPassword"
            );

            when(userRepository.findById(userId)).thenReturn(Mono.just(user));
            when(passwordHashingService.verifyPassword("wrongPassword", user.getPasswordHash()))
                    .thenReturn(false);

            // When & Then
            StepVerifier.create(service.changePassword(command))
                    .expectError(InvalidPasswordException.class)
                    .verify();

            verify(userRepository).findById(userId);
            verify(passwordHashingService).verifyPassword("wrongPassword", user.getPasswordHash());
            verify(passwordHashingService, never()).hashPassword(anyString());
            verify(userRepository, never()).update(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserAccountInactiveException when user is inactive")
        void shouldThrowUserAccountInactiveExceptionWhenUserIsInactive() {
            // Given
            UUID userId = UUID.randomUUID();
            User inactiveUser = createTestUser(userId, "testuser", "test@example.com")
                    .toBuilder()
                    .isActive(false)
                    .build();
            ChangePasswordCommand command = new ChangePasswordCommand(
                    userId, "currentPassword", "newPassword"
            );

            when(userRepository.findById(userId)).thenReturn(Mono.just(inactiveUser));

            // When & Then
            StepVerifier.create(service.changePassword(command))
                    .expectError(UserAccountInactiveException.class)
                    .verify();

            verify(userRepository).findById(userId);
            verify(passwordHashingService, never()).verifyPassword(anyString(), anyString());
            verify(passwordHashingService, never()).hashPassword(anyString());
            verify(userRepository, never()).update(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            ChangePasswordCommand command = new ChangePasswordCommand(
                    userId, "currentPassword", "newPassword"
            );

            when(userRepository.findById(userId)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(service.changePassword(command))
                    .expectError(UserNotFoundException.class)
                    .verify();

            verify(userRepository).findById(userId);
            verify(passwordHashingService, never()).verifyPassword(anyString(), anyString());
            verify(passwordHashingService, never()).hashPassword(anyString());
            verify(userRepository, never()).update(any(User.class));
        }
    }

    @Nested
    @DisplayName("Deactivate User Tests")
    class DeactivateUserTests {

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            User activeUser = createTestUser(userId, "testuser", "test@example.com");
            User deactivatedUser = activeUser.deactivate();

            when(userRepository.findById(userId)).thenReturn(Mono.just(activeUser));
            when(userRepository.update(any(User.class))).thenReturn(Mono.just(deactivatedUser));

            // When & Then
            StepVerifier.create(service.deactivateUser(userId))
                    .verifyComplete();

            verify(userRepository).findById(userId);
            verify(userRepository).update(argThat(user -> !user.isActive()));
        }

        @Test
        @DisplayName("Should handle deactivation of already inactive user")
        void shouldHandleDeactivationOfAlreadyInactiveUser() {
            // Given
            UUID userId = UUID.randomUUID();
            User inactiveUser = createTestUser(userId, "testuser", "test@example.com")
                    .toBuilder()
                    .isActive(false)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Mono.just(inactiveUser));

            // When & Then
            StepVerifier.create(service.deactivateUser(userId))
                    .verifyComplete();

            verify(userRepository).findById(userId);
            verify(userRepository, never()).update(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(service.deactivateUser(userId))
                    .expectError(UserNotFoundException.class)
                    .verify();

            verify(userRepository).findById(userId);
            verify(userRepository, never()).update(any(User.class));
        }
    }

    @Nested
    @DisplayName("Reactivate User Tests")
    class ReactivateUserTests {

        @Test
        @DisplayName("Should reactivate user successfully")
        void shouldReactivateUserSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            User inactiveUser = createTestUser(userId, "testuser", "test@example.com")
                    .toBuilder()
                    .isActive(false)
                    .build();
            User reactivatedUser = inactiveUser.reactivate();

            when(userRepository.findById(userId)).thenReturn(Mono.just(inactiveUser));
            when(userRepository.update(any(User.class))).thenReturn(Mono.just(reactivatedUser));

            // When & Then
            StepVerifier.create(service.reactivateUser(userId))
                    .expectNext(reactivatedUser)
                    .verifyComplete();

            verify(userRepository).findById(userId);
            verify(userRepository).update(argThat(user -> user.isActive()));
        }

        @Test
        @DisplayName("Should handle reactivation of already active user")
        void shouldHandleReactivationOfAlreadyActiveUser() {
            // Given
            UUID userId = UUID.randomUUID();
            User activeUser = createTestUser(userId, "testuser", "test@example.com");

            when(userRepository.findById(userId)).thenReturn(Mono.just(activeUser));

            // When & Then
            StepVerifier.create(service.reactivateUser(userId))
                    .expectNext(activeUser)
                    .verifyComplete();

            verify(userRepository).findById(userId);
            verify(userRepository, never()).update(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(service.reactivateUser(userId))
                    .expectError(UserNotFoundException.class)
                    .verify();

            verify(userRepository).findById(userId);
            verify(userRepository, never()).update(any(User.class));
        }
    }

    @Nested
    @DisplayName("Get All Active Users Tests")
    class GetAllActiveUsersTests {

        @Test
        @DisplayName("Should get all active users successfully")
        void shouldGetAllActiveUsersSuccessfully() {
            // Given
            User user1 = createTestUser(UUID.randomUUID(), "user1", "user1@example.com");
            User user2 = createTestUser(UUID.randomUUID(), "user2", "user2@example.com");

            when(userRepository.findAllActive()).thenReturn(Flux.just(user1, user2));

            // When & Then
            StepVerifier.create(service.getAllActiveUsers())
                    .expectNext(user1)
                    .expectNext(user2)
                    .verifyComplete();

            verify(userRepository).findAllActive();
        }

        @Test
        @DisplayName("Should return empty flux when no active users exist")
        void shouldReturnEmptyFluxWhenNoActiveUsersExist() {
            // Given
            when(userRepository.findAllActive()).thenReturn(Flux.empty());

            // When & Then
            StepVerifier.create(service.getAllActiveUsers())
                    .verifyComplete();

            verify(userRepository).findAllActive();
        }
    }

    @Nested
    @DisplayName("Search Users by Username Tests")
    class SearchUsersByUsernameTests {

        @Test
        @DisplayName("Should search users by username pattern successfully")
        void shouldSearchUsersByUsernamePatternSuccessfully() {
            // Given
            String pattern = "john";
            User user1 = createTestUser(UUID.randomUUID(), "john123", "john123@example.com");
            User user2 = createTestUser(UUID.randomUUID(), "johnsmith", "johnsmith@example.com");

            when(userRepository.searchByUsernamePattern(pattern))
                    .thenReturn(Flux.just(user1, user2));

            // When & Then
            StepVerifier.create(service.searchUsersByUsername(pattern))
                    .expectNext(user1)
                    .expectNext(user2)
                    .verifyComplete();

            verify(userRepository).searchByUsernamePattern(pattern);
        }

        @Test
        @DisplayName("Should return all active users when pattern is empty")
        void shouldReturnAllActiveUsersWhenPatternIsEmpty() {
            // Given
            String pattern = "";
            User user1 = createTestUser(UUID.randomUUID(), "user1", "user1@example.com");
            User user2 = createTestUser(UUID.randomUUID(), "user2", "user2@example.com");

            when(userRepository.findAllActive()).thenReturn(Flux.just(user1, user2));

            // When & Then
            StepVerifier.create(service.searchUsersByUsername(pattern))
                    .expectNext(user1)
                    .expectNext(user2)
                    .verifyComplete();

            verify(userRepository).findAllActive();
            verify(userRepository, never()).searchByUsernamePattern(anyString());
        }

        @Test
        @DisplayName("Should return all active users when pattern is null")
        void shouldReturnAllActiveUsersWhenPatternIsNull() {
            // Given
            String pattern = null;
            User user1 = createTestUser(UUID.randomUUID(), "user1", "user1@example.com");

            when(userRepository.findAllActive()).thenReturn(Flux.just(user1));

            // When & Then
            StepVerifier.create(service.searchUsersByUsername(pattern))
                    .expectNext(user1)
                    .verifyComplete();

            verify(userRepository).findAllActive();
            verify(userRepository, never()).searchByUsernamePattern(anyString());
        }

        @Test
        @DisplayName("Should trim whitespace from search pattern")
        void shouldTrimWhitespaceFromSearchPattern() {
            // Given
            String pattern = "  john  ";
            String trimmedPattern = "john";
            User user1 = createTestUser(UUID.randomUUID(), "john123", "john123@example.com");

            when(userRepository.searchByUsernamePattern(trimmedPattern))
                    .thenReturn(Flux.just(user1));

            // When & Then
            StepVerifier.create(service.searchUsersByUsername(pattern))
                    .expectNext(user1)
                    .verifyComplete();

            verify(userRepository).searchByUsernamePattern(trimmedPattern);
        }
    }

    // Helper method
    private User createTestUser(UUID id, String username, String email) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
