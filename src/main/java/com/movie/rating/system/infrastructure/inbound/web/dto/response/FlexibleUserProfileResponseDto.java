package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing a flexible user profile response that can contain either
 * full profile data (for own profile) or limited profile data (for other users).
 * This is used for OpenAPI documentation to represent the varying response structure.
 */
@Schema(description = "User profile response - contains full data for own profile, limited data for other users")
public record FlexibleUserProfileResponseDto(
        
        @JsonProperty("id")
        @Schema(description = "User ID", required = true)
        UUID id,
        
        @JsonProperty("username")
        @Schema(description = "Username", required = true)
        String username,
        
        @JsonProperty("first_name")
        @Schema(description = "First name", required = true)
        String firstName,
        
        @JsonProperty("last_name")
        @Schema(description = "Last name", required = true)
        String lastName,
        
        @JsonProperty("full_name")
        @Schema(description = "Full name", required = true)
        String fullName,
        
        @JsonProperty("email")
        @Schema(description = "Email address - only returned for own profile", nullable = true)
        String email,
        
        @JsonProperty("is_active")
        @Schema(description = "Account active status - only returned for own profile", nullable = true)
        Boolean isActive,
        
        @JsonProperty("created_at")
        @Schema(description = "Account creation timestamp - only returned for own profile", nullable = true)
        Instant createdAt,
        
        @JsonProperty("updated_at")
        @Schema(description = "Last update timestamp - only returned for own profile", nullable = true)
        Instant updatedAt,
        
        @JsonProperty("deactivated_at")
        @Schema(description = "Deactivation timestamp - only returned for own profile", nullable = true)
        Instant deactivatedAt
) {
}
