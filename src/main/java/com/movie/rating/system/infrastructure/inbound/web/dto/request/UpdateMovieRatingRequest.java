package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a movie rating.
 */
@Schema(description = "Request to update an existing movie rating. All fields are optional.")
public record UpdateMovieRatingRequest(
        @Schema(description = "Updated rating score from 1 to 5", example = "4", minimum = "1", maximum = "5")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,
        
        @Schema(description = "Updated review text", example = "Updated review with more thoughts...")
        @Size(max = 1000, message = "Review must not exceed 1000 characters")
        String review
) {
}
