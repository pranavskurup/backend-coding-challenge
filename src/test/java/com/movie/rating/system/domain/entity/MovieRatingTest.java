package com.movie.rating.system.domain.entity;

import com.movie.rating.system.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MovieRating entity.
 * Tests entity validation, builder functionality, and business methods.
 */
@DisplayName("MovieRating Entity Tests")
class MovieRatingTest {

    @Nested
    @DisplayName("MovieRating Builder Tests")
    class MovieRatingBuilderTests {

        @Test
        @DisplayName("Should create movie rating with all required fields")
        void shouldCreateMovieRatingWithAllRequiredFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID movieId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Integer rating = 8;
            String review = "Great movie!";
            Instant now = Instant.now();

            // When
            MovieRating movieRating = MovieRating.builder()
                    .id(id)
                    .movieId(movieId)
                    .userId(userId)
                    .rating(rating)
                    .review(review)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // Then
            assertThat(movieRating.getId()).isEqualTo(id);
            assertThat(movieRating.getMovieId()).isEqualTo(movieId);
            assertThat(movieRating.getUserId()).isEqualTo(userId);
            assertThat(movieRating.getRating()).isEqualTo(rating);
            assertThat(movieRating.getReview()).isEqualTo(review);
            assertThat(movieRating.isActive()).isTrue();
            assertThat(movieRating.getCreatedAt()).isEqualTo(now);
            assertThat(movieRating.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should create movie rating with default values when timestamps not provided")
        void shouldCreateMovieRatingWithDefaultTimestamps() {
            // Given
            Instant beforeCreation = Instant.now();

            // When
            MovieRating movieRating = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(7)
                    .build();

            // Then
            Instant afterCreation = Instant.now();
            
            assertThat(movieRating.getCreatedAt()).isBetween(beforeCreation, afterCreation);
            assertThat(movieRating.getUpdatedAt()).isEqualTo(movieRating.getCreatedAt());
            assertThat(movieRating.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw ValidationException when movie ID is null")
        void shouldThrowValidationExceptionWhenMovieIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> MovieRating.builder()
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Movie ID is required");
        }

        @Test
        @DisplayName("Should throw ValidationException when user ID is null")
        void shouldThrowValidationExceptionWhenUserIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .rating(8)
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("User ID is required");
        }

        @Test
        @DisplayName("Should throw ValidationException when rating is null")
        void shouldThrowValidationExceptionWhenRatingIsNull() {
            // When & Then
            assertThatThrownBy(() -> MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Rating is required");
        }

        @Test
        @DisplayName("Should throw ValidationException when rating is below minimum")
        void shouldThrowValidationExceptionWhenRatingIsBelowMinimum() {
            // When & Then
            assertThatThrownBy(() -> MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(0)
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Rating must be between 1 and 10");
        }

        @Test
        @DisplayName("Should throw ValidationException when rating is above maximum")
        void shouldThrowValidationExceptionWhenRatingIsAboveMaximum() {
            // When & Then
            assertThatThrownBy(() -> MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(11)
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Rating must be between 1 and 10");
        }

        @Test
        @DisplayName("Should accept minimum rating value")
        void shouldAcceptMinimumRatingValue() {
            // When & Then
            assertThatCode(() -> MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(1)
                    .build())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should accept maximum rating value")
        void shouldAcceptMaximumRatingValue() {
            // When & Then
            assertThatCode(() -> MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(10)
                    .build())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw ValidationException when review is too long")
        void shouldThrowValidationExceptionWhenReviewIsTooLong() {
            // Given
            String longReview = "A".repeat(5001); // Max is 5000

            // When & Then
            assertThatThrownBy(() -> MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .review(longReview)
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Review is too long");
        }

        @Test
        @DisplayName("Should accept maximum review length")
        void shouldAcceptMaximumReviewLength() {
            // Given
            String maxLengthReview = "A".repeat(5000);

            // When & Then
            assertThatCode(() -> MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .review(maxLengthReview)
                    .build())
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("MovieRating Business Methods Tests")
    class MovieRatingBusinessMethodsTests {

        private MovieRating createTestMovieRating() {
            return MovieRating.builder()
                    .id(UUID.randomUUID())
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .review("Great movie!")
                    .build();
        }

        @Test
        @DisplayName("Should check if rating belongs to user correctly")
        void shouldCheckIfRatingBelongsToUserCorrectly() {
            // Given
            UUID userId = UUID.randomUUID();
            MovieRating movieRating = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(userId)
                    .rating(8)
                    .build();

            // When & Then
            assertThat(movieRating.belongsToUser(userId)).isTrue();
            assertThat(movieRating.belongsToUser(UUID.randomUUID())).isFalse();
        }

        @Test
        @DisplayName("Should check if rating is for movie correctly")
        void shouldCheckIfRatingIsForMovieCorrectly() {
            // Given
            UUID movieId = UUID.randomUUID();
            MovieRating movieRating = MovieRating.builder()
                    .movieId(movieId)
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .build();

            // When & Then
            assertThat(movieRating.isForMovie(movieId)).isTrue();
            assertThat(movieRating.isForMovie(UUID.randomUUID())).isFalse();
        }

        @Test
        @DisplayName("Should update rating and review correctly")
        void shouldUpdateRatingAndReviewCorrectly() {
            // Given
            MovieRating movieRating = createTestMovieRating();
            Integer newRating = 9;
            String newReview = "Excellent movie!";

            // When
            MovieRating updatedRating = movieRating.updateRating(newRating, newReview);

            // Then
            assertThat(updatedRating.getRating()).isEqualTo(newRating);
            assertThat(updatedRating.getReview()).isEqualTo(newReview);
            assertThat(updatedRating.getUpdatedAt()).isAfter(movieRating.getUpdatedAt());
            assertThat(updatedRating.getId()).isEqualTo(movieRating.getId());
            assertThat(updatedRating.getCreatedAt()).isEqualTo(movieRating.getCreatedAt());
        }

        @Test
        @DisplayName("Should keep original values when update parameters are null")
        void shouldKeepOriginalValuesWhenUpdateParametersAreNull() {
            // Given
            MovieRating movieRating = createTestMovieRating();

            // When
            MovieRating updatedRating = movieRating.updateRating(null, null);

            // Then
            assertThat(updatedRating.getRating()).isEqualTo(movieRating.getRating());
            assertThat(updatedRating.getReview()).isEqualTo(movieRating.getReview());
            assertThat(updatedRating.getUpdatedAt()).isAfter(movieRating.getUpdatedAt());
        }

        @Test
        @DisplayName("Should deactivate rating correctly")
        void shouldDeactivateRatingCorrectly() {
            // Given
            MovieRating movieRating = createTestMovieRating();

            // When
            MovieRating deactivatedRating = movieRating.deactivate();

            // Then
            assertThat(deactivatedRating.isActive()).isFalse();
            assertThat(deactivatedRating.getUpdatedAt()).isAfter(movieRating.getUpdatedAt());
            assertThat(deactivatedRating.getId()).isEqualTo(movieRating.getId());
        }

        @Test
        @DisplayName("Should reactivate rating correctly")
        void shouldReactivateRatingCorrectly() {
            // Given
            MovieRating movieRating = createTestMovieRating().deactivate();

            // When
            MovieRating reactivatedRating = movieRating.reactivate();

            // Then
            assertThat(reactivatedRating.isActive()).isTrue();
            assertThat(reactivatedRating.getUpdatedAt()).isAfter(movieRating.getUpdatedAt());
            assertThat(reactivatedRating.getId()).isEqualTo(movieRating.getId());
        }

        @Test
        @DisplayName("Should get rating summary correctly for excellent rating")
        void shouldGetRatingSummaryCorrectlyForExcellentRating() {
            // Given
            MovieRating movieRating = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(9)
                    .build();

            // When & Then
            assertThat(movieRating.getRatingSummary()).isEqualTo("9/10 (Excellent)");
        }

        @Test
        @DisplayName("Should get rating summary correctly for good rating")
        void shouldGetRatingSummaryCorrectlyForGoodRating() {
            // Given
            MovieRating movieRating = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(7)
                    .build();

            // When & Then
            assertThat(movieRating.getRatingSummary()).isEqualTo("7/10 (Good)");
        }

        @Test
        @DisplayName("Should get rating summary correctly for average rating")
        void shouldGetRatingSummaryCorrectlyForAverageRating() {
            // Given
            MovieRating movieRating = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(5)
                    .build();

            // When & Then
            assertThat(movieRating.getRatingSummary()).isEqualTo("5/10 (Average)");
        }

        @Test
        @DisplayName("Should get rating summary correctly for poor rating")
        void shouldGetRatingSummaryCorrectlyForPoorRating() {
            // Given
            MovieRating movieRating = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(3)
                    .build();

            // When & Then
            assertThat(movieRating.getRatingSummary()).isEqualTo("3/10 (Poor)");
        }

        @Test
        @DisplayName("Should check if rating has review correctly")
        void shouldCheckIfRatingHasReviewCorrectly() {
            // Given
            MovieRating ratingWithReview = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .review("Great movie!")
                    .build();

            MovieRating ratingWithoutReview = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .build();

            MovieRating ratingWithEmptyReview = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .review("   ")
                    .build();

            // When & Then
            assertThat(ratingWithReview.hasReview()).isTrue();
            assertThat(ratingWithoutReview.hasReview()).isFalse();
            assertThat(ratingWithEmptyReview.hasReview()).isFalse();
        }

        @Test
        @DisplayName("Should get review or default correctly")
        void shouldGetReviewOrDefaultCorrectly() {
            // Given
            MovieRating ratingWithReview = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .review("Great movie!")
                    .build();

            MovieRating ratingWithoutReview = MovieRating.builder()
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .build();

            // When & Then
            assertThat(ratingWithReview.getReviewOrDefault()).isEqualTo("Great movie!");
            assertThat(ratingWithoutReview.getReviewOrDefault()).isEqualTo("No review provided");
        }
    }

    @Nested
    @DisplayName("MovieRating Equals and HashCode Tests")
    class MovieRatingEqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when IDs are the same")
        void shouldBeEqualWhenIdsAreTheSame() {
            // Given
            UUID id = UUID.randomUUID();
            MovieRating rating1 = MovieRating.builder()
                    .id(id)
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(8)
                    .review("Review 1")
                    .build();

            MovieRating rating2 = MovieRating.builder()
                    .id(id)
                    .movieId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .rating(5)
                    .review("Review 2")
                    .build();

            // When & Then
            assertThat(rating1).isEqualTo(rating2);
            assertThat(rating1.hashCode()).isEqualTo(rating2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            // Given
            UUID movieId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            
            MovieRating rating1 = MovieRating.builder()
                    .id(UUID.randomUUID())
                    .movieId(movieId)
                    .userId(userId)
                    .rating(8)
                    .review("Same review")
                    .build();

            MovieRating rating2 = MovieRating.builder()
                    .id(UUID.randomUUID())
                    .movieId(movieId)
                    .userId(userId)
                    .rating(8)
                    .review("Same review")
                    .build();

            // When & Then
            assertThat(rating1).isNotEqualTo(rating2);
            assertThat(rating1.hashCode()).isNotEqualTo(rating2.hashCode());
        }
    }

    @Nested
    @DisplayName("MovieRating ToString Tests")
    class MovieRatingToStringTests {

        @Test
        @DisplayName("Should include all important fields in toString")
        void shouldIncludeAllImportantFieldsInToString() {
            // Given
            UUID id = UUID.randomUUID();
            UUID movieId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            MovieRating movieRating = MovieRating.builder()
                    .id(id)
                    .movieId(movieId)
                    .userId(userId)
                    .rating(8)
                    .review("Great movie!")
                    .build();

            // When
            String toString = movieRating.toString();

            // Then
            assertThat(toString).contains(id.toString());
            assertThat(toString).contains(movieId.toString());
            assertThat(toString).contains(userId.toString());
            assertThat(toString).contains("8");
            assertThat(toString).contains("Great movie!");
        }
    }
}
