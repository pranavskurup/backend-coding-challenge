package com.movie.rating.system.infrastructure.outbound.persistence.adapter;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.port.outbound.UserRepository;
import com.movie.rating.system.infrastructure.outbound.persistence.mapper.UserEntityMapper;
import com.movie.rating.system.infrastructure.outbound.persistence.repository.R2dbcUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC implementation of the user repository adapter.
 * This adapter implements the outbound port for user persistence operations
 * using R2DBC for reactive database access.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2dbcUserRepositoryAdapter implements UserRepository {

    private final R2dbcUserRepository repository;
    private final UserEntityMapper mapper;

    /**
     * Saves a user to the database.
     *
     * @param user the user to save
     * @return Mono containing the saved user
     */
    @Transactional
    public Mono<User> save(User user) {
        log.debug("Saving user with username: {}", user.getUsername());

        return repository.save(mapper.toEntity(user))
                .map(mapper::toDomain)
                .doOnSuccess(savedUser -> log.debug("Successfully saved user with ID: {}", savedUser.getId()))
                .doOnError(error -> log.error("Failed to save user with username: {}", user.getUsername(), error));
    }

    /**
     * Updates a user in the database.
     *
     * @param user the user to update
     * @return Mono containing the updated user
     */
    @Transactional
    public Mono<User> update(User user) {
        log.debug("Updating user with ID: {}", user.getId());

        // Set updated timestamp
        User updatedUser = user.toBuilder()
                .updatedAt(Instant.now())
                .build();

        return repository.save(mapper.toEntity(updatedUser))
                .map(mapper::toDomain)
                .doOnSuccess(result -> log.debug("Successfully updated user with ID: {}", result.getId()))
                .doOnError(error -> log.error("Failed to update user with ID: {}", user.getId(), error));
    }

    /**
     * Finds a user by ID.
     *
     * @param id the user ID
     * @return Mono containing the user if found, empty otherwise
     */
    public Mono<User> findById(UUID id) {
        log.debug("Finding user by ID: {}", id);

        return repository.findById(id)
                .map(mapper::toDomain)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("Found user with ID: {}", id);
                    } else {
                        log.debug("No user found with ID: {}", id);
                    }
                })
                .doOnError(error -> log.error("Error finding user by ID: {}", id, error));
    }

    /**
     * Finds a user by username.
     *
     * @param username the username
     * @return Mono containing the user if found, empty otherwise
     */
    public Mono<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);

        return repository.findByUsername(username)
                .map(mapper::toDomain)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("Found user with username: {}", username);
                    } else {
                        log.debug("No user found with username: {}", username);
                    }
                })
                .doOnError(error -> log.error("Error finding user by username: {}", username, error));
    }

    /**
     * Finds a user by email.
     *
     * @param email the email address
     * @return Mono containing the user if found, empty otherwise
     */
    public Mono<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);

        return repository.findByEmail(email)
                .map(mapper::toDomain)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("Found user with email: {}", email);
                    } else {
                        log.debug("No user found with email: {}", email);
                    }
                })
                .doOnError(error -> log.error("Error finding user by email: {}", email, error));
    }

    /**
     * Checks if a user exists by username.
     *
     * @param username the username
     * @return Mono containing true if user exists, false otherwise
     */
    public Mono<Boolean> existsByUsername(String username) {
        log.debug("Checking if user exists by username: {}", username);

        return repository.existsByUsername(username)
                .doOnSuccess(exists -> log.debug("User exists by username {}: {}", username, exists))
                .doOnError(error -> log.error("Error checking user existence by username: {}", username, error));
    }

    /**
     * Checks if a user exists by email.
     *
     * @param email the email address
     * @return Mono containing true if user exists, false otherwise
     */
    public Mono<Boolean> existsByEmail(String email) {
        log.debug("Checking if user exists by email: {}", email);

        return repository.existsByEmail(email)
                .doOnSuccess(exists -> log.debug("User exists by email {}: {}", email, exists))
                .doOnError(error -> log.error("Error checking user existence by email: {}", email, error));
    }

    /**
     * Finds all users.
     *
     * @return Flux of all users
     */
    public Flux<User> findAll() {
        log.debug("Finding all users");

        return repository.findAll()
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Successfully retrieved all users"))
                .doOnError(error -> log.error("Error finding all users", error));
    }

    /**
     * Finds all active users.
     *
     * @return Flux of active users
     */
    public Flux<User> findAllActive() {
        log.debug("Finding all active users");

        return repository.findAllActiveUsers()
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Successfully retrieved all active users"))
                .doOnError(error -> log.error("Error finding all active users", error));
    }

    /**
     * Counts active users.
     *
     * @return Mono containing the count of active users
     */
    public Mono<Long> countActiveUsers() {
        log.debug("Counting active users");

        return repository.countActiveUsers()
                .doOnSuccess(count -> log.debug("Active user count: {}", count))
                .doOnError(error -> log.error("Error counting active users", error));
    }

    /**
     * Deletes a user by ID.
     *
     * @param id the user ID
     * @return Mono that completes when deletion is finished
     */
    @Transactional
    public Mono<Void> deleteById(UUID id) {
        log.debug("Deleting user by ID: {}", id);

        return repository.deleteById(id)
                .doOnSuccess(result -> log.debug("Successfully deleted user with ID: {}", id))
                .doOnError(error -> log.error("Failed to delete user with ID: {}", id, error));
    }

    /**
     * Deletes a user.
     *
     * @param user the user to delete
     * @return Mono that completes when deletion is finished
     */
    @Transactional
    public Mono<Void> delete(User user) {
        log.debug("Deleting user with ID: {}", user.getId());

        return repository.delete(mapper.toEntity(user))
                .doOnSuccess(result -> log.debug("Successfully deleted user with ID: {}", user.getId()))
                .doOnError(error -> log.error("Failed to delete user with ID: {}", user.getId(), error));
    }

    /**
     * Searches users by username pattern (case insensitive).
     *
     * @param pattern the search pattern
     * @return Flux containing matching users
     */
    public Flux<User> searchByUsernamePattern(String pattern) {
        log.debug("Searching users by username pattern: {}", pattern);

        return repository.findByUsernameContainingIgnoreCase(pattern)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Successfully completed search for pattern: {}", pattern))
                .doOnError(error -> log.error("Failed to search users with pattern: {}", pattern, error));
    }
}
