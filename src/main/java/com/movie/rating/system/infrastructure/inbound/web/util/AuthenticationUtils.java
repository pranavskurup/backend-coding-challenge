package com.movie.rating.system.infrastructure.inbound.web.util;

import com.movie.rating.system.infrastructure.inbound.web.filter.JwtAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Utility class for handling authentication in web handlers.
 * Provides methods to extract authenticated user information and handle authorization.
 */
public class AuthenticationUtils {

    /**
     * Get authenticated user ID from the request
     */
    public static UUID getAuthenticatedUserId(ServerRequest request) {
        return JwtAuthenticationFilter.getAuthenticatedUserId(request.exchange());
    }

    /**
     * Get authenticated username from the request
     */
    public static String getAuthenticatedUsername(ServerRequest request) {
        return JwtAuthenticationFilter.getAuthenticatedUsername(request.exchange());
    }

    /**
     * Get authenticated user email from the request
     */
    public static String getAuthenticatedUserEmail(ServerRequest request) {
        return JwtAuthenticationFilter.getAuthenticatedUserEmail(request.exchange());
    }

    /**
     * Check if the authenticated user is the same as the requested user ID
     */
    public static boolean isAuthenticatedUser(ServerRequest request, UUID requestedUserId) {
        UUID authenticatedUserId = getAuthenticatedUserId(request);
        return authenticatedUserId != null && authenticatedUserId.equals(requestedUserId);
    }

    /**
     * Check if the authenticated user is the same as the requested user ID (from path variable)
     */
    public static boolean isAuthenticatedUser(ServerRequest request, String userIdPathVariable) {
        try {
            UUID requestedUserId = UUID.fromString(request.pathVariable(userIdPathVariable));
            return isAuthenticatedUser(request, requestedUserId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Ensure the authenticated user matches the requested user ID, otherwise return 403 Forbidden
     */
    public static Mono<ServerResponse> ensureUserAccess(ServerRequest request, String userIdPathVariable, 
                                                       Mono<ServerResponse> successResponse) {
        if (!isAuthenticatedUser(request, userIdPathVariable)) {
            return ServerResponse.status(HttpStatus.FORBIDDEN)
                    .bodyValue(createErrorResponse("Access denied", "You can only access your own resources"));
        }
        return successResponse;
    }

    /**
     * Ensure the authenticated user matches the requested user ID, otherwise return 403 Forbidden
     */
    public static Mono<ServerResponse> ensureUserAccess(ServerRequest request, UUID requestedUserId, 
                                                       Mono<ServerResponse> successResponse) {
        if (!isAuthenticatedUser(request, requestedUserId)) {
            return ServerResponse.status(HttpStatus.FORBIDDEN)
                    .bodyValue(createErrorResponse("Access denied", "You can only access your own resources"));
        }
        return successResponse;
    }

    /**
     * Create a standardized error response
     */
    private static Object createErrorResponse(String error, String message) {
        return new ErrorResponse(error, message, java.time.Instant.now());
    }

    /**
     * Simple error response record
     */
    public record ErrorResponse(String error, String message, java.time.Instant timestamp) {}
}
