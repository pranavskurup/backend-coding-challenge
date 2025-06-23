package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.exception.AuthenticationFailedException;
import com.movie.rating.system.domain.exception.UserAccountInactiveException;
import com.movie.rating.system.domain.exception.UserNotFoundException;
import com.movie.rating.system.domain.port.inbound.UserAuthenticationUseCase;
import com.movie.rating.system.domain.port.outbound.PasswordHashingService;
import com.movie.rating.system.domain.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Service implementation for user authentication operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthenticationService implements UserAuthenticationUseCase {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;

    @Override
    public Mono<User> authenticate(AuthenticationCommand command) {
        log.info("Authenticating user with username/email: {}", command.usernameOrEmail());
        
        return findUserByUsernameOrEmail(command.usernameOrEmail())
                .flatMap(user -> validateUserForAuthentication(user, command.password()))
                .doOnSuccess(user -> log.info("User authenticated successfully: {}", user.getUsername()))
                .doOnError(error -> log.warn("Authentication failed for {}: {}", 
                        command.usernameOrEmail(), error.getMessage()));
    }

    @Override
    public Mono<Boolean> validateCredentials(AuthenticationCommand command) {
        log.debug("Validating credentials for user: {}", command.usernameOrEmail());
        
        return findUserByUsernameOrEmail(command.usernameOrEmail())
                .flatMap(user -> {
                    if (!user.isActive()) {
                        return Mono.just(false);
                    }
                    boolean isValid = passwordHashingService.verifyPassword(command.password(), user.getPasswordHash());
                    return Mono.just(isValid);
                })
                .onErrorReturn(false)
                .doOnSuccess(valid -> log.debug("Credential validation result for {}: {}", 
                        command.usernameOrEmail(), valid));
    }

    @Override
    public Mono<Void> updateLastLogin(UUID userId) {
        log.debug("Updating last login timestamp for user: {}", userId);
        
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId.toString())))
                .flatMap(user -> {
                    User updatedUser = user.toBuilder()
                            .updatedAt(Instant.now())
                            .build();
                    return userRepository.save(updatedUser);
                })
                .then()
                .doOnSuccess(v -> log.debug("Last login updated for user: {}", userId))
                .doOnError(error -> log.error("Failed to update last login for user {}: {}", 
                        userId, error.getMessage()));
    }

    /**
     * Find user by username or email
     */
    private Mono<User> findUserByUsernameOrEmail(String usernameOrEmail) {
        // Try to find by email first (if it contains @), then by username
        if (usernameOrEmail.contains("@")) {
            return userRepository.findByEmail(usernameOrEmail)
                    .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + usernameOrEmail)));
        } else {
            return userRepository.findByUsername(usernameOrEmail)
                    .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with username: " + usernameOrEmail)));
        }
    }

    /**
     * Validate user account and password for authentication
     */
    private Mono<User> validateUserForAuthentication(User user, String password) {
        log.debug("Validating user account for authentication: {}", user.getUsername());
        
        // Check if user account is active
        if (!user.isActive()) {
            return Mono.error(new UserAccountInactiveException("User account is deactivated: " + user.getUsername()));
        }

        // Verify password
        boolean passwordValid = passwordHashingService.verifyPassword(password, user.getPasswordHash());
        if (!passwordValid) {
            return Mono.error(new AuthenticationFailedException("Invalid username/email or password"));
        }
        
        return Mono.just(user);
    }
}
