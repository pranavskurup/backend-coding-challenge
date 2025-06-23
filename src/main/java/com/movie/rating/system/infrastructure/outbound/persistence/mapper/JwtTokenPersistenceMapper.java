package com.movie.rating.system.infrastructure.outbound.persistence.mapper;

import com.movie.rating.system.domain.entity.JwtToken;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.JwtTokenEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between JwtToken domain entity and JwtTokenEntity
 */
@Component
public class JwtTokenPersistenceMapper {

    /**
     * Map domain entity to database entity
     */
    public JwtTokenEntity toEntity(JwtToken domain) {
        if (domain == null) {
            return null;
        }

        return new JwtTokenEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getTokenHash(),
                domain.getTokenType() != null ? domain.getTokenType().name() : null,
                domain.getIssuedAt(),
                domain.getExpiresAt(),
                domain.isRevoked(),
                domain.getRevokedAt(),
                domain.getRevokedReason(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    /**
     * Map database entity to domain entity
     */
    public JwtToken toDomain(JwtTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        return JwtToken.builder()
                .id(entity.id())
                .userId(entity.userId())
                .tokenHash(entity.tokenHash())
                .tokenType(entity.tokenType() != null ? JwtToken.TokenType.valueOf(entity.tokenType()) : null)
                .issuedAt(entity.issuedAt())
                .expiresAt(entity.expiresAt())
                .isRevoked(entity.isRevoked() != null ? entity.isRevoked() : false)
                .revokedAt(entity.revokedAt())
                .revokedReason(entity.revokedReason())
                .createdAt(entity.createdAt())
                .updatedAt(entity.updatedAt())
                .build();
    }
}
