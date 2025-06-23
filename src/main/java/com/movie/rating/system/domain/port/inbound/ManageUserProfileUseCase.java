package com.movie.rating.system.domain.port.inbound;

import com.movie.rating.system.domain.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use case interface for managing user profile operations using reactive streams
 */
public interface ManageUserProfileUseCase {

    /**
     * Get user profile by ID
     * @param userId the user ID
     * @return Mono containing the user profile
     */
    Mono<User> getUserProfile(UUID userId);

    /**
     * Update user profile
     * @param command the update profile command
     * @return Mono containing the updated user
     */
    Mono<User> updateUserProfile(UpdateUserProfileCommand command);

    /**
     * Change user password
     * @param command the change password command
     * @return Mono<Void> indicating completion
     */
    Mono<Void> changePassword(ChangePasswordCommand command);

    /**
     * Deactivate user account
     * @param userId the user ID
     * @return Mono<Void> indicating completion
     */
    Mono<Void> deactivateUser(UUID userId);

    /**
     * Reactivate user account
     * @param userId the user ID
     * @return Mono<User> containing the reactivated user
     */
    Mono<User> reactivateUser(UUID userId);

    /**
     * Get all active users (admin operation)
     * @return Flux containing all active users
     */
    Flux<User> getAllActiveUsers();

    /**
     * Search users by username pattern
     * @param usernamePattern the pattern to search for
     * @return Flux containing matching users
     */
    Flux<User> searchUsersByUsername(String usernamePattern);

    /**
     * Command object for updating user profile
     */
    record UpdateUserProfileCommand(
            UUID userId,
            String email,
            String firstName,
            String lastName
    ) {
        public UpdateUserProfileCommand {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (email != null && email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be empty");
            }
            if (firstName != null && firstName.trim().isEmpty()) {
                throw new IllegalArgumentException("First name cannot be empty");
            }
            if (lastName != null && lastName.trim().isEmpty()) {
                throw new IllegalArgumentException("Last name cannot be empty");
            }
        }
    }

    /**
     * Command object for changing password
     */
    record ChangePasswordCommand(
            UUID userId,
            String currentPassword,
            String newPassword
    ) {
        public ChangePasswordCommand {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("Current password cannot be null or empty");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("New password cannot be null or empty");
            }
        }
    }
}