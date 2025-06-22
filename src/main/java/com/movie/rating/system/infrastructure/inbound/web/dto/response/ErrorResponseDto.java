package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response DTO for API errors.
 * Provides consistent error information to clients.
 */
@Schema(description = "Standard error response format")
public record ErrorResponseDto(

        @Schema(description = "Error type identifier", example = "VALIDATION_ERROR")
        @JsonProperty("error")
        String error,

        @Schema(description = "Human-readable error message", example = "Invalid request data provided")
        @JsonProperty("message")
        String message,

        @Schema(description = "Timestamp when the error occurred", example = "2023-12-01T10:30:00.000Z")
        @JsonProperty("timestamp")
        Instant timestamp,

        @Schema(description = "API path where the error occurred", example = "/api/v1/movies")
        @JsonProperty("path")
        String path,

        @Schema(description = "HTTP status code", example = "400")
        @JsonProperty("status")
        Integer status,

        @Schema(description = "Additional error details and validation information")
        @JsonProperty("details")
        Map<String, Object> details
) {

    public static ErrorResponseDto of(String error, String message, String path, Integer status) {
        return new ErrorResponseDto(error, message, Instant.now(), path, status, null);
    }

    public static ErrorResponseDto of(String error, String message, String path, Integer status, Map<String, Object> details) {
        return new ErrorResponseDto(error, message, Instant.now(), path, status, details);
    }
}
