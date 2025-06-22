package com.movie.rating.system.infrastructure.inbound.web.config;

import com.movie.rating.system.infrastructure.inbound.web.handler.UserHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
 * Configuration for user-related web routes using Spring WebFlux functional endpoints.
 * Defines all HTTP routes for user operations including registration and availability checks.
 */
@Configuration
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User registration and availability check operations")
public class UserRouterConfig {

    private final UserHandler userHandler;

    /**
     * Configures user-related routes.
     *
     * @return RouterFunction containing all user routes
     */
    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/api/v1/auth/register",
            method = RequestMethod.POST,
            operation = @Operation(
                operationId = "registerUser",
                summary = "Register new user",
                description = "Register a new user account with username, email, and password.",
                tags = {"User Management - Public"},
                responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid registration data"),
                    @ApiResponse(responseCode = "409", description = "Username or email already exists")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/users/check/username",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "checkUsernameAvailability",
                summary = "Check username availability",
                description = "Check if a username is available for registration.",
                tags = {"User Management - Public"},
                parameters = @Parameter(
                    name = "username", 
                    in = ParameterIn.QUERY, 
                    description = "Username to check availability for", 
                    required = true,
                    example = "john_doe"
                ),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Username availability checked"),
                    @ApiResponse(responseCode = "400", description = "Invalid username format")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/users/check/email",
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "checkEmailAvailability",
                summary = "Check email availability",
                description = "Check if an email address is available for registration.",
                tags = {"User Management - Public"},
                parameters = @Parameter(
                    name = "email", 
                    in = ParameterIn.QUERY, 
                    description = "Email address to check availability for", 
                    required = true,
                    example = "john.doe@example.com"
                ),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Email availability checked"),
                    @ApiResponse(responseCode = "400", description = "Invalid email format")
                }
            )
        )
    })
    public RouterFunction<ServerResponse> userRoutes() {
        return RouterFunctions.route()
                // User registration
                .POST("/api/v1/auth/register",
                      accept(MediaType.APPLICATION_JSON)
                          .and(contentType(MediaType.APPLICATION_JSON)),
                      userHandler::registerUser)

                // Username availability check
                .GET("/api/v1/users/check/username",
                     accept(MediaType.APPLICATION_JSON),
                     userHandler::checkUsernameAvailability)

                // Email availability check
                .GET("/api/v1/users/check/email",
                     accept(MediaType.APPLICATION_JSON),
                     userHandler::checkEmailAvailability)

                .build();
    }
}
