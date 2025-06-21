package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request
 */
public record LoginRequestDto(
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,
        
        @NotBlank(message = "Password is required")
        String password
) {
}
