package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.exception.EmailAlreadyExistsException;
import com.movie.rating.system.domain.exception.UsernameAlreadyExistsException;
import com.movie.rating.system.domain.port.inbound.RegisterUserUseCase;
import com.movie.rating.system.domain.port.outbound.PasswordHashingService;
import com.movie.rating.system.domain.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Implementation of RegisterUserUseCase for user registration operations.
 * This service handles user registration business logic including validation,
 * password hashing, and persistence operations using reactive streams.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;

    /**
     * Registers a new user after validating uniqueness constraints.
     * This operation is transactional to ensure data consistency.
     *
     * @param command the registration command containing user details
     * @return Mono containing the registered user
     * @throws UsernameAlreadyExistsException if username is already taken
     * @throws EmailAlreadyExistsException if email is already taken
     */
    @Override
    @Transactional
    public Mono<User> registerUser(RegisterUserCommand command) {
        log.info("Attempting to register user with username: {} and email: {}", 
                command.username(), command.email());

        return validateUserUniqueness(command)
                .then(createAndSaveUser(command))
                .doOnSuccess(user -> log.info("Successfully registered user with ID: {} and username: {}", 
                        user.getId(), user.getUsername()))
                .doOnError(error -> log.error("Failed to register user with username: {} and email: {}", 
                        command.username(), command.email(), error));
    }

    /**
     * Validates if a username is available for registration.
     *
     * @param username the username to validate
     * @return Mono<Boolean> indicating if username is available (true) or taken (false)
     */
    @Override
    public Mono<Boolean> isUsernameAvailable(String username) {
        log.debug("Checking username availability for: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            return Mono.just(false);
        }

        return userRepository.existsByUsername(username.trim())
                .map(exists -> !exists)
                .doOnSuccess(available -> log.debug("Username '{}' availability: {}", username, available))
                .doOnError(error -> log.error("Error checking username availability for: {}", username, error));
    }

    /**
     * Validates if an email is available for registration.
     *
     * @param email the email to validate
     * @return Mono<Boolean> indicating if email is available (true) or taken (false)
     */
    @Override
    public Mono<Boolean> isEmailAvailable(String email) {
        log.debug("Checking email availability for: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            return Mono.just(false);
        }

        return userRepository.existsByEmail(email.trim().toLowerCase())
                .map(exists -> !exists)
                .doOnSuccess(available -> log.debug("Email '{}' availability: {}", email, available))
                .doOnError(error -> log.error("Error checking email availability for: {}", email, error));
    }

    /**
     * Validates that both username and email are unique before registration.
     * This method performs both checks concurrently for better performance.
     *
     * @param command the registration command to validate
     * @return Mono<Void> that completes successfully if validation passes
     * @throws UsernameAlreadyExistsException if username is taken
     * @throws EmailAlreadyExistsException if email is taken
     */
    private Mono<Void> validateUserUniqueness(RegisterUserCommand command) {
        log.debug("Validating uniqueness for username: {} and email: {}", 
                command.username(), command.email());

        String trimmedUsername = command.username().trim();
        String normalizedEmail = command.email().trim().toLowerCase();

        Mono<Boolean> usernameCheck = userRepository.existsByUsername(trimmedUsername)
                .flatMap(exists -> exists 
                        ? Mono.error(new UsernameAlreadyExistsException(command.username()))
                        : Mono.just(false));

        Mono<Boolean> emailCheck = userRepository.existsByEmail(normalizedEmail)
                .flatMap(exists -> exists 
                        ? Mono.error(new EmailAlreadyExistsException(command.email()))
                        : Mono.just(false));

        return Mono.zip(usernameCheck, emailCheck)
                .then()
                .doOnSuccess(result -> log.debug("Uniqueness validation passed for username: {} and email: {}", 
                        command.username(), command.email()));
    }

    /**
     * Creates a new user entity with hashed password and saves it to the repository.
     *
     * @param command the registration command containing user details
     * @return Mono containing the saved user
     */
    private Mono<User> createAndSaveUser(RegisterUserCommand command) {
        log.debug("Creating and saving user with username: {}", command.username());

        return Mono.fromCallable(() -> passwordHashingService.hashPassword(command.password()))
                .doOnSuccess(hashedPassword -> log.debug("Password hashed successfully for user: {}", command.username()))
                .doOnError(error -> log.error("Failed to hash password for user: {}", command.username(), error))
                .flatMap(hashedPassword -> {
                    Instant now = Instant.now();
                    
                    User newUser = User.builder()
                            .username(command.username().trim())
                            .email(command.email().trim().toLowerCase())
                            .passwordHash(hashedPassword)
                            .firstName(command.firstName().trim())
                            .lastName(command.lastName().trim())
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    return userRepository.save(newUser);
                })
                .doOnSuccess(savedUser -> log.debug("User created and saved with ID: {}", savedUser.getId()));
    }
}
