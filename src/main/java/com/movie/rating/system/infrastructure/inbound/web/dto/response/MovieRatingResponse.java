package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for movie rating information.
 */
public record MovieRatingResponse(
        UUID id,
        UUID movieId,
        UUID userId,
        Integer rating,
        String review,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
