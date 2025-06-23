package com.movie.rating.system.domain.port.outbound;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Port for JWT token operations
 */
public interface JwtTokenService {

    /**
     * Generate a JWT token for a user
     *
     * @param userId the user ID
     * @param username the username
     * @param email the user email
     * @param duration token validity duration
     * @return generated token
     */
    Mono<String> generateToken(UUID userId, String username, String email, Duration duration);

    /**
     * Generate a JWT token with custom claims
     *
     * @param userId the user ID
     * @param username the username
     * @param email the user email
     * @param duration token validity duration
     * @param customClaims additional claims to include
     * @return generated token
     */
    Mono<String> generateToken(UUID userId, String username, String email, Duration duration, Map<String, Object> customClaims);

    /**
     * Validate and parse a JWT token
     *
     * @param token the token to validate
     * @return token claims if valid
     */
    Mono<TokenClaims> validateToken(String token);

    /**
     * Validate token and check if it's blacklisted
     *
     * @param token the token to validate
     * @return token claims if valid and not blacklisted
     */
    Mono<TokenClaims> validateTokenWithBlacklist(String token);

    /**
     * Extract user ID from token without full validation
     *
     * @param token the token
     * @return user ID if extractable
     */
    Mono<UUID> extractUserId(String token);

    /**
     * Check if token is expired
     *
     * @param token the token to check
     * @return true if expired
     */
    Mono<Boolean> isTokenExpired(String token);

    /**
     * Refresh a token (generate new token with same claims but extended expiry)
     *
     * @param token the current token
     * @param newDuration new validity duration
     * @return new token
     */
    Mono<String> refreshToken(String token, Duration newDuration);

    /**
     * Blacklist/invalidate a token
     *
     * @param token the token to blacklist
     * @param reason the reason for blacklisting
     * @return completion signal
     */
    Mono<Void> blacklistToken(String token, String reason);

    /**
     * Hash a token for storage (to avoid storing actual token)
     *
     * @param token the token to hash
     * @return hashed token
     */
    String hashToken(String token);

    /**
     * Token claims data structure
     */
    record TokenClaims(
            UUID userId,
            String username,
            String email,
            java.time.Instant issuedAt,
            java.time.Instant expiresAt,
            Map<String, Object> customClaims
    ) {}
}
