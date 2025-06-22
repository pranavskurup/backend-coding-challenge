package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request
 */
@Schema(description = "User login request")
public record LoginRequestDto(
        @Schema(description = "Username or email address", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,
        
        @Schema(description = "User password", example = "securePassword123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        String password
) {
}
