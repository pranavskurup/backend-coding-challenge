package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Request DTO for updating a movie.
 */
@Schema(description = "Request to update an existing movie. All fields are optional.")
public record UpdateMovieRequest(
        @Schema(description = "Updated title of the movie", example = "The Shawshank Redemption (Director's Cut)")
        @Size(min = 1, max = 255, message = "Movie title must be between 1 and 255 characters")
        String title,
        
        @Schema(description = "Updated plot description of the movie", example = "Enhanced version with deleted scenes...")
        @Size(min = 10, max = 2000, message = "Movie plot must be between 10 and 2000 characters")
        String plot,
        
        @Schema(description = "Updated year the movie was released", example = "1995", minimum = "1888", maximum = "2030")
        @Min(value = 1888, message = "Year of release must be 1888 or later")
        @Max(value = 2030, message = "Year of release must be 2030 or earlier")
        Integer yearOfRelease
) {
}
