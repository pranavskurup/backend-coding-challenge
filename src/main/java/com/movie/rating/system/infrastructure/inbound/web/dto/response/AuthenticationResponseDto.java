package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for authentication response containing JWT tokens and user info
 */
public record AuthenticationResponseDto(
        @JsonProperty("access_token")
        String accessToken,
        
        @JsonProperty("refresh_token")
        String refreshToken,
        
        @JsonProperty("token_type")
        String tokenType,
        
        @JsonProperty("expires_in")
        long expiresIn,
        
        @JsonProperty("user")
        UserInfo user
) {
    /**
     * Nested user information
     */
    public record UserInfo(
            UUID id,
            String username,
            String email,
            @JsonProperty("first_name")
            String firstName,
            @JsonProperty("last_name")
            String lastName,
            @JsonProperty("is_active")
            boolean isActive,
            @JsonProperty("created_at")
            Instant createdAt
    ) {}
    
    /**
     * Factory method for successful authentication
     */
    public static AuthenticationResponseDto of(String accessToken, String refreshToken, 
                                             long expiresIn, UserInfo user) {
        return new AuthenticationResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                user
        );
    }
}
