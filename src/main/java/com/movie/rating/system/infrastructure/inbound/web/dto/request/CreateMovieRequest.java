package com.movie.rating.system.infrastructure.inbound.web.dto.request;

/**
 * Request DTO for creating a new movie.
 */
public record CreateMovieRequest(
        String title,
        String plot,
        Integer yearOfRelease
) {
}
