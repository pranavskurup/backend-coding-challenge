package com.movie.rating.system.domain.port.outbound;

import com.movie.rating.system.domain.entity.JwtToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Repository interface for JWT token persistence operations
 */
public interface JwtTokenRepository {

    /**
     * Save a JWT token record
     *
     * @param jwtToken the token to save
     * @return saved token
     */
    Mono<JwtToken> save(JwtToken jwtToken);

    /**
     * Find token by token hash
     *
     * @param tokenHash the hashed token value
     * @return token if found
     */
    Mono<JwtToken> findByTokenHash(String tokenHash);

    /**
     * Find all active tokens for a user
     *
     * @param userId the user ID
     * @return flux of active tokens
     */
    Flux<JwtToken> findActiveTokensByUserId(UUID userId);

    /**
     * Find active tokens by user ID and token type
     *
     * @param userId the user ID
     * @param tokenType the token type
     * @return flux of active tokens
     */
    Flux<JwtToken> findActiveTokensByUserIdAndType(UUID userId, JwtToken.TokenType tokenType);

    /**
     * Revoke token by token hash
     *
     * @param tokenHash the token hash
     * @param reason the revocation reason
     * @return updated token
     */
    Mono<JwtToken> revokeByTokenHash(String tokenHash, String reason);

    /**
     * Revoke all tokens for a user
     *
     * @param userId the user ID
     * @param reason the revocation reason
     * @return number of revoked tokens
     */
    Mono<Long> revokeAllTokensForUser(UUID userId, String reason);

    /**
     * Revoke all tokens of specific type for a user
     *
     * @param userId the user ID
     * @param tokenType the token type to revoke
     * @param reason the revocation reason
     * @return number of revoked tokens
     */
    Mono<Long> revokeAllTokensForUserByType(UUID userId, JwtToken.TokenType tokenType, String reason);

    /**
     * Check if token is revoked by hash
     *
     * @param tokenHash the token hash
     * @return true if revoked
     */
    Mono<Boolean> isTokenRevoked(String tokenHash);

    /**
     * Delete expired tokens (cleanup)
     *
     * @param cutoffTime tokens expired before this time
     * @return number of deleted tokens
     */
    Mono<Long> deleteExpiredTokens(Instant cutoffTime);

    /**
     * Delete all tokens for a user (for user deletion)
     *
     * @param userId the user ID
     * @return number of deleted tokens
     */
    Mono<Long> deleteAllTokensForUser(UUID userId);

    /**
     * Find token by ID
     *
     * @param id the token ID
     * @return token if found
     */
    Mono<JwtToken> findById(UUID id);

    /**
     * Count active tokens for a user
     *
     * @param userId the user ID
     * @return count of active tokens
     */
    Mono<Long> countActiveTokensForUser(UUID userId);
}
