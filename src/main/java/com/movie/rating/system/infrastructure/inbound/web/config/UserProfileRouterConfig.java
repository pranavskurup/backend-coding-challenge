package com.movie.rating.system.infrastructure.inbound.web.config;

import com.movie.rating.system.infrastructure.inbound.web.handler.UserProfileHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Router configuration for user profile management endpoints.
 * Defines all routes for user profile operations.
 */
@Configuration
public class UserProfileRouterConfig {

    /**
     * Configure routes for user profile operations.
     *
     * @param userProfileHandler the handler for user profile operations
     * @return RouterFunction for user profile routes
     */
    @Bean
    public RouterFunction<ServerResponse> userProfileRoutes(UserProfileHandler userProfileHandler) {
        return RouterFunctions.route()
                // Get user profile by ID
                .GET("/api/v1/users/{userId}/profile",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::getUserProfile)
                
                // Update user profile
                .PUT("/api/v1/users/{userId}/profile",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        userProfileHandler::updateUserProfile)
                
                // Change password
                .POST("/api/v1/users/{userId}/change-password",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        userProfileHandler::changePassword)
                
                // Deactivate user
                .DELETE("/api/v1/users/{userId}",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::deactivateUser)
                
                // Reactivate user
                .POST("/api/v1/users/{userId}/reactivate",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::reactivateUser)
                
                // Get all active users (admin operation)
                .GET("/api/v1/users",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::getAllActiveUsers)
                
                // Search users by username pattern
                .GET("/api/v1/users/search",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::searchUsersByUsername)
                
                .build();
    }
}
