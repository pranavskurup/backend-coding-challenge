package com.movie.rating.system.infrastructure.outbound.security;

import com.movie.rating.system.domain.entity.JwtToken;
import com.movie.rating.system.domain.exception.InvalidTokenException;
import com.movie.rating.system.domain.port.outbound.JwtTokenRepository;
import com.movie.rating.system.domain.port.outbound.JwtTokenService.TokenClaims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for JjwtTokenService.
 * Tests JWT token generation, validation, blacklisting, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JJWT Token Service Tests")
class JjwtTokenServiceTest {

    @Mock
    private JwtTokenRepository jwtTokenRepository;

    private JjwtTokenService jwtTokenService;
    private final String testSecret = "mySecretKey123456789012345678901234567890";
    private final String testIssuer = "test-movie-rating-system";
    private final UUID testUserId = UUID.randomUUID();
    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtTokenService = new JjwtTokenService(jwtTokenRepository, testSecret, testIssuer);
        
        // Default mock behavior - using lenient to avoid unnecessary stubbing errors
        lenient().when(jwtTokenRepository.save(any(JwtToken.class)))
                .thenReturn(Mono.just(createMockJwtToken()));
        lenient().when(jwtTokenRepository.isTokenRevoked(anyString()))
                .thenReturn(Mono.just(false));
        lenient().when(jwtTokenRepository.revokeByTokenHash(anyString(), anyString()))
                .thenReturn(Mono.just(createMockJwtToken()));
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid JWT token with basic claims")
        void shouldGenerateValidJwtTokenWithBasicClaims() {
            // Given
            Duration duration = Duration.ofHours(1);

            // When & Then
            StepVerifier.create(jwtTokenService.generateToken(testUserId, testUsername, testEmail, duration))
                    .assertNext(token -> {
                        assertThat(token).isNotNull();
                        assertThat(token).isNotEmpty();
                        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
                        
                        // Verify token can be parsed
                        TokenClaims claims = parseTokenClaims(token);
                        assertThat(claims.userId()).isEqualTo(testUserId);
                        assertThat(claims.username()).isEqualTo(testUsername);
                        assertThat(claims.email()).isEqualTo(testEmail);
                        assertThat(claims.expiresAt()).isAfter(Instant.now());
                    })
                    .verifyComplete();

            // Verify repository interaction
            ArgumentCaptor<JwtToken> tokenCaptor = ArgumentCaptor.forClass(JwtToken.class);
            verify(jwtTokenRepository).save(tokenCaptor.capture());
            
            JwtToken savedToken = tokenCaptor.getValue();
            assertThat(savedToken.getUserId()).isEqualTo(testUserId);
            assertThat(savedToken.getTokenType()).isEqualTo(JwtToken.TokenType.ACCESS);
            assertThat(savedToken.isRevoked()).isFalse();
        }

        @Test
        @DisplayName("Should generate JWT token with custom claims")
        void shouldGenerateJwtTokenWithCustomClaims() {
            // Given
            Duration duration = Duration.ofMinutes(30);
            Map<String, Object> customClaims = Map.of(
                    "role", "ADMIN",
                    "permissions", "READ,WRITE",
                    "token_type", "refresh"
            );

            // When & Then
            StepVerifier.create(jwtTokenService.generateToken(testUserId, testUsername, testEmail, duration, customClaims))
                    .assertNext(token -> {
                        assertThat(token).isNotNull();
                        
                        TokenClaims claims = parseTokenClaims(token);
                        assertThat(claims.customClaims()).containsEntry("role", "ADMIN");
                        assertThat(claims.customClaims()).containsEntry("permissions", "READ,WRITE");
                        assertThat(claims.customClaims()).containsEntry("token_type", "refresh");
                    })
                    .verifyComplete();

            // Verify refresh token type is saved
            ArgumentCaptor<JwtToken> tokenCaptor = ArgumentCaptor.forClass(JwtToken.class);
            verify(jwtTokenRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().getTokenType()).isEqualTo(JwtToken.TokenType.REFRESH);
        }

