package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for movie information.
 */
@Schema(description = "Movie information response")
public record MovieResponse(
        @Schema(description = "Unique identifier of the movie", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        
        @Schema(description = "Title of the movie", example = "The Shawshank Redemption")
        String title,
        
        @Schema(description = "Plot description of the movie", example = "Two imprisoned men bond over a number of years...")
        String plot,
        
        @Schema(description = "Year the movie was released", example = "1994")
        Integer yearOfRelease,
        
        @Schema(description = "Whether the movie is active (not deleted)", example = "true")
        boolean isActive,
        
        @Schema(description = "ID of the user who created this movie", example = "987e6543-e21b-43d2-a654-426614174000")
        UUID createdBy,
        
        @Schema(description = "Timestamp when the movie was created", example = "2023-12-01T10:30:00.000Z")
        Instant createdAt,
        
        @Schema(description = "Timestamp when the movie was last updated", example = "2023-12-02T15:45:00.000Z")
        Instant updatedAt
) {
}
