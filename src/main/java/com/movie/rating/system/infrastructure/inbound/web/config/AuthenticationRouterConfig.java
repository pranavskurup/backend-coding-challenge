package com.movie.rating.system.infrastructure.inbound.web.config;

import com.movie.rating.system.infrastructure.inbound.web.handler.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Configuration for authentication routes
 */
@Configuration
@RequiredArgsConstructor
public class AuthenticationRouterConfig {

    private final AuthenticationHandler authenticationHandler;

    @Bean
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
