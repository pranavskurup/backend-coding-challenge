package com.movie.rating.system.domain.entity;

import com.movie.rating.system.domain.exception.ValidationException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.time.Year;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Movie {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final String title;
    private final String plot;
    private final Integer yearOfRelease;
    @Builder.Default
    private final boolean isActive = true;
    private final UUID createdBy;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant deactivatedAt;
    private final UUID deactivatedBy;

    /**
     * Custom builder to handle validation and defaults
     */
    public static MovieBuilder builder() {
        return new MovieBuilder() {
            private static final int TITLE_MIN_LENGTH = 1;
            private static final int TITLE_MAX_LENGTH = 255;
            private static final int PLOT_MAX_LENGTH = 10000;
            private static final int MIN_YEAR = 1888; // First motion picture
            
            public Movie build() {
                if (super.createdAt == null) {
                    super.createdAt = Instant.now();
                }
                if (super.updatedAt == null) {
                    super.updatedAt = super.createdAt;
                }
                
                // Validate required fields
                validateTitle();
                validateYearOfRelease();
                validateCreatedBy();
                validatePlot();
                
                return super.build();
            }
            
            private void validateTitle() {
                if (super.title == null || super.title.trim().isEmpty()) {
                    throw new ValidationException(
                            "Movie title is required", 
                            Map.of("title", "Title cannot be null or empty")
                    );
                }
                
                String trimmedTitle = super.title.trim();
                if (trimmedTitle.length() < TITLE_MIN_LENGTH) {
                    throw new ValidationException(
                            "Movie title is too short", 
                            Map.of("title", "Title must be at least " + TITLE_MIN_LENGTH + " character(s) long")
                    );
                }
                
                if (trimmedTitle.length() > TITLE_MAX_LENGTH) {
                    throw new ValidationException(
                            "Movie title is too long", 
                            Map.of("title", "Title must not exceed " + TITLE_MAX_LENGTH + " characters")
                    );
                }
                
                // Normalize title
                super.title = trimmedTitle;
            }
            
            private void validateYearOfRelease() {
                if (super.yearOfRelease == null) {
                    throw new ValidationException(
                            "Year of release is required", 
                            Map.of("yearOfRelease", "Year of release cannot be null")
                    );
                }
                
                int currentYear = Year.now().getValue();
                int maxFutureYear = currentYear + 5; // Allow up to 5 years in future for planned releases
                
                if (super.yearOfRelease < MIN_YEAR) {
                    throw new ValidationException(
                            "Invalid year of release", 
                            Map.of("yearOfRelease", "Year of release cannot be before " + MIN_YEAR)
                    );
                }
                
                if (super.yearOfRelease > maxFutureYear) {
                    throw new ValidationException(
                            "Invalid year of release", 
                            Map.of("yearOfRelease", "Year of release cannot be more than 5 years in the future")
                    );
                }
            }
            
            private void validateCreatedBy() {
                if (super.createdBy == null) {
                    throw new ValidationException(
                            "Created by user ID is required", 
                            Map.of("createdBy", "Created by user ID cannot be null")
                    );
                }
            }
            
            private void validatePlot() {
                if (super.plot != null && super.plot.length() > PLOT_MAX_LENGTH) {
                    throw new ValidationException(
                            "Plot is too long", 
                            Map.of("plot", "Plot must not exceed " + PLOT_MAX_LENGTH + " characters")
                    );
                }
            }
        };
    }

    /**
     * Check if this movie is effectively active (not deactivated and active flag is true)
     */
    public boolean isEffectivelyActive() {
        return isActive && deactivatedAt == null;
    }

    /**
     * Get full display information combining title and year
     */
    public String getDisplayTitle() {
        return title + " (" + yearOfRelease + ")";
    }

    /**
     * Check if the movie was created by the specified user
     */
    public boolean isCreatedBy(UUID userId) {
        return Objects.equals(createdBy, userId);
    }

    /**
     * Check if the movie was deactivated by the specified user
     */
    public boolean isDeactivatedBy(UUID userId) {
        return Objects.equals(deactivatedBy, userId);
    }

    /**
     * Create a new movie with updated information
     */
    public Movie updateWith(String newTitle, String newPlot, Integer newYearOfRelease) {
        return this.toBuilder()
                .title(newTitle != null ? newTitle : this.title)
                .plot(newPlot != null ? newPlot : this.plot)
                .yearOfRelease(newYearOfRelease != null ? newYearOfRelease : this.yearOfRelease)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Deactivate the movie
     */
    public Movie deactivate(UUID deactivatedByUserId) {
        return this.toBuilder()
                .isActive(false)
                .deactivatedAt(Instant.now())
                .deactivatedBy(deactivatedByUserId)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Reactivate the movie
     */
    public Movie reactivate() {
        return this.toBuilder()
                .isActive(true)
                .deactivatedAt(null)
                .deactivatedBy(null)
                .updatedAt(Instant.now())
                .build();
    }
}
