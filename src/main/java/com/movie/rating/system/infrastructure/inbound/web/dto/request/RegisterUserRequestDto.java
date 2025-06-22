package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration.
 * Contains validation constraints for user input.
 */
@Schema(description = "User registration request")
public record RegisterUserRequestDto(

        @Schema(description = "Unique username for the account", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
        @JsonProperty("username")
        String username,

        @Schema(description = "Email address for the account", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        @JsonProperty("email")
        String email,

        @Schema(description = "Password for the account (minimum 8 characters)", example = "securePassword123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @JsonProperty("password")
        String password,

        @Schema(description = "User's first name", example = "John")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @JsonProperty("first_name")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @JsonProperty("last_name")
        String lastName
) {
}
