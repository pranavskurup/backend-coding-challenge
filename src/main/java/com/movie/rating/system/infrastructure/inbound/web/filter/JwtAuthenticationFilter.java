package com.movie.rating.system.infrastructure.inbound.web.filter;

import com.movie.rating.system.domain.port.outbound.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * JWT Authentication Filter for validating JWT tokens in requests.
 * This filter extracts JWT tokens from the Authorization header and validates them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String EMAIL_ATTRIBUTE = "email";

    private final JwtTokenService jwtTokenService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // Skip JWT validation for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        return extractTokenFromRequest(exchange)
                .flatMap(this::validateToken)
                .flatMap(claims -> {
                    // Add user information to exchange attributes
                    exchange.getAttributes().put(USER_ID_ATTRIBUTE, claims.userId());
                    exchange.getAttributes().put(USERNAME_ATTRIBUTE, claims.username());
                    exchange.getAttributes().put(EMAIL_ATTRIBUTE, claims.email());
                    
                    log.debug("Authenticated user: {} ({})", claims.username(), claims.userId());
                    return chain.filter(exchange);
                })
                .onErrorResume(throwable -> {
                    log.warn("JWT authentication failed for path {}: {}", path, throwable.getMessage());
                    return handleAuthenticationFailure(exchange);
                });
    }

    /**
     * Extract JWT token from Authorization header
     */
    private Mono<String> extractTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Mono.error(new RuntimeException("Missing or invalid Authorization header"));
        }
        
        String token = authHeader.substring(BEARER_PREFIX.length());
        if (token.isEmpty()) {
            return Mono.error(new RuntimeException("Empty JWT token"));
        }
        
        return Mono.just(token);
    }

    /**
     * Validate JWT token using the token service
     */
    private Mono<JwtTokenService.TokenClaims> validateToken(String token) {
        return jwtTokenService.validateTokenWithBlacklist(token)
                .doOnSuccess(claims -> log.debug("Token validated successfully for user: {}", claims.username()))
                .doOnError(error -> log.debug("Token validation failed: {}", error.getMessage()));
    }

    /**
     * Handle authentication failure by returning 401 Unauthorized
     */
    private Mono<Void> handleAuthenticationFailure(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String errorBody = """
                {
                    "error": "UNAUTHORIZED",
                    "message": "Authentication required",
                    "timestamp": "%s",
                    "path": "%s",
                    "status": 401
                }
                """.formatted(
                java.time.Instant.now(),
                exchange.getRequest().getURI().getPath()
        );
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorBody.getBytes()))
        );
    }

    /**
     * Check if the endpoint is public and doesn't require authentication
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/v1/users/register") ||
               path.startsWith("/api/v1/users/check/") ||
               path.equals("/health") ||
               path.equals("/actuator/health") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars/");
    }

    /**
     * Utility method to get authenticated user ID from exchange
     */
    public static UUID getAuthenticatedUserId(ServerWebExchange exchange) {
        return (UUID) exchange.getAttributes().get(USER_ID_ATTRIBUTE);
    }

    /**
     * Utility method to get authenticated username from exchange
     */
    public static String getAuthenticatedUsername(ServerWebExchange exchange) {
        return (String) exchange.getAttributes().get(USERNAME_ATTRIBUTE);
    }

    /**
     * Utility method to get authenticated user email from exchange
     */
    public static String getAuthenticatedUserEmail(ServerWebExchange exchange) {
        return (String) exchange.getAttributes().get(EMAIL_ATTRIBUTE);
    }
}
