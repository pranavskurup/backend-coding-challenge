package com.movie.rating.system.domain.port.outbound;

import com.movie.rating.system.domain.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for User entity operations using reactive streams
 */
public interface UserRepository {

    /**
     * Save a user entity
     * @param user the user to save
     * @return Mono containing the saved user
     */
    Mono<User> save(User user);

    /**
     * Find a user by ID
     * @param id the user ID
     * @return Mono containing the user if found, empty otherwise
     */
    Mono<User> findById(UUID id);

    /**
     * Find a user by username
     * @param username the username
     * @return Mono containing the user if found, empty otherwise
     */
    Mono<User> findByUsername(String username);

    /**
     * Find a user by email
     * @param email the email address
     * @return Mono containing the user if found, empty otherwise
     */
    Mono<User> findByEmail(String email);

    /**
     * Find all active users
     * @return Flux containing all active users
     */
    Flux<User> findAllActive();

    /**
     * Find all users (active and inactive)
     * @return Flux containing all users
     */
    Flux<User> findAll();

    /**
     * Update a user entity
     * @param user the user to update
     * @return Mono containing the updated user
     */
    Mono<User> update(User user);

    /**
     * Delete a user by ID (soft delete - marks as inactive)
     * @param id the user ID
     * @return Mono<Void> indicating completion
     */
    Mono<Void> deleteById(UUID id);

    /**
     * Check if a user exists by username
     * @param username the username
     * @return Mono<Boolean> indicating if user exists
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Check if a user exists by email
     * @param email the email address
     * @return Mono<Boolean> indicating if user exists
     */
    Mono<Boolean> existsByEmail(String email);

    /**
     * Count total number of active users
     * @return Mono<Long> containing the count
     */
    Mono<Long> countActiveUsers();

    /**
     * Search users by username pattern (case insensitive)
     * @param pattern the search pattern
     * @return Flux containing matching users
     */
    Flux<User> searchByUsernamePattern(String pattern);
}