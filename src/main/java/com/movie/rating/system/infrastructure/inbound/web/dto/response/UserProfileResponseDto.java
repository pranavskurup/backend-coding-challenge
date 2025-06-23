package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing a user profile response.
 * Contains all user information that can be safely exposed via API.
 */
public record UserProfileResponseDto(
        
        @JsonProperty("id")
        UUID id,
        
        @JsonProperty("username")
        String username,
        
        @JsonProperty("email")
        String email,
        
        @JsonProperty("first_name")
        String firstName,
        
        @JsonProperty("last_name")
        String lastName,
        
        @JsonProperty("full_name")
        String fullName,
        
        @JsonProperty("is_active")
        Boolean isActive,
        
        @JsonProperty("created_at")
        Instant createdAt,
        
        @JsonProperty("updated_at")
        Instant updatedAt,
        
        @JsonProperty("deactivated_at")
        Instant deactivatedAt
) {
}
