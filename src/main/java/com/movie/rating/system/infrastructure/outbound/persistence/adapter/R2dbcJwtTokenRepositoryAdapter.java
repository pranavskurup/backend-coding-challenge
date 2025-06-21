package com.movie.rating.system.infrastructure.outbound.persistence.adapter;

import com.movie.rating.system.domain.entity.JwtToken;
import com.movie.rating.system.domain.port.outbound.JwtTokenRepository;
import com.movie.rating.system.infrastructure.outbound.persistence.mapper.JwtTokenPersistenceMapper;
import com.movie.rating.system.infrastructure.outbound.persistence.repository.R2dbcJwtTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC adapter implementation for JWT token repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class R2dbcJwtTokenRepositoryAdapter implements JwtTokenRepository {

    private final R2dbcJwtTokenRepository r2dbcRepository;
    private final JwtTokenPersistenceMapper mapper;

    @Override
    public Mono<JwtToken> save(JwtToken jwtToken) {
        log.debug("Saving JWT token for user: {}", jwtToken.getUserId());
        
        return Mono.fromSupplier(() -> mapper.toEntity(jwtToken))
                .flatMap(r2dbcRepository::save)
                .map(mapper::toDomain)
                .doOnSuccess(saved -> log.debug("Successfully saved JWT token with ID: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to save JWT token: {}", error.getMessage()));
    }

    @Override
    public Mono<JwtToken> findByTokenHash(String tokenHash) {
        log.debug("Finding JWT token by hash");
        
        return r2dbcRepository.findByTokenHash(tokenHash)
                .map(mapper::toDomain)
                .doOnSuccess(token -> {
                    if (token != null) {
                        log.debug("Found JWT token for user: {}", token.getUserId());
                    } else {
                        log.debug("No JWT token found for hash");
                    }
                });
    }

    @Override
    public Flux<JwtToken> findActiveTokensByUserId(UUID userId) {
        log.debug("Finding active tokens for user: {}", userId);
        
        return r2dbcRepository.findActiveTokensByUserId(userId, Instant.now())
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Retrieved active tokens for user: {}", userId));
    }

    @Override
    public Flux<JwtToken> findActiveTokensByUserIdAndType(UUID userId, JwtToken.TokenType tokenType) {
        log.debug("Finding active {} tokens for user: {}", tokenType, userId);
        
        return r2dbcRepository.findActiveTokensByUserIdAndType(userId, tokenType.name(), Instant.now())
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Retrieved active {} tokens for user: {}", tokenType, userId));
    }

    @Override
    public Mono<JwtToken> revokeByTokenHash(String tokenHash, String reason) {
        log.debug("Revoking token with reason: {}", reason);
        
        Instant now = Instant.now();
        return r2dbcRepository.revokeByTokenHash(tokenHash, reason, now, now)
                .then(r2dbcRepository.findByTokenHash(tokenHash))
                .map(mapper::toDomain)
                .doOnSuccess(token -> log.debug("Successfully revoked token"))
                .doOnError(error -> log.error("Failed to revoke token: {}", error.getMessage()));
    }

    @Override
    public Mono<Long> revokeAllTokensForUser(UUID userId, String reason) {
        log.debug("Revoking all tokens for user: {} with reason: {}", userId, reason);
        
        Instant now = Instant.now();
        return r2dbcRepository.revokeAllTokensForUser(userId, reason, now, now)
                .map(Integer::longValue)
                .doOnSuccess(count -> log.debug("Revoked {} tokens for user: {}", count, userId))
                .doOnError(error -> log.error("Failed to revoke tokens for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<Long> revokeAllTokensForUserByType(UUID userId, JwtToken.TokenType tokenType, String reason) {
        log.debug("Revoking all {} tokens for user: {} with reason: {}", tokenType, userId, reason);
        
        Instant now = Instant.now();
        return r2dbcRepository.revokeAllTokensForUserByType(userId, tokenType.name(), reason, now, now)
                .map(Integer::longValue)
                .doOnSuccess(count -> log.debug("Revoked {} {} tokens for user: {}", count, tokenType, userId))
                .doOnError(error -> log.error("Failed to revoke {} tokens for user {}: {}", tokenType, userId, error.getMessage()));
    }

    @Override
    public Mono<Boolean> isTokenRevoked(String tokenHash) {
        log.debug("Checking if token is revoked");
        
        return r2dbcRepository.isTokenRevoked(tokenHash)
                .doOnSuccess(revoked -> log.debug("Token revoked status: {}", revoked));
    }

    @Override
    public Mono<Long> deleteExpiredTokens(Instant cutoffTime) {
        log.debug("Deleting tokens expired before: {}", cutoffTime);
        
        return r2dbcRepository.deleteExpiredTokens(cutoffTime)
                .map(Integer::longValue)
                .doOnSuccess(count -> log.debug("Deleted {} expired tokens", count))
                .doOnError(error -> log.error("Failed to delete expired tokens: {}", error.getMessage()));
    }

    @Override
    public Mono<Long> deleteAllTokensForUser(UUID userId) {
        log.debug("Deleting all tokens for user: {}", userId);
        
        return r2dbcRepository.deleteByUserId(userId)
                .map(Integer::longValue)
                .doOnSuccess(count -> log.debug("Deleted {} tokens for user: {}", count, userId))
                .doOnError(error -> log.error("Failed to delete tokens for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<JwtToken> findById(UUID id) {
        log.debug("Finding JWT token by ID: {}", id);
        
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain)
                .doOnSuccess(token -> {
                    if (token != null) {
                        log.debug("Found JWT token: {}", id);
                    } else {
                        log.debug("No JWT token found with ID: {}", id);
                    }
                });
    }

    @Override
    public Mono<Long> countActiveTokensForUser(UUID userId) {
        log.debug("Counting active tokens for user: {}", userId);
        
        return r2dbcRepository.countActiveTokensForUser(userId, Instant.now())
                .doOnSuccess(count -> log.debug("User {} has {} active tokens", userId, count));
    }
}
