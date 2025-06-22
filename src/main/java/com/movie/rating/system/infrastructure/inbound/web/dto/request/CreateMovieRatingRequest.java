package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import java.util.UUID;

/**
 * Request DTO for creating a new movie rating.
 */
public record CreateMovieRatingRequest(
        UUID movieId,
        Integer rating,
        String review
) {
}
