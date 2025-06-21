package com.movie.rating.system.infrastructure.outbound.security;

import com.movie.rating.system.domain.entity.JwtToken;
import com.movie.rating.system.domain.exception.InvalidTokenException;
import com.movie.rating.system.domain.port.outbound.JwtTokenRepository;
import com.movie.rating.system.domain.port.outbound.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * JWT token service implementation using JJWT library with blacklisting support
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JjwtTokenService implements JwtTokenService {

    private final JwtTokenRepository jwtTokenRepository;
    private final SecretKey secretKey;
    private final String issuer;

    public JjwtTokenService(JwtTokenRepository jwtTokenRepository,
                           @Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}") String secret,
                           @Value("${app.jwt.issuer:movie-rating-system}") String issuer) {
        this.jwtTokenRepository = jwtTokenRepository;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.issuer = issuer;
    }

    @Override
    public Mono<String> generateToken(UUID userId, String username, String email, Duration duration) {
        return generateToken(userId, username, email, duration, Map.of());
    }

    @Override
    public Mono<String> generateToken(UUID userId, String username, String email, Duration duration, Map<String, Object> customClaims) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(duration);

            Map<String, Object> claims = new HashMap<>(customClaims);
            claims.put("user_id", userId.toString());
            claims.put("username", username);
            claims.put("email", email);

            String token = Jwts.builder()
                    .subject(userId.toString())
                    .issuer(issuer)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiry))
                    .claims(claims)
                    .signWith(secretKey)
                    .compact();

            // Determine token type from custom claims
            JwtToken.TokenType tokenType = "refresh".equals(customClaims.get("token_type")) 
                    ? JwtToken.TokenType.REFRESH 
                    : JwtToken.TokenType.ACCESS;

            // Save token to database for blacklisting
            JwtToken jwtTokenEntity = JwtToken.builder()
                    .userId(userId)
                    .tokenHash(hashToken(token))
                    .tokenType(tokenType)
                    .issuedAt(now)
                    .expiresAt(expiry)
                    .isRevoked(false)
                    .build();

            return jwtTokenRepository.save(jwtTokenEntity)
                    .then(Mono.just(token))
                    .doOnSuccess(t -> log.debug("Generated and saved JWT token for user: {}", username))
                    .doOnError(error -> log.error("Failed to save JWT token for user: {}", username, error));
        } catch (Exception e) {
            log.error("Failed to generate JWT token for user: {}", username, e);
            return Mono.error(new InvalidTokenException("Failed to generate token", e));
        }
    }

    @Override
    public Mono<TokenClaims> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.get("user_id", String.class));
            String username = claims.get("username", String.class);
            String email = claims.get("email", String.class);
            Instant issuedAt = claims.getIssuedAt().toInstant();
            Instant expiresAt = claims.getExpiration().toInstant();

            // Extract custom claims (exclude standard and our known claims)
            Map<String, Object> customClaims = new HashMap<>(claims);
            customClaims.remove("sub");
            customClaims.remove("iss");
            customClaims.remove("iat");
            customClaims.remove("exp");
            customClaims.remove("user_id");
            customClaims.remove("username");
            customClaims.remove("email");

            TokenClaims tokenClaims = new TokenClaims(userId, username, email, issuedAt, expiresAt, customClaims);
            
            log.debug("Successfully validated JWT token for user: {}", username);
            return Mono.just(tokenClaims);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return Mono.error(new InvalidTokenException("Token has expired"));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return Mono.error(new InvalidTokenException("Invalid token"));
        }
    }

    @Override
    public Mono<TokenClaims> validateTokenWithBlacklist(String token) {
        String tokenHash = hashToken(token);
        
        return jwtTokenRepository.isTokenRevoked(tokenHash)
                .flatMap(isRevoked -> {
                    if (isRevoked) {
                        log.warn("Token is blacklisted/revoked");
                        return Mono.error(new InvalidTokenException("Token has been revoked"));
                    }
                    return validateToken(token);
                })
                .doOnSuccess(claims -> log.debug("Token validated and not blacklisted"))
                .doOnError(error -> log.warn("Token validation failed: {}", error.getMessage()));
    }

    @Override
    public Mono<UUID> extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.get("user_id", String.class));
            return Mono.just(userId);
        } catch (Exception e) {
            log.warn("Failed to extract user ID from token: {}", e.getMessage());
            return Mono.error(new InvalidTokenException("Cannot extract user ID from token"));
        }
    }

    @Override
    public Mono<Boolean> isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            boolean expired = claims.getExpiration().before(new Date());
            return Mono.just(expired);
        } catch (ExpiredJwtException e) {
            return Mono.just(true);
        } catch (Exception e) {
            log.warn("Failed to check token expiry: {}", e.getMessage());
            return Mono.error(new InvalidTokenException("Cannot check token expiry"));
        }
    }

    @Override
    public Mono<String> refreshToken(String token, Duration newDuration) {
        return validateToken(token)
                .flatMap(claims -> {
                    // Blacklist the old token
                    return blacklistToken(token, "Token refreshed")
                            .then(generateToken(
                                    claims.userId(),
                                    claims.username(),
                                    claims.email(),
                                    newDuration,
                                    claims.customClaims()
                            ));
                })
                .doOnSuccess(newToken -> log.debug("Refreshed JWT token"))
                .doOnError(error -> log.error("Failed to refresh token: {}", error.getMessage()));
    }

    @Override
    public Mono<Void> blacklistToken(String token, String reason) {
        String tokenHash = hashToken(token);
        
        return jwtTokenRepository.revokeByTokenHash(tokenHash, reason)
                .then()
                .doOnSuccess(v -> log.debug("Successfully blacklisted token with reason: {}", reason))
                .doOnError(error -> log.error("Failed to blacklist token: {}", error.getMessage()));
    }

    @Override
    public String hashToken(String token) {
        if (token == null) {
            token = ""; // Convert null to empty string for consistent hashing
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
