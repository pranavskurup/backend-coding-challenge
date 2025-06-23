package com.movie.rating.system.domain.port.inbound;

import com.movie.rating.system.domain.entity.User;
import reactor.core.publisher.Mono;

/**
 * Use case interface for user registration operations using reactive streams
 */
public interface RegisterUserUseCase {

    /**
     * Register a new user
     * @param command the registration command containing user details
     * @return Mono containing the registered user
     */
    Mono<User> registerUser(RegisterUserCommand command);

    /**
     * Validate if a username is available
     * @param username the username to validate
     * @return Mono<Boolean> indicating if username is available
     */
    Mono<Boolean> isUsernameAvailable(String username);

    /**
     * Validate if an email is available
     * @param email the email to validate
     * @return Mono<Boolean> indicating if email is available
     */
    Mono<Boolean> isEmailAvailable(String email);

    /**
     * Command object for user registration
     */
    record RegisterUserCommand(
            String username,
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        public RegisterUserCommand {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be null or empty");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be null or empty");
            }
            if (firstName == null || firstName.trim().isEmpty()) {
                throw new IllegalArgumentException("First name cannot be null or empty");
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                throw new IllegalArgumentException("Last name cannot be null or empty");
            }
        }
    }
}