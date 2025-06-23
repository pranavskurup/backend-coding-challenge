package com.movie.rating.system.infrastructure.inbound.web.config;

import com.movie.rating.system.infrastructure.inbound.web.handler.AuthenticationHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Configuration for authentication routes
 */
@Configuration
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and authorization operations")
public class AuthenticationRouterConfig {

    private final AuthenticationHandler authenticationHandler;

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/api/v1/auth/login",
            method = RequestMethod.POST,
            operation = @Operation(
                operationId = "login",
                summary = "User login",
                description = "Authenticate user with username/email and password. Returns JWT access token and refresh token.",
                tags = {"Authentication - Public"},
                requestBody = @RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.request.LoginRequestDto.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Login successful",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.AuthenticationResponseDto.class)
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/auth/refresh",
            method = RequestMethod.POST,
            operation = @Operation(
                operationId = "refreshToken",
                summary = "Refresh access token",
                description = "Generate a new access token using a valid refresh token.",
                tags = {"Authentication - Public"},
                requestBody = @RequestBody(
                    description = "Refresh token request",
                    required = true,
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.request.RefreshTokenRequestDto.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Token refreshed successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.AuthenticationResponseDto.class)
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
                    @ApiResponse(responseCode = "401", description = "Refresh token expired")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/auth/logout",
            method = RequestMethod.POST,
            operation = @Operation(
                operationId = "logout",
                summary = "User logout",
                description = "Invalidate the current user session and tokens.",
                tags = {"Authentication - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Logout successful",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.OperationSuccessResponseDto.class)
                        )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                }
            )
        )
    })
    public RouterFunction<ServerResponse> authenticationRoutes() {
        return route()
                .path("/api/v1/auth", builder -> builder
                        .POST("/login", authenticationHandler::login)
                        .POST("/refresh", authenticationHandler::refreshToken)
                        .POST("/logout", authenticationHandler::logout)
                )
                .build();
    }
}
