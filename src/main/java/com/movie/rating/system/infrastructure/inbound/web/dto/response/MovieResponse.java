package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for movie information.
 */
public record MovieResponse(
        UUID id,
        String title,
        String plot,
        Integer yearOfRelease,
        boolean isActive,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
