package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response DTO for API errors.
 * Provides consistent error information to clients.
 */
public record ErrorResponseDto(

        @JsonProperty("error")
        String error,

        @JsonProperty("message")
        String message,

        @JsonProperty("timestamp")
        Instant timestamp,

        @JsonProperty("path")
        String path,

        @JsonProperty("status")
        Integer status,

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
