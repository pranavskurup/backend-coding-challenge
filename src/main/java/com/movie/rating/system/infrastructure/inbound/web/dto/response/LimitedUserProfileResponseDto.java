package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * DTO representing a limited user profile response for other users.
 * Contains only public information that can be safely exposed to other users.
 */
public record LimitedUserProfileResponseDto(
        
        @JsonProperty("id")
        UUID id,
        
        @JsonProperty("username")
        String username,
        
        @JsonProperty("first_name")
        String firstName,
        
        @JsonProperty("last_name")
        String lastName,
        
        @JsonProperty("full_name")
        String fullName
) {
}
