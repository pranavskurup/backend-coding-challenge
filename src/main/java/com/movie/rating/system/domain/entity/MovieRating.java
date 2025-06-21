package com.movie.rating.system.domain.entity;

import com.movie.rating.system.domain.exception.ValidationException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class MovieRating {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final UUID movieId;
    private final UUID userId;
    private final Integer rating;
    private final String review;
    @Builder.Default
    private final boolean isActive = true;
    private final Instant createdAt;
    private final Instant updatedAt;

    /**
     * Custom builder to handle validation and defaults
     */
    public static MovieRatingBuilder builder() {
        return new MovieRatingBuilder() {
            private static final int MIN_RATING = 1;
            private static final int MAX_RATING = 10;
            private static final int REVIEW_MAX_LENGTH = 5000;
            
            public MovieRating build() {
                if (super.createdAt == null) {
                    super.createdAt = Instant.now();
                }
                if (super.updatedAt == null) {
                    super.updatedAt = super.createdAt;
                }
                
                // Validate required fields
                validateMovieId();
                validateUserId();
                validateRating();
                validateReview();
                
                return super.build();
            }
            
            private void validateMovieId() {
                if (super.movieId == null) {
                    throw new ValidationException(
                            "Movie ID is required", 
                            Map.of("movieId", "Movie ID cannot be null")
                    );
                }
            }
            
            private void validateUserId() {
                if (super.userId == null) {
                    throw new ValidationException(
                            "User ID is required", 
                            Map.of("userId", "User ID cannot be null")
                    );
                }
            }
            
            private void validateRating() {
                if (super.rating == null) {
                    throw new ValidationException(
                            "Rating is required", 
                            Map.of("rating", "Rating cannot be null")
                    );
                }
                
                if (super.rating < MIN_RATING || super.rating > MAX_RATING) {
                    throw new ValidationException(
                            "Rating must be between " + MIN_RATING + " and " + MAX_RATING, 
                            Map.of("rating", "Rating must be between " + MIN_RATING + " and " + MAX_RATING)
                    );
                }
            }
            
            private void validateReview() {
                if (super.review != null && super.review.length() > REVIEW_MAX_LENGTH) {
                    throw new ValidationException(
                            "Review is too long", 
                            Map.of("review", "Review must not exceed " + REVIEW_MAX_LENGTH + " characters")
                    );
                }
            }
        };
    }

    /**
     * Check if this rating belongs to the specified user
     */
    public boolean belongsToUser(UUID userId) {
        return Objects.equals(this.userId, userId);
    }

    /**
     * Check if this rating is for the specified movie
     */
    public boolean isForMovie(UUID movieId) {
        return Objects.equals(this.movieId, movieId);
    }

    /**
     * Update the rating and review
     */
    public MovieRating updateRating(Integer newRating, String newReview) {
        return this.toBuilder()
                .rating(newRating != null ? newRating : this.rating)
                .review(newReview != null ? newReview : this.review)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Deactivate the rating
     */
    public MovieRating deactivate() {
        return this.toBuilder()
                .isActive(false)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Reactivate the rating
     */
    public MovieRating reactivate() {
        return this.toBuilder()
                .isActive(true)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Get a descriptive summary of the rating
     */
    public String getRatingSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(rating).append("/10");
        
        if (rating >= 9) {
            summary.append(" (Excellent)");
        } else if (rating >= 7) {
            summary.append(" (Good)");
        } else if (rating >= 5) {
            summary.append(" (Average)");
        } else {
            summary.append(" (Poor)");
        }
        
        return summary.toString();
    }

    /**
     * Check if the rating has a written review
     */
    public boolean hasReview() {
        return review != null && !review.trim().isEmpty();
    }

    /**
     * Get the review or a default message if no review exists
     */
    public String getReviewOrDefault() {
        return hasReview() ? review : "No review provided";
    }
}
