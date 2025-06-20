package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for username/email availability checks.
 */
public record AvailabilityResponseDto(

        @JsonProperty("available")
        Boolean available,

        @JsonProperty("field")
        String field,

        @JsonProperty("value")
        String value,

        @JsonProperty("message")
        String message
) {
}
