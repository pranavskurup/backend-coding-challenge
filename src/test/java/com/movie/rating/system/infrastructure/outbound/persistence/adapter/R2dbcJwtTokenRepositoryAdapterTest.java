package com.movie.rating.system.infrastructure.outbound.persistence.adapter;

import com.movie.rating.system.domain.entity.JwtToken;
import com.movie.rating.system.domain.port.outbound.JwtTokenRepository;
import com.movie.rating.system.infrastructure.outbound.persistence.mapper.JwtTokenPersistenceMapper;
import com.movie.rating.system.infrastructure.outbound.persistence.repository.R2dbcJwtTokenRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for R2dbcJwtTokenRepositoryAdapter using Testcontainers
 */
@DataR2dbcTest
@Testcontainers
@Import(R2dbcJwtTokenRepositoryAdapterTest.TestConfig.class)
class R2dbcJwtTokenRepositoryAdapterTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers manages the lifecycle
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + postgres.getHost() + ":" + postgres.getFirstMappedPort()
                + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        JwtTokenPersistenceMapper jwtTokenPersistenceMapper() {
            return new JwtTokenPersistenceMapper();
        }

        @Bean
        R2dbcJwtTokenRepositoryAdapter r2dbcJwtTokenRepositoryAdapter(
                R2dbcJwtTokenRepository r2dbcRepository,
                JwtTokenPersistenceMapper mapper) {
            return new R2dbcJwtTokenRepositoryAdapter(r2dbcRepository, mapper);
        }
    }

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Autowired
    private R2dbcJwtTokenRepository r2dbcJwtTokenRepository;

    @Autowired
    private org.springframework.r2dbc.core.DatabaseClient databaseClient;

    private UUID testUserId;
    private String testTokenHash;
    private JwtToken testJwtToken;

    @BeforeEach
    void setUp() {
        runMigrations();

        // Clean up before each test
        r2dbcJwtTokenRepository.deleteAll().block();
        
        // Clean up users table
        databaseClient.sql("DELETE FROM users").fetch().rowsUpdated().block();

        testUserId = UUID.randomUUID();
        testTokenHash = "test-token-hash-" + System.currentTimeMillis();

        // Create a test user first to satisfy foreign key constraint
        createTestUser(testUserId).block();

        testJwtToken = JwtToken.builder()
                .userId(testUserId)
                .tokenHash(testTokenHash)
                .tokenType(JwtToken.TokenType.ACCESS)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .isRevoked(false)
                .build();
    }

    private Mono<Void> createTestUser(UUID userId) {
        return databaseClient.sql("""
                INSERT INTO users (id, username, email, password_hash, first_name, last_name, is_active, created_at, updated_at)
                VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                """)
                .bind("$1", userId)
                .bind("$2", "testuser-" + userId.toString().substring(0, 8))
                .bind("$3", "test-" + userId.toString().substring(0, 8) + "@example.com")
                .bind("$4", "hashed-password")
                .bind("$5", "Test")
                .bind("$6", "User")
                .bind("$7", true)
                .bind("$8", Instant.now())
                .bind("$9", Instant.now())
                .fetch()
                .rowsUpdated()
                .then();
    }

    private void runMigrations() {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();

        flyway.clean();
        flyway.migrate();
    }

    @Nested
    @DisplayName("Save and Find Operations")
    class SaveAndFindOperations {

        @Test
        @DisplayName("Should save and retrieve JWT token")
        void shouldSaveAndRetrieveJwtToken() {
            // When & Then
            StepVerifier.create(jwtTokenRepository.save(testJwtToken))
                    .assertNext(savedToken -> {
                        assertThat(savedToken.getId()).isNotNull();
                        assertThat(savedToken.getUserId()).isEqualTo(testUserId);
                        assertThat(savedToken.getTokenHash()).isEqualTo(testTokenHash);
                        assertThat(savedToken.getTokenType()).isEqualTo(JwtToken.TokenType.ACCESS);
                        assertThat(savedToken.isRevoked()).isFalse();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should find token by hash")
        void shouldFindTokenByHash() {
            // Given
            Mono<JwtToken> setup = jwtTokenRepository.save(testJwtToken);

            // When & Then
            StepVerifier.create(setup.then(jwtTokenRepository.findByTokenHash(testTokenHash)))
                    .assertNext(foundToken -> {
                        assertThat(foundToken.getUserId()).isEqualTo(testUserId);
                        assertThat(foundToken.getTokenHash()).isEqualTo(testTokenHash);
                        assertThat(foundToken.getTokenType()).isEqualTo(JwtToken.TokenType.ACCESS);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when token hash not found")
        void shouldReturnEmptyWhenTokenHashNotFound() {
            // When & Then
            StepVerifier.create(jwtTokenRepository.findByTokenHash("non-existent-hash"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should find token by ID")
        void shouldFindTokenById() {
            // Given
            Mono<JwtToken> setup = jwtTokenRepository.save(testJwtToken);

            // When & Then
            StepVerifier.create(setup.flatMap(saved -> jwtTokenRepository.findById(saved.getId())))
                    .assertNext(foundToken -> {
                        assertThat(foundToken.getUserId()).isEqualTo(testUserId);
                        assertThat(foundToken.getTokenHash()).isEqualTo(testTokenHash);
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Active Token Operations")
    class ActiveTokenOperations {

        @Test
        @DisplayName("Should find active tokens by user ID")
        void shouldFindActiveTokensByUserId() {
            // Given
            JwtToken accessToken = testJwtToken;
            JwtToken refreshToken = JwtToken.builder()
                    .userId(testUserId)
                    .tokenHash("refresh-token-hash")
                    .tokenType(JwtToken.TokenType.REFRESH)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .isRevoked(false)
                    .build();

            Mono<Void> setup = jwtTokenRepository.save(accessToken)
                    .then(jwtTokenRepository.save(refreshToken))
                    .then();

            // When & Then
            StepVerifier.create(setup.thenMany(jwtTokenRepository.findActiveTokensByUserId(testUserId)))
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should find active tokens by user ID and type")
        void shouldFindActiveTokensByUserIdAndType() {
            // Given
            JwtToken accessToken = testJwtToken;
            JwtToken refreshToken = JwtToken.builder()
                    .userId(testUserId)
                    .tokenHash("refresh-token-hash")
                    .tokenType(JwtToken.TokenType.REFRESH)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .isRevoked(false)
                    .build();

            Mono<Void> setup = jwtTokenRepository.save(accessToken)
                    .then(jwtTokenRepository.save(refreshToken))
                    .then();

            // When & Then
            StepVerifier.create(setup.thenMany(
                    jwtTokenRepository.findActiveTokensByUserIdAndType(testUserId, JwtToken.TokenType.ACCESS)))
                    .assertNext(token -> {
                        assertThat(token.getTokenType()).isEqualTo(JwtToken.TokenType.ACCESS);
                        assertThat(token.getTokenHash()).isEqualTo(testTokenHash);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should count active tokens for user")
        void shouldCountActiveTokensForUser() {
            // Given
            JwtToken token1 = testJwtToken;
            JwtToken token2 = JwtToken.builder()
                    .userId(testUserId)
                    .tokenHash("token-hash-2")
                    .tokenType(JwtToken.TokenType.REFRESH)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .isRevoked(false)
                    .build();

            Mono<Void> setup = jwtTokenRepository.save(token1)
                    .then(jwtTokenRepository.save(token2))
                    .then();

            // When & Then
            StepVerifier.create(setup.then(jwtTokenRepository.countActiveTokensForUser(testUserId)))
                    .expectNext(2L)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should not include expired tokens in active count")
        void shouldNotIncludeExpiredTokensInActiveCount() {
            // Given
            JwtToken activeToken = testJwtToken;
            JwtToken expiredToken = JwtToken.builder()
                    .userId(testUserId)
                    .tokenHash("expired-token-hash")
                    .tokenType(JwtToken.TokenType.ACCESS)
                    .issuedAt(Instant.now().minus(2, ChronoUnit.HOURS))
                    .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS)) // Expired
                    .isRevoked(false)
                    .build();

            Mono<Void> setup = jwtTokenRepository.save(activeToken)
                    .then(jwtTokenRepository.save(expiredToken))
                    .then();

            // When & Then
            StepVerifier.create(setup.then(jwtTokenRepository.countActiveTokensForUser(testUserId)))
                    .expectNext(1L) // Only the active token should be counted
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Token Revocation Operations")
    class TokenRevocationOperations {

        @Test
        @DisplayName("Should revoke token by hash")
        void shouldRevokeTokenByHash() {
            // Given
            Mono<JwtToken> setup = jwtTokenRepository.save(testJwtToken);

            // When & Then
            StepVerifier.create(setup.then(jwtTokenRepository.revokeByTokenHash(testTokenHash, "User logout")))
                    .assertNext(revokedToken -> {
                        assertThat(revokedToken.isRevoked()).isTrue();
                        assertThat(revokedToken.getRevokedReason()).isEqualTo("User logout");
                        assertThat(revokedToken.getRevokedAt()).isNotNull();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should check if token is revoked")
        void shouldCheckIfTokenIsRevoked() {
            // Given
            Mono<JwtToken> setup = jwtTokenRepository.save(testJwtToken)
                    .then(jwtTokenRepository.revokeByTokenHash(testTokenHash, "Test revocation"))
                    .then(jwtTokenRepository.findByTokenHash(testTokenHash));

            // When & Then
            StepVerifier.create(setup.then(jwtTokenRepository.isTokenRevoked(testTokenHash)))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return false for non-revoked token")
        void shouldReturnFalseForNonRevokedToken() {
            // Given
            Mono<JwtToken> setup = jwtTokenRepository.save(testJwtToken);

            // When & Then
            StepVerifier.create(setup.then(jwtTokenRepository.isTokenRevoked(testTokenHash)))
                    .expectNext(false)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should revoke all tokens for user")
        void shouldRevokeAllTokensForUser() {
            // Given
            JwtToken token1 = testJwtToken;
            JwtToken token2 = JwtToken.builder()
                    .userId(testUserId)
                    .tokenHash("token-hash-2")
                    .tokenType(JwtToken.TokenType.REFRESH)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .isRevoked(false)
                    .build();

            Mono<Void> setup = jwtTokenRepository.save(token1)
                    .then(jwtTokenRepository.save(token2))
                    .then();

            // When & Then
            StepVerifier.create(setup.then(jwtTokenRepository.revokeAllTokensForUser(testUserId, "Account compromise")))
                    .expectNext(2L)
                    .verifyComplete();

            // Verify tokens are revoked
            StepVerifier.create(jwtTokenRepository.isTokenRevoked(testTokenHash))
                    .expectNext(true)
                    .verifyComplete();

            StepVerifier.create(jwtTokenRepository.isTokenRevoked("token-hash-2"))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should revoke all tokens for user by type")
        void shouldRevokeAllTokensForUserByType() {
            // Given
            JwtToken accessToken = testJwtToken;
            JwtToken refreshToken = JwtToken.builder()
                    .userId(testUserId)
                    .tokenHash("refresh-token-hash")
                    .tokenType(JwtToken.TokenType.REFRESH)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .isRevoked(false)
                    .build();

            Mono<Void> setup = jwtTokenRepository.save(accessToken)
                    .then(jwtTokenRepository.save(refreshToken))
                    .then();

            // When & Then - Revoke only ACCESS tokens
            StepVerifier.create(setup.then(
                    jwtTokenRepository.revokeAllTokensForUserByType(testUserId, JwtToken.TokenType.ACCESS, "Access revocation")))
                    .expectNext(1L)
                    .verifyComplete();

            // Verify only ACCESS token is revoked
            StepVerifier.create(jwtTokenRepository.isTokenRevoked(testTokenHash))
                    .expectNext(true)
                    .verifyComplete();

            StepVerifier.create(jwtTokenRepository.isTokenRevoked("refresh-token-hash"))
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Token Cleanup Operations")
    class TokenCleanupOperations {

        @Test
        @DisplayName("Should delete expired tokens")
        void shouldDeleteExpiredTokens() {
            // Given
            JwtToken activeToken = testJwtToken;
            JwtToken expiredToken = JwtToken.builder()
                    .userId(testUserId)
                    .tokenHash("expired-token-hash")
                    .tokenType(JwtToken.TokenType.ACCESS)
                    .issuedAt(Instant.now().minus(2, ChronoUnit.HOURS))
                    .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS)) // Expired
                    .isRevoked(false)
                    .build();

            Mono<Void> setup = jwtTokenRepository.save(activeToken)
                    .then(jwtTokenRepository.save(expiredToken))
                    .then();

            // When & Then
            Instant cutoffTime = Instant.now();
            StepVerifier.create(setup.then(jwtTokenRepository.deleteExpiredTokens(cutoffTime)))
                    .expectNext(1L) // Should delete 1 expired token
                    .verifyComplete();

            // Verify active token still exists
            StepVerifier.create(jwtTokenRepository.findByTokenHash(testTokenHash))
                    .expectNextCount(1)
                    .verifyComplete();

            // Verify expired token is deleted
            StepVerifier.create(jwtTokenRepository.findByTokenHash("expired-token-hash"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should delete all tokens for user")
        void shouldDeleteAllTokensForUser() {
            // Given
            UUID anotherUserId = UUID.randomUUID();
            
            // Create another test user
            Mono<Void> createAnotherUser = createTestUser(anotherUserId);
            
            JwtToken userToken1 = testJwtToken;
            JwtToken userToken2 = JwtToken.builder()
                    .userId(testUserId)
                    .tokenHash("token-hash-2")
                    .tokenType(JwtToken.TokenType.REFRESH)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .isRevoked(false)
                    .build();
            JwtToken otherUserToken = JwtToken.builder()
                    .userId(anotherUserId)
                    .tokenHash("other-user-token-hash")
                    .tokenType(JwtToken.TokenType.ACCESS)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .isRevoked(false)
                    .build();

            Mono<Void> setup = createAnotherUser
                    .then(jwtTokenRepository.save(userToken1))
                    .then(jwtTokenRepository.save(userToken2))
                    .then(jwtTokenRepository.save(otherUserToken))
                    .then();

            // When & Then
            StepVerifier.create(setup.then(jwtTokenRepository.deleteAllTokensForUser(testUserId)))
                    .expectNext(2L) // Should delete 2 tokens for testUserId
                    .verifyComplete();

            // Verify other user's token still exists
            StepVerifier.create(jwtTokenRepository.findByTokenHash("other-user-token-hash"))
                    .expectNextCount(1)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should handle operations on non-existent tokens gracefully")
        void shouldHandleNonExistentTokensGracefully() {
            // When & Then - Revoke non-existent token
            StepVerifier.create(jwtTokenRepository.revokeByTokenHash("non-existent-hash", "Test"))
                    .verifyComplete();

            // When & Then - Check if non-existent token is revoked
            StepVerifier.create(jwtTokenRepository.isTokenRevoked("non-existent-hash"))
                    .expectNext(false)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle multiple tokens with same user ID")
        void shouldHandleMultipleTokensWithSameUserId() {
            // Given
            JwtToken token1 = testJwtToken;
            JwtToken token2 = JwtToken.builder()
                    .userId(testUserId) // Same user ID
                    .tokenHash("different-token-hash")
                    .tokenType(JwtToken.TokenType.REFRESH)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .isRevoked(false)
                    .build();

            Mono<Void> setup = jwtTokenRepository.save(token1)
                    .then(jwtTokenRepository.save(token2))
                    .then();

            // When & Then
            StepVerifier.create(setup.thenMany(jwtTokenRepository.findActiveTokensByUserId(testUserId)))
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle tokens near expiry boundary")
        void shouldHandleTokensNearExpiryBoundary() {
            // Given - Token that expires in a very short time
            Instant now = Instant.now();
            JwtToken shortLivedToken = JwtToken.builder()
                    .userId(testUserId)
                    .tokenHash("short-lived-token")
                    .tokenType(JwtToken.TokenType.ACCESS)
                    .issuedAt(now.minus(1, ChronoUnit.HOURS))
                    .expiresAt(now.plus(100, ChronoUnit.MILLIS)) // Very short expiry - 100ms
                    .isRevoked(false)
                    .build();

            // When & Then - Save token and immediately check it's active (before expiry)
            Instant beforeExpiry = now.plus(50, ChronoUnit.MILLIS); // 50ms after now, still before expiry
            StepVerifier.create(
                    jwtTokenRepository.save(shortLivedToken)
                            .then(((R2dbcJwtTokenRepositoryAdapter) jwtTokenRepository)
                                    .countActiveTokensForUserAt(testUserId, beforeExpiry)))
                    .expectNext(1L)
                    .verifyComplete();

            // Check it's now expired (after expiry time)
            Instant afterExpiry = now.plus(200, ChronoUnit.MILLIS); // 200ms after now, well after expiry
            StepVerifier.create(
                    ((R2dbcJwtTokenRepositoryAdapter) jwtTokenRepository)
                            .countActiveTokensForUserAt(testUserId, afterExpiry))
                    .expectNext(0L)
                    .verifyComplete();
        }
    }
}
