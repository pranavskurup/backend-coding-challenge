package com.movie.rating.system.infrastructure.inbound.web.dto.request;

/**
 * Request DTO for updating a movie.
 */
public record UpdateMovieRequest(
        String title,
        String plot,
        Integer yearOfRelease
) {
}
