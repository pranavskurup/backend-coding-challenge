package com.movie.rating.system.domain.port.inbound;

import com.movie.rating.system.domain.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use case interface for user query operations using reactive streams
 */
public interface UserQueryUseCase {

    /**
     * Find user by username
     * @param username the username
     * @return Mono containing the user if found
     */
    Mono<User> findByUsername(String username);

    /**
     * Find user by email
     * @param email the email address
     * @return Mono containing the user if found
     */
    Mono<User> findByEmail(String email);

    /**
     * Find user by ID
     * @param userId the user ID
     * @return Mono containing the user if found
     */
    Mono<User> findById(UUID userId);

    /**
     * Get all users with pagination
     * @param query the pagination query
     * @return Flux containing users for the requested page
     */
    Flux<User> findAllUsers(UserPaginationQuery query);

    /**
     * Count total active users
     * @return Mono containing the count
     */
    Mono<Long> countActiveUsers();

    /**
     * Check if user exists by username
     * @param username the username
     * @return Mono<Boolean> indicating existence
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Check if user exists by email
     * @param email the email
     * @return Mono<Boolean> indicating existence
     */
    Mono<Boolean> existsByEmail(String email);

    /**
     * Query object for user pagination
     */
    record UserPaginationQuery(
            int page,
            int size,
            String sortBy,
            String sortDirection,
            Boolean activeOnly
    ) {
        public UserPaginationQuery {
            if (page < 0) {
                throw new IllegalArgumentException("Page number cannot be negative");
            }
            if (size <= 0) {
                throw new IllegalArgumentException("Page size must be positive");
            }
            if (size > 100) {
                throw new IllegalArgumentException("Page size cannot exceed 100");
            }
        }

        public static UserPaginationQuery defaultQuery() {
            return new UserPaginationQuery(0, 20, "createdAt", "DESC", true);
        }
    }
}
