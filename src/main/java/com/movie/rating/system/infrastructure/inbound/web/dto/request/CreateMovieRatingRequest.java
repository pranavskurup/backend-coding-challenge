package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for creating a new movie rating.
 */
@Schema(description = "Request to create a new movie rating")
public record CreateMovieRatingRequest(
        @Schema(description = "ID of the movie to rate", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Movie ID is required")
        UUID movieId,
        
        @Schema(description = "Rating score from 1 to 5", example = "5", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5")
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,
        
        @Schema(description = "Optional review text", example = "Amazing movie with great story and characters!")
        @Size(max = 1000, message = "Review must not exceed 1000 characters")
        String review
) {
}
