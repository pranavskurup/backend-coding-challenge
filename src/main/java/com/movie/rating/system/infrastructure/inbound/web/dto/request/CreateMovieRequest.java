package com.movie.rating.system.infrastructure.inbound.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a new movie.
 */
@Schema(description = "Request to create a new movie in the system")
public record CreateMovieRequest(
        @Schema(description = "Title of the movie", example = "The Shawshank Redemption", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Movie title is required")
        @Size(min = 1, max = 255, message = "Movie title must be between 1 and 255 characters")
        String title,
        
        @Schema(description = "Plot description of the movie", example = "Two imprisoned men bond over a number of years...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Movie plot is required")
        @Size(min = 10, max = 2000, message = "Movie plot must be between 10 and 2000 characters")
        String plot,
        
        @Schema(description = "Year the movie was released", example = "1994", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1888", maximum = "2030")
        @NotNull(message = "Year of release is required")
        @Min(value = 1888, message = "Year of release must be 1888 or later")
        @Max(value = 2030, message = "Year of release must be 2030 or earlier")
        Integer yearOfRelease
) {
}
