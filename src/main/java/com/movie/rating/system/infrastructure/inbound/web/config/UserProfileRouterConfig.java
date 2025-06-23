package com.movie.rating.system.infrastructure.inbound.web.config;

import com.movie.rating.system.infrastructure.inbound.web.handler.UserProfileHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Router configuration for user profile management endpoints.
 * Defines all routes for user profile operations.
 */
@Configuration
@Tag(name = "User Profile", description = "User profile management and administration operations")
public class UserProfileRouterConfig {

    /**
     * Configure routes for user profile operations.
     *
     * @param userProfileHandler the handler for user profile operations
     * @return RouterFunction for user profile routes
     */
    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/api/v1/users/{userId}/profile",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getUserProfile",
                summary = "Get user profile",
                description = "Retrieve user profile information by user ID. Users can only access their own profile.",
                tags = {"User Profile - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "userId", in = ParameterIn.PATH, description = "User ID", required = true),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Profile retrieved successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.UserProfileResponseDto.class)
                        )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - can only access own profile"),
                    @ApiResponse(responseCode = "404", description = "User not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/users/{userId}/profile",
            method = RequestMethod.PUT,
            operation = @Operation(
                operationId = "updateUserProfile",
                summary = "Update user profile",
                description = "Update user profile information. Users can only update their own profile.",
                tags = {"User Profile - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "userId", in = ParameterIn.PATH, description = "User ID", required = true),
                requestBody = @RequestBody(
                    description = "Updated profile information",
                    required = true,
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateUserProfileRequestDto.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Profile updated successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.UserProfileResponseDto.class)
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid profile data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - can only update own profile"),
                    @ApiResponse(responseCode = "404", description = "User not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/users/{userId}/change-password",
            method = RequestMethod.POST,
            operation = @Operation(
                operationId = "changePassword",
                summary = "Change user password",
                description = "Change the user's password. Users can only change their own password.",
                tags = {"User Profile - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "userId", in = ParameterIn.PATH, description = "User ID", required = true),
                requestBody = @RequestBody(
                    description = "Password change request",
                    required = true,
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.request.ChangePasswordRequestDto.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Password changed successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.OperationSuccessResponseDto.class)
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid password data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - can only change own password"),
                    @ApiResponse(responseCode = "404", description = "User not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/users/{userId}",
            method = RequestMethod.DELETE,
            operation = @Operation(
                operationId = "deactivateUser",
                summary = "Deactivate user account",
                description = "Soft delete/deactivate a user account. Users can only deactivate their own account.",
                tags = {"User Profile - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "userId", in = ParameterIn.PATH, description = "User ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "204", description = "User deactivated successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - can only deactivate own account"),
                    @ApiResponse(responseCode = "404", description = "User not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/users/{userId}/reactivate",
            method = RequestMethod.POST,
            operation = @Operation(
                operationId = "reactivateUser",
                summary = "Reactivate user account",
                description = "Reactivate a previously deactivated user account. Admin operation.",
                tags = {"User Profile - Admin"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "userId", in = ParameterIn.PATH, description = "User ID", required = true),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "User reactivated successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.UserResponseDto.class)
                        )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - admin only"),
                    @ApiResponse(responseCode = "404", description = "User not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/users",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getAllActiveUsers",
                summary = "Get all active users",
                description = "Retrieve all active users in the system. Admin operation.",
                tags = {"User Profile - Admin"},
                security = @SecurityRequirement(name = "bearerAuth"),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Users retrieved successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.UserResponseDto.class)
                        )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - admin only")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/users/search",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "searchUsersByUsername",
                summary = "Search users by username",
                description = "Search for users by username pattern. Admin operation.",
                tags = {"User Profile - Admin"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(
                    name = "username", 
                    in = ParameterIn.QUERY, 
                    description = "Username pattern to search for", 
                    required = true
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Search completed",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.UserResponseDto.class)
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - admin only")
                }
            )
        )
    })
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
