package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for token refresh request
 */
public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
