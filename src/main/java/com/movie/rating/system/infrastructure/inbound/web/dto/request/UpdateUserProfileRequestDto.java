package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating user profile information.
 * All fields are optional - only provided fields will be updated.
 */
public record UpdateUserProfileRequestDto(
        
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        @JsonProperty("email")
        String email,
        
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        @JsonProperty("first_name")
        String firstName,
        
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        @JsonProperty("last_name")
        String lastName
) {
    /**
     * Check if any field is provided for update
     * @return true if at least one field is provided
     */
    public boolean hasAnyUpdate() {
        return email != null || firstName != null || lastName != null;
    }
}
