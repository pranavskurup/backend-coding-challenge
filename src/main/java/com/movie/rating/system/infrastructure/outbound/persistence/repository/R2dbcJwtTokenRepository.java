package com.movie.rating.system.infrastructure.outbound.persistence.repository;

import com.movie.rating.system.infrastructure.outbound.persistence.entity.JwtTokenEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC repository for JWT token entities
 */
public interface R2dbcJwtTokenRepository extends R2dbcRepository<JwtTokenEntity, UUID> {

    /**
     * Find token by token hash
     */
    Mono<JwtTokenEntity> findByTokenHash(String tokenHash);

    /**
     * Find all active tokens for a user
     */
    @Query("SELECT * FROM jwt_tokens WHERE user_id = :userId AND is_revoked = false AND expires_at > :now")
    Flux<JwtTokenEntity> findActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Find active tokens by user ID and token type
     */
    @Query("SELECT * FROM jwt_tokens WHERE user_id = :userId AND token_type = :tokenType AND is_revoked = false AND expires_at > :now")
    Flux<JwtTokenEntity> findActiveTokensByUserIdAndType(@Param("userId") UUID userId, 
                                                         @Param("tokenType") String tokenType,
                                                         @Param("now") Instant now);

    /**
     * Revoke token by token hash
     */
    @Modifying
    @Query("UPDATE jwt_tokens SET is_revoked = true, revoked_at = :revokedAt, revoked_reason = :reason, updated_at = :updatedAt WHERE token_hash = :tokenHash")
    Mono<Integer> revokeByTokenHash(@Param("tokenHash") String tokenHash, 
                                   @Param("reason") String reason,
                                   @Param("revokedAt") Instant revokedAt,
                                   @Param("updatedAt") Instant updatedAt);

    /**
     * Revoke all tokens for a user
     */
    @Modifying
    @Query("UPDATE jwt_tokens SET is_revoked = true, revoked_at = :revokedAt, revoked_reason = :reason, updated_at = :updatedAt WHERE user_id = :userId AND is_revoked = false")
    Mono<Integer> revokeAllTokensForUser(@Param("userId") UUID userId, 
                                        @Param("reason") String reason,
                                        @Param("revokedAt") Instant revokedAt,
                                        @Param("updatedAt") Instant updatedAt);

    /**
     * Revoke all tokens of specific type for a user
     */
    @Modifying
    @Query("UPDATE jwt_tokens SET is_revoked = true, revoked_at = :revokedAt, revoked_reason = :reason, updated_at = :updatedAt WHERE user_id = :userId AND token_type = :tokenType AND is_revoked = false")
    Mono<Integer> revokeAllTokensForUserByType(@Param("userId") UUID userId, 
                                              @Param("tokenType") String tokenType,
                                              @Param("reason") String reason,
                                              @Param("revokedAt") Instant revokedAt,
                                              @Param("updatedAt") Instant updatedAt);

    /**
     * Check if token is revoked
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM jwt_tokens WHERE token_hash = :tokenHash AND is_revoked = true")
    Mono<Boolean> isTokenRevoked(@Param("tokenHash") String tokenHash);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM jwt_tokens WHERE expires_at < :cutoffTime")
    Mono<Integer> deleteExpiredTokens(@Param("cutoffTime") Instant cutoffTime);

    /**
     * Delete all tokens for a user
     */
    Mono<Integer> deleteByUserId(UUID userId);

    /**
     * Count active tokens for a user
     */
    @Query("SELECT COUNT(*) FROM jwt_tokens WHERE user_id = :userId AND is_revoked = false AND expires_at > :now")
    Mono<Long> countActiveTokensForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
