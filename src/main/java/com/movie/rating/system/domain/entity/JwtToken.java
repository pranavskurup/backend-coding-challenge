package com.movie.rating.system.domain.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a JWT token for tracking and invalidation
 */
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"tokenHash"}) // Exclude sensitive data from toString
public class JwtToken {
    
    @EqualsAndHashCode.Include
    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final TokenType tokenType;
    private final Instant issuedAt;
    private final Instant expiresAt;
    @Builder.Default
    private final boolean isRevoked = false;
    private final Instant revokedAt;
    private final String revokedReason;
    private final Instant createdAt;
    private final Instant updatedAt;

    /**
     * Enum for token types
     */
    public enum TokenType {
        ACCESS,
        REFRESH
    }

    /**
     * Custom builder to handle validation and defaults
     */
    public static JwtTokenBuilder builder() {
        return new JwtTokenBuilder() {
            public JwtToken build() {
                if (super.createdAt == null) {
                    super.createdAt = Instant.now();
                }
                if (super.updatedAt == null) {
                    super.updatedAt = super.createdAt;
                }
                
                // Validate required fields
                Objects.requireNonNull(super.userId, "User ID cannot be null");
                Objects.requireNonNull(super.tokenHash, "Token hash cannot be null");
                Objects.requireNonNull(super.tokenType, "Token type cannot be null");
                Objects.requireNonNull(super.issuedAt, "Issued at cannot be null");
                Objects.requireNonNull(super.expiresAt, "Expires at cannot be null");
                Objects.requireNonNull(super.createdAt, "Created at cannot be null");
                Objects.requireNonNull(super.updatedAt, "Updated at cannot be null");
                
                // Validate business rules
                if (super.issuedAt.isAfter(super.expiresAt)) {
                    throw new IllegalArgumentException("Issued at cannot be after expires at");
                }
                
                return super.build();
            }
        };
    }

    /**
     * Check if the token is expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if the token is valid (not revoked and not expired)
     */
    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    /**
     * Revoke the token with a reason
     */
    public JwtToken revoke(String reason) {
        return this.toBuilder()
                .isRevoked(true)
                .revokedAt(Instant.now())
                .revokedReason(reason)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Get time until expiration in seconds
     */
    public long getSecondsUntilExpiration() {
        Instant now = Instant.now();
        if (now.isAfter(expiresAt)) {
            return 0;
        }
        return expiresAt.getEpochSecond() - now.getEpochSecond();
    }
}