        @Test
        @DisplayName("Should handle repository save failure")
        void shouldHandleRepositorySaveFailure() {
            // Given
            when(jwtTokenRepository.save(any(JwtToken.class)))
                    .thenReturn(Mono.error(new RuntimeException("Database error")));

            // When & Then
            StepVerifier.create(jwtTokenService.generateToken(testUserId, testUsername, testEmail, Duration.ofHours(1)))
                    .expectError(RuntimeException.class)
                    .verify();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should handle invalid username")
        void shouldHandleInvalidUsername(String invalidUsername) {
            // When & Then
            StepVerifier.create(jwtTokenService.generateToken(testUserId, invalidUsername, testEmail, Duration.ofHours(1)))
                    .assertNext(token -> {
                        assertThat(token).isNotNull(); // Service should still generate token
                        TokenClaims claims = parseTokenClaims(token);
                        assertThat(claims.username()).isEqualTo(invalidUsername);
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate valid JWT token")
        void shouldValidateValidJwtToken() {
            // Given
            String token = createValidToken(Duration.ofHours(1));

            // When & Then
            StepVerifier.create(jwtTokenService.validateToken(token))
                    .assertNext(claims -> {
                        assertThat(claims.userId()).isEqualTo(testUserId);
                        assertThat(claims.username()).isEqualTo(testUsername);
                        assertThat(claims.email()).isEqualTo(testEmail);
                        assertThat(claims.expiresAt()).isAfter(Instant.now());
                        assertThat(claims.issuedAt()).isBefore(Instant.now());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should reject expired JWT token")
        void shouldRejectExpiredJwtToken() {
            // Given - Token that expired 1 hour ago
            String expiredToken = createValidToken(Duration.ofHours(-1));

            // When & Then
            StepVerifier.create(jwtTokenService.validateToken(expiredToken))
                    .expectError(InvalidTokenException.class)
                    .verify();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"invalid.token", "header.payload", "invalid"})
        @DisplayName("Should reject malformed tokens")
        void shouldRejectMalformedTokens(String malformedToken) {
            // When & Then
            StepVerifier.create(jwtTokenService.validateToken(malformedToken))
                    .expectError(InvalidTokenException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should reject token with wrong signature")
        void shouldRejectTokenWithWrongSignature() {
            // Given - Token signed with different secret
            SecretKey wrongKey = Keys.hmacShaKeyFor("differentSecret123456789012345678901234567890".getBytes());
            String wrongToken = Jwts.builder()
                    .subject(testUserId.toString())
                    .issuer(testIssuer)
                    .issuedAt(new Date())
                    .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .claim("user_id", testUserId.toString())
                    .claim("username", testUsername)
                    .claim("email", testEmail)
                    .signWith(wrongKey)
                    .compact();

            // When & Then
            StepVerifier.create(jwtTokenService.validateToken(wrongToken))
                    .expectError(InvalidTokenException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Token Validation with Blacklist Tests")
    class TokenValidationWithBlacklistTests {

        @Test
        @DisplayName("Should validate non-blacklisted token")
        void shouldValidateNonBlacklistedToken() {
            // Given
            String token = createValidToken(Duration.ofHours(1));
            when(jwtTokenRepository.isTokenRevoked(anyString())).thenReturn(Mono.just(false));

            // When & Then
            StepVerifier.create(jwtTokenService.validateTokenWithBlacklist(token))
                    .assertNext(claims -> {
                        assertThat(claims.userId()).isEqualTo(testUserId);
                        assertThat(claims.username()).isEqualTo(testUsername);
                    })
                    .verifyComplete();

            verify(jwtTokenRepository).isTokenRevoked(anyString());
        }

        @Test
        @DisplayName("Should reject blacklisted token")
        void shouldRejectBlacklistedToken() {
            // Given
            String token = createValidToken(Duration.ofHours(1));
            when(jwtTokenRepository.isTokenRevoked(anyString())).thenReturn(Mono.just(true));

            // When & Then
            StepVerifier.create(jwtTokenService.validateTokenWithBlacklist(token))
                    .expectErrorMatches(error -> 
                        error instanceof InvalidTokenException &&
                        error.getMessage().contains("revoked"))
                    .verify();

            verify(jwtTokenRepository).isTokenRevoked(anyString());
        }

        @Test
        @DisplayName("Should handle repository error during blacklist check")
        void shouldHandleRepositoryErrorDuringBlacklistCheck() {
            // Given
            String token = createValidToken(Duration.ofHours(1));
            when(jwtTokenRepository.isTokenRevoked(anyString()))
                    .thenReturn(Mono.error(new RuntimeException("Database error")));

            // When & Then
            StepVerifier.create(jwtTokenService.validateTokenWithBlacklist(token))
                    .expectError(RuntimeException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Token Utility Tests")
    class TokenUtilityTests {

        @Test
        @DisplayName("Should extract user ID from valid token")
        void shouldExtractUserIdFromValidToken() {
            // Given
            String token = createValidToken(Duration.ofHours(1));

            // When & Then
            StepVerifier.create(jwtTokenService.extractUserId(token))
                    .expectNext(testUserId)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should fail to extract user ID from invalid token")
        void shouldFailToExtractUserIdFromInvalidToken() {
            // When & Then
            StepVerifier.create(jwtTokenService.extractUserId("invalid.token"))
                    .expectError(InvalidTokenException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should correctly identify non-expired token")
        void shouldCorrectlyIdentifyNonExpiredToken() {
            // Given
            String token = createValidToken(Duration.ofHours(1));

            // When & Then
            StepVerifier.create(jwtTokenService.isTokenExpired(token))
                    .expectNext(false)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should correctly identify expired token")
        void shouldCorrectlyIdentifyExpiredToken() {
            // Given
            String expiredToken = createValidToken(Duration.ofHours(-1));

            // When & Then
            StepVerifier.create(jwtTokenService.isTokenExpired(expiredToken))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle invalid token when checking expiry")
        void shouldHandleInvalidTokenWhenCheckingExpiry() {
            // When & Then
            StepVerifier.create(jwtTokenService.isTokenExpired("invalid.token"))
                    .expectError(InvalidTokenException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Token Refresh Tests")
    class TokenRefreshTests {

        @Test
        @DisplayName("Should refresh valid token successfully")
        void shouldRefreshValidTokenSuccessfully() {
            // Given
            String originalToken = createValidToken(Duration.ofHours(1));
            Duration newDuration = Duration.ofHours(2);
            when(jwtTokenRepository.revokeByTokenHash(anyString(), eq("Token refreshed")))
                    .thenReturn(Mono.just(createMockJwtToken()));

            // When & Then
            StepVerifier.create(jwtTokenService.refreshToken(originalToken, newDuration))
                    .assertNext(newToken -> {
                        assertThat(newToken).isNotNull();
                        assertThat(newToken).isNotEqualTo(originalToken);
                        
                        TokenClaims claims = parseTokenClaims(newToken);
                        assertThat(claims.userId()).isEqualTo(testUserId);
                        assertThat(claims.username()).isEqualTo(testUsername);
                        assertThat(claims.email()).isEqualTo(testEmail);
                    })
                    .verifyComplete();

            // Verify original token was blacklisted
            verify(jwtTokenRepository).revokeByTokenHash(anyString(), eq("Token refreshed"));
            // Verify new token was saved (only once for the new token)
            verify(jwtTokenRepository, times(1)).save(any(JwtToken.class));
        }

        @Test
        @DisplayName("Should fail to refresh invalid token")
        void shouldFailToRefreshInvalidToken() {
            // When & Then
            StepVerifier.create(jwtTokenService.refreshToken("invalid.token", Duration.ofHours(1)))
                    .expectError(InvalidTokenException.class)
                    .verify();

            // Verify no blacklisting occurred
            verify(jwtTokenRepository, never()).revokeByTokenHash(anyString(), anyString());
        }

        @Test
        @DisplayName("Should fail to refresh expired token")
        void shouldFailToRefreshExpiredToken() {
            // Given
            String expiredToken = createValidToken(Duration.ofHours(-1));

            // When & Then
            StepVerifier.create(jwtTokenService.refreshToken(expiredToken, Duration.ofHours(1)))
                    .expectError(InvalidTokenException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Token Blacklisting Tests")
    class TokenBlacklistingTests {

        @Test
        @DisplayName("Should blacklist token successfully")
        void shouldBlacklistTokenSuccessfully() {
            // Given
            String token = createValidToken(Duration.ofHours(1));
            String reason = "User logout";
            when(jwtTokenRepository.revokeByTokenHash(anyString(), eq(reason)))
                    .thenReturn(Mono.just(createMockJwtToken()));

            // When & Then
            StepVerifier.create(jwtTokenService.blacklistToken(token, reason))
                    .verifyComplete();

            verify(jwtTokenRepository).revokeByTokenHash(anyString(), eq(reason));
        }

        @Test
        @DisplayName("Should handle repository error during blacklisting")
        void shouldHandleRepositoryErrorDuringBlacklisting() {
            // Given
            String token = createValidToken(Duration.ofHours(1));
            when(jwtTokenRepository.revokeByTokenHash(anyString(), anyString()))
                    .thenReturn(Mono.error(new RuntimeException("Database error")));

            // When & Then
            StepVerifier.create(jwtTokenService.blacklistToken(token, "Test reason"))
                    .expectError(RuntimeException.class)
                    .verify();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should handle invalid blacklist reasons")
        void shouldHandleInvalidBlacklistReasons(String invalidReason) {
            // Given
            String token = createValidToken(Duration.ofHours(1));
            when(jwtTokenRepository.revokeByTokenHash(anyString(), eq(invalidReason)))
                    .thenReturn(Mono.just(createMockJwtToken()));

            // When & Then
            StepVerifier.create(jwtTokenService.blacklistToken(token, invalidReason))
                    .verifyComplete();

            verify(jwtTokenRepository).revokeByTokenHash(anyString(), eq(invalidReason));
        }
    }

    @Nested
    @DisplayName("Token Hashing Tests")
    class TokenHashingTests {

        @Test
        @DisplayName("Should hash token consistently")
        void shouldHashTokenConsistently() {
            // Given
            String token = "test.jwt.token";

            // When
            String hash1 = jwtTokenService.hashToken(token);
            String hash2 = jwtTokenService.hashToken(token);

            // Then
            assertThat(hash1).isEqualTo(hash2);
            assertThat(hash1).isNotEqualTo(token);
            assertThat(hash1).hasSize(64); // SHA-256 produces 64-character hex string
        }

        @Test
        @DisplayName("Should produce different hashes for different tokens")
        void shouldProduceDifferentHashesForDifferentTokens() {
            // Given
            String token1 = "test.jwt.token1";
            String token2 = "test.jwt.token2";

            // When
            String hash1 = jwtTokenService.hashToken(token1);
            String hash2 = jwtTokenService.hashToken(token2);

            // Then
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n", "special@#$%chars"})
        @DisplayName("Should handle edge case tokens for hashing")
        void shouldHandleEdgeCaseTokensForHashing(String edgeCaseToken) {
            // When & Then - Should not throw exception
            String hash = jwtTokenService.hashToken(edgeCaseToken);
            assertThat(hash).isNotNull();
            assertThat(hash).hasSize(64);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete token lifecycle")
        void shouldHandleCompleteTokenLifecycle() {
            // Given
            Duration duration = Duration.ofHours(1);
            Map<String, Object> customClaims = Map.of("role", "USER");

            // Generate token
            StepVerifier.create(jwtTokenService.generateToken(testUserId, testUsername, testEmail, duration, customClaims))
                    .assertNext(token -> {
                        assertThat(token).isNotNull();
                        
                        // Validate token
                        StepVerifier.create(jwtTokenService.validateToken(token))
                                .assertNext(claims -> {
                                    assertThat(claims.userId()).isEqualTo(testUserId);
                                    assertThat(claims.customClaims()).containsEntry("role", "USER");
                                })
                                .verifyComplete();
                        
                        // Check token is not expired
                        StepVerifier.create(jwtTokenService.isTokenExpired(token))
                                .expectNext(false)
                                .verifyComplete();
                        
                        // Extract user ID
                        StepVerifier.create(jwtTokenService.extractUserId(token))
                                .expectNext(testUserId)
                                .verifyComplete();
                        
                        // Blacklist token
                        StepVerifier.create(jwtTokenService.blacklistToken(token, "Test blacklist"))
                                .verifyComplete();
                    })
                    .verifyComplete();
        }
    }

    // Helper methods
    private String createValidToken(Duration duration) {
        Instant now = Instant.now();
        Instant expiry = now.plus(duration);
        
        return Jwts.builder()
                .subject(testUserId.toString())
                .issuer(testIssuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("user_id", testUserId.toString())
                .claim("username", testUsername)
                .claim("email", testEmail)
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
                .compact();
    }

    private TokenClaims parseTokenClaims(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.get("user_id", String.class));
            String username = claims.get("username", String.class);
            String email = claims.get("email", String.class);
            Instant issuedAt = claims.getIssuedAt().toInstant();
            Instant expiresAt = claims.getExpiration().toInstant();

            Map<String, Object> customClaims = new HashMap<>(claims);
            customClaims.remove("sub");
            customClaims.remove("iss");
            customClaims.remove("iat");
            customClaims.remove("exp");
            customClaims.remove("user_id");
            customClaims.remove("username");
            customClaims.remove("email");

            return new TokenClaims(userId, username, email, issuedAt, expiresAt, customClaims);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token claims", e);
        }
    }

    private JwtToken createMockJwtToken() {
        return JwtToken.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .tokenHash("mockedHash")
                .tokenType(JwtToken.TokenType.ACCESS)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .isRevoked(false)
                .build();
    }
}
