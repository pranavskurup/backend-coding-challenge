package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.exception.*;
import com.movie.rating.system.domain.port.inbound.ManageUserProfileUseCase;
import com.movie.rating.system.domain.port.outbound.PasswordHashingService;
import com.movie.rating.system.domain.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementation of ManageUserProfileUseCase for user profile management operations.
 * This service handles profile updates, password changes, account activation/deactivation,
 * and user search operations using reactive streams.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManageUserProfileService implements ManageUserProfileUseCase {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;

    /**
     * Retrieves a user profile by ID.
     *
     * @param userId the user ID
     * @return Mono containing the user profile
     * @throws UserNotFoundException if user is not found
     */
    @Override
    public Mono<User> getUserProfile(UUID userId) {
        log.info("Retrieving user profile for ID: {}", userId);
        
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId.toString())))
                .doOnSuccess(user -> log.debug("Successfully retrieved profile for user: {}", user.getUsername()))
                .doOnError(error -> log.error("Failed to retrieve profile for user ID: {}", userId, error));
    }

    /**
     * Updates a user profile with the provided information.
     * Only non-null fields in the command will be updated.
     *
     * @param command the update profile command
     * @return Mono containing the updated user
     * @throws UserNotFoundException if user is not found
     * @throws EmailAlreadyExistsException if email is already taken by another user
     */
    @Override
    @Transactional
    public Mono<User> updateUserProfile(UpdateUserProfileCommand command) {
        log.info("Updating profile for user ID: {}", command.userId());
        
        return userRepository.findById(command.userId())
                .switchIfEmpty(Mono.error(new UserNotFoundException(command.userId().toString())))
                .flatMap(existingUser -> validateEmailUniqueness(command, existingUser)
                        .then(Mono.just(existingUser)))
                .flatMap(existingUser -> updateUserFields(existingUser, command))
                .flatMap(userRepository::update)
                .doOnSuccess(updatedUser -> log.info("Successfully updated profile for user: {}", updatedUser.getUsername()))
                .doOnError(error -> log.error("Failed to update profile for user ID: {}", command.userId(), error));
    }

    /**
     * Changes a user's password after verifying the current password.
     *
     * @param command the change password command
     * @return Mono<Void> indicating completion
     * @throws UserNotFoundException if user is not found
     * @throws InvalidPasswordException if current password is incorrect
     * @throws UserAccountInactiveException if user account is inactive
     */
    @Override
    @Transactional
    public Mono<Void> changePassword(ChangePasswordCommand command) {
        log.info("Changing password for user ID: {}", command.userId());
        
        return userRepository.findById(command.userId())
                .switchIfEmpty(Mono.error(new UserNotFoundException(command.userId().toString())))
                .flatMap(user -> {
                    if (!user.isActive()) {
                        return Mono.error(new UserAccountInactiveException(command.userId().toString()));
                    }
                    return Mono.just(user);
                })
                .flatMap(user -> verifyCurrentPassword(user, command.currentPassword())
                        .then(Mono.just(user)))
                .flatMap(user -> hashNewPasswordAndUpdate(user, command.newPassword()))
                .then()
                .doOnSuccess(result -> log.info("Successfully changed password for user ID: {}", command.userId()))
                .doOnError(error -> log.error("Failed to change password for user ID: {}", command.userId(), error));
    }

    /**
     * Deactivates a user account.
     *
     * @param userId the user ID
     * @return Mono<Void> indicating completion
     * @throws UserNotFoundException if user is not found
     */
    @Override
    @Transactional
    public Mono<Void> deactivateUser(UUID userId) {
        log.info("Deactivating user ID: {}", userId);
        
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId.toString())))
                .flatMap(user -> {
                    if (!user.isActive()) {
                        log.warn("User ID: {} is already inactive", userId);
                        return Mono.just(user);
                    }
                    
                    User deactivatedUser = user.deactivate();
                    return userRepository.update(deactivatedUser);
                })
                .then()
                .doOnSuccess(result -> log.info("Successfully deactivated user ID: {}", userId))
                .doOnError(error -> log.error("Failed to deactivate user ID: {}", userId, error));
    }

    /**
     * Reactivates a user account.
     *
     * @param userId the user ID
     * @return Mono<User> containing the reactivated user
     * @throws UserNotFoundException if user is not found
     */
    @Override
    @Transactional
    public Mono<User> reactivateUser(UUID userId) {
        log.info("Reactivating user ID: {}", userId);
        
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId.toString())))
                .flatMap(user -> {
                    if (user.isActive()) {
                        log.warn("User ID: {} is already active", userId);
                        return Mono.just(user);
                    }
                    
                    User reactivatedUser = user.reactivate();
                    return userRepository.update(reactivatedUser);
                })
                .doOnSuccess(user -> log.info("Successfully reactivated user: {}", user.getUsername()))
                .doOnError(error -> log.error("Failed to reactivate user ID: {}", userId, error));
    }

    /**
     * Retrieves all active users.
     *
     * @return Flux containing all active users
     */
    @Override
    public Flux<User> getAllActiveUsers() {
        log.info("Retrieving all active users");
        
        return userRepository.findAllActive()
                .doOnComplete(() -> log.debug("Successfully retrieved all active users"))
                .doOnError(error -> log.error("Failed to retrieve active users", error));
    }

    /**
     * Searches users by username pattern.
     *
     * @param usernamePattern the pattern to search for
     * @return Flux containing matching users
     */
    @Override
    public Flux<User> searchUsersByUsername(String usernamePattern) {
        log.info("Searching users with pattern: {}", usernamePattern);
        
        if (usernamePattern == null || usernamePattern.trim().isEmpty()) {
            log.debug("Empty search pattern, returning all active users");
            return getAllActiveUsers();
        }
        
        String sanitizedPattern = usernamePattern.trim();
        
        return userRepository.searchByUsernamePattern(sanitizedPattern)
                .doOnComplete(() -> log.debug("Successfully completed search for pattern: {}", sanitizedPattern))
                .doOnError(error -> log.error("Failed to search users with pattern: {}", sanitizedPattern, error));
    }

    /**
     * Validates that the email is not already taken by another user.
     */
    private Mono<Void> validateEmailUniqueness(UpdateUserProfileCommand command, User existingUser) {
        if (command.email() == null) {
            return Mono.empty(); // No email update
        }
        
        String normalizedEmail = command.email().trim().toLowerCase();
        String currentEmail = existingUser.getEmail().toLowerCase();
        
        if (normalizedEmail.equals(currentEmail)) {
            return Mono.empty(); // Same email, no validation needed
        }
        
        log.debug("Validating email uniqueness for: {}", normalizedEmail);
        
        return userRepository.existsByEmail(normalizedEmail)
                .flatMap(exists -> exists 
                        ? Mono.<Void>error(new EmailAlreadyExistsException(command.email()))
                        : Mono.<Void>empty())
                .doOnSuccess(result -> log.debug("Email uniqueness validation passed for: {}", normalizedEmail));
    }

    /**
     * Updates user fields based on the command.
     */
    private Mono<User> updateUserFields(User existingUser, UpdateUserProfileCommand command) {
        log.debug("Updating fields for user: {}", existingUser.getUsername());
        
        User.UserBuilder userBuilder = existingUser.toBuilder()
                .updatedAt(Instant.now());
        
        if (command.email() != null) {
            userBuilder.email(command.email().trim().toLowerCase());
        }
        
        if (command.firstName() != null) {
            userBuilder.firstName(command.firstName().trim());
        }
        
        if (command.lastName() != null) {
            userBuilder.lastName(command.lastName().trim());
        }
        
        return Mono.just(userBuilder.build());
    }

    /**
     * Verifies the current password matches the stored hash.
     */
    private Mono<Void> verifyCurrentPassword(User user, String currentPassword) {
        log.debug("Verifying current password for user: {}", user.getUsername());
        
        return Mono.fromCallable(() -> passwordHashingService.verifyPassword(currentPassword, user.getPasswordHash()))
                .flatMap(isValid -> isValid 
                        ? Mono.<Void>empty()
                        : Mono.error(new InvalidPasswordException()))
                .doOnSuccess(result -> log.debug("Password verification successful for user: {}", user.getUsername()))
                .doOnError(error -> log.warn("Password verification failed for user: {}", user.getUsername()));
    }

    /**
     * Hashes the new password and updates the user.
     */
    private Mono<User> hashNewPasswordAndUpdate(User user, String newPassword) {
        log.debug("Hashing new password for user: {}", user.getUsername());
        
        return Mono.fromCallable(() -> passwordHashingService.hashPassword(newPassword))
                .map(hashedPassword -> user.toBuilder()
                        .passwordHash(hashedPassword)
                        .updatedAt(Instant.now())
                        .build())
                .flatMap(userRepository::update)
                .doOnSuccess(updatedUser -> log.debug("Password updated successfully for user: {}", updatedUser.getUsername()));
    }
}
