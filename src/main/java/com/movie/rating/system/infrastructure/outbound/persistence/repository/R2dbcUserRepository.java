package com.movie.rating.system.infrastructure.outbound.persistence.repository;

import com.movie.rating.system.infrastructure.outbound.persistence.entity.UserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for UserEntity operations.
 * Provides reactive database access for user-related operations.
 */
@Repository
public interface R2dbcUserRepository extends ReactiveCrudRepository<UserEntity, UUID> {

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return Mono containing the user entity if found, empty otherwise
     */
    Mono<UserEntity> findByUsername(String username);

    /**
     * Finds a user by email address.
     *
     * @param email the email address to search for
     * @return Mono containing the user entity if found, empty otherwise
     */
    Mono<UserEntity> findByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     *
     * @param username the username to check
     * @return Mono containing true if user exists, false otherwise
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email the email address to check
     * @return Mono containing true if user exists, false otherwise
     */
    Mono<Boolean> existsByEmail(String email);

    /**
     * Finds all active users.
     *
     * @return Flux of active user entities
     */
    @Query("SELECT * FROM users WHERE is_active = true")
    Flux<UserEntity> findAllActiveUsers();

    /**
     * Counts the number of active users.
     *
     * @return Mono containing the count of active users
     */
    @Query("SELECT COUNT(*) FROM users WHERE is_active = true")
    Mono<Long> countActiveUsers();

    /**
     * Finds users by their first name (case-insensitive).
     *
     * @param firstName the first name to search for
     * @return Flux of user entities matching the first name
     */
    @Query("SELECT * FROM users WHERE LOWER(first_name) = LOWER(:firstName)")
    Flux<UserEntity> findByFirstNameIgnoreCase(String firstName);

    /**
     * Finds users by their last name (case-insensitive).
     *
     * @param lastName the last name to search for
     * @return Flux of user entities matching the last name
     */
    @Query("SELECT * FROM users WHERE LOWER(last_name) = LOWER(:lastName)")
    Flux<UserEntity> findByLastNameIgnoreCase(String lastName);

    /**
     * Finds users whose username contains the given string (case-insensitive).
     *
     * @param username the username pattern to search for
     * @return Flux of user entities with matching usernames
     */
    @Query("SELECT * FROM users WHERE LOWER(username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Flux<UserEntity> findByUsernameContainingIgnoreCase(String username);
}
