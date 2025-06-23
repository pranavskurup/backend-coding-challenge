package com.movie.rating.system.domain.port.inbound;

import com.movie.rating.system.domain.entity.User;
import reactor.core.publisher.Mono;

/**
 * Use case interface for user authentication operations using reactive streams
 */
public interface UserAuthenticationUseCase {

    /**
     * Authenticate user with username/email and password
     * @param command the authentication command
     * @return Mono containing the authenticated user
     */
    Mono<User> authenticate(AuthenticationCommand command);

    /**
     * Validate user credentials without full authentication
     * @param command the validation command
     * @return Mono<Boolean> indicating if credentials are valid
     */
    Mono<Boolean> validateCredentials(AuthenticationCommand command);

    /**
     * Update user's last login timestamp
     * @param userId the user ID
     * @return Mono<Void> indicating completion
     */
    Mono<Void> updateLastLogin(java.util.UUID userId);

    /**
     * Command object for user authentication
     */
    record AuthenticationCommand(
            String usernameOrEmail,
            String password
    ) {
        public AuthenticationCommand {
            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                throw new IllegalArgumentException("Username or email cannot be null or empty");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be null or empty");
            }
        }
    }
}
