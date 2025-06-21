package com.movie.rating.system.infrastructure.outbound.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC entity for JWT token persistence
 */
@Table("jwt_tokens")
public record JwtTokenEntity(
        @Id
        UUID id,
        
        @Column("user_id")
        UUID userId,
        
        @Column("token_hash")
        String tokenHash,
        
        @Column("token_type")
        String tokenType,
        
        @Column("issued_at")
        Instant issuedAt,
        
        @Column("expires_at")
        Instant expiresAt,
        
        @Column("is_revoked")
        Boolean isRevoked,
        
        @Column("revoked_at")
        Instant revokedAt,
        
        @Column("revoked_reason")
        String revokedReason,
        
        @Column("created_at")
        Instant createdAt,
        
        @Column("updated_at")
        Instant updatedAt
) {
}
