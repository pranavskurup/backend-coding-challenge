package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for changing user password.
 */
public record ChangePasswordRequestDto(
        
        @NotBlank(message = "Current password is required")
        @JsonProperty("current_password")
        String currentPassword,
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 255, message = "New password must be between 8 and 255 characters")
        @JsonProperty("new_password")
        String newPassword
) {
}
