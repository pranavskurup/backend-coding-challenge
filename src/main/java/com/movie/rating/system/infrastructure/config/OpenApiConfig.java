package com.movie.rating.system.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for the Movie Rating System.
 * Provides comprehensive API documentation with security schemes.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI movieRatingSystemOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development server"),
                        new Server()
                                .url("https://api.movierating.example.com")
                                .description("Production server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", createAPIKeyScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Movie Rating System API")
                .description("""
                        A comprehensive movie rating system built with Spring WebFlux and R2DBC.
                        
                        ## Features
                        - User authentication and profile management
                        - Movie catalog management
                        - Movie rating and review system
                        - Advanced movie recommendations
                        - Real-time statistics and analytics
                        
                        ## API Organization
                        The API endpoints are organized into the following groups based on security requirements:
                        
                        ### Public Endpoints (No Authentication Required)
                        - **Movies - Public**: Browse movies, search, and view basic information
                        - **Ratings - Public**: View ratings and reviews from all users
                        - **Movie Statistics - Public**: Access rating statistics and averages
                        - **Authentication - Public**: User login, registration, and token refresh
                        - **User Management - Public**: Username/email availability checks
                        
                        ### Secured Endpoints (JWT Authentication Required)
                        - **Movies - Secured**: Create, update, and delete movies (user's own movies only)
                        - **Ratings - Secured**: Create, update, and delete ratings (user's own ratings only)
                        - **Authentication - Secured**: User logout and session management
                        - **User Profile - Secured**: User profile management and password changes
                        
                        ### Admin Endpoints (Admin Role Required)
                        - **User Profile - Admin**: User administration and system management operations
                        
                        ## Authentication
                        Secured endpoints require JWT authentication. Use the `/api/v1/auth/login` endpoint to obtain a token,
                        then include it in the Authorization header as `Bearer {token}`.
                        
                        ## Rate Limiting
                        API calls are rate-limited to prevent abuse. Check response headers for current limits.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Movie Rating System Team")
                        .email("support@movierating.example.com")
                        .url("https://github.com/movierating/api"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer token authentication. Format: Bearer {token}");
    }
}
