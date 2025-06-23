package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for movie rating information.
 */
@Schema(description = "Movie rating information response")
public record MovieRatingResponse(
        @Schema(description = "Unique identifier of the movie rating", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        
        @Schema(description = "ID of the rated movie", example = "987e6543-e21b-43d2-a654-426614174000")
        UUID movieId,
        
        @Schema(description = "ID of the user who created this rating", example = "456e7890-e12b-34d5-a678-426614174000")
        UUID userId,
        
        @Schema(description = "Rating score from 1 to 5", example = "5", minimum = "1", maximum = "5")
        Integer rating,
        
        @Schema(description = "Review text provided by the user", example = "Amazing movie with great story and characters!")
        String review,
        
        @Schema(description = "Whether the rating is active (not deleted)", example = "true")
        boolean isActive,
        
        @Schema(description = "Timestamp when the rating was created", example = "2023-12-01T10:30:00.000Z")
        Instant createdAt,
        
        @Schema(description = "Timestamp when the rating was last updated", example = "2023-12-02T15:45:00.000Z")
        Instant updatedAt
) {
}
