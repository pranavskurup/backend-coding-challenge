package com.movie.rating.system.infrastructure.inbound.web.config;

import com.movie.rating.system.infrastructure.inbound.web.handler.UserHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
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
public class UserRouterConfig {

    private final UserHandler userHandler;

    /**
     * Configures user-related routes.
     *
     * @return RouterFunction containing all user routes
     */
    @Bean
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
