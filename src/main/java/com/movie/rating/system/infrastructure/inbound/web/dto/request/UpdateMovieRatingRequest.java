package com.movie.rating.system.infrastructure.inbound.web.dto.request;

/**
 * Request DTO for updating a movie rating.
 */
public record UpdateMovieRatingRequest(
        Integer rating,
        String review
) {
}
