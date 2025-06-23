package com.movie.rating.system.infrastructure.outbound.persistence.mapper;

import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieRatingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MovieRatingPersistenceMapper Tests")
class MovieRatingPersistenceMapperTest {

    private MovieRatingPersistenceMapper mapper;

    private UUID ratingId;
    private UUID movieId;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;

    @BeforeEach
    void setUp() {
        mapper = new MovieRatingPersistenceMapper();
        
        ratingId = UUID.randomUUID();
        movieId = UUID.randomUUID();
        userId = UUID.randomUUID();
        createdAt = Instant.now().minusSeconds(3600);
        updatedAt = Instant.now().minusSeconds(1800);
    }

    @Test
    @DisplayName("Should convert domain MovieRating to MovieRatingEntity")
    void shouldConvertDomainMovieRatingToEntity() {
        // Given
        MovieRating movieRating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(5)
                .review("Absolutely fantastic movie! A must-watch for everyone.")
                .isActive(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When
        MovieRatingEntity entity = mapper.toEntity(movieRating);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.id()).isEqualTo(ratingId);
        assertThat(entity.movieId()).isEqualTo(movieId);
        assertThat(entity.userId()).isEqualTo(userId);
        assertThat(entity.rating()).isEqualTo(5);
        assertThat(entity.review()).isEqualTo("Absolutely fantastic movie! A must-watch for everyone.");
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.createdAt()).isEqualTo(createdAt);
        assertThat(entity.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should convert domain MovieRating to MovieRatingEntity without review")
    void shouldConvertDomainMovieRatingToEntityWithoutReview() {
        // Given
        MovieRating movieRating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(3)
                .review(null)
                .isActive(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When
        MovieRatingEntity entity = mapper.toEntity(movieRating);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.id()).isEqualTo(ratingId);
        assertThat(entity.movieId()).isEqualTo(movieId);
        assertThat(entity.userId()).isEqualTo(userId);
        assertThat(entity.rating()).isEqualTo(3);
        assertThat(entity.review()).isNull();
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.createdAt()).isEqualTo(createdAt);
        assertThat(entity.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should convert domain MovieRating to MovieRatingEntity when inactive")
    void shouldConvertDomainMovieRatingToEntityWhenInactive() {
        // Given
        MovieRating movieRating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(1)
                .review("Terrible movie, would not recommend.")
                .isActive(false)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When
        MovieRatingEntity entity = mapper.toEntity(movieRating);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.id()).isEqualTo(ratingId);
        assertThat(entity.movieId()).isEqualTo(movieId);
        assertThat(entity.userId()).isEqualTo(userId);
        assertThat(entity.rating()).isEqualTo(1);
        assertThat(entity.review()).isEqualTo("Terrible movie, would not recommend.");
        assertThat(entity.isActive()).isFalse();
        assertThat(entity.createdAt()).isEqualTo(createdAt);
        assertThat(entity.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should convert MovieRatingEntity to domain MovieRating")
    void shouldConvertEntityToDomainMovieRating() {
        // Given
        MovieRatingEntity entity = new MovieRatingEntity(
                ratingId,
                movieId,
                userId,
                4,
                "Really good movie with excellent cinematography.",
                true,
                createdAt,
                updatedAt
        );

        // When
        MovieRating movieRating = mapper.toDomain(entity);

        // Then
        assertThat(movieRating).isNotNull();
        assertThat(movieRating.getId()).isEqualTo(ratingId);
        assertThat(movieRating.getMovieId()).isEqualTo(movieId);
        assertThat(movieRating.getUserId()).isEqualTo(userId);
        assertThat(movieRating.getRating()).isEqualTo(4);
        assertThat(movieRating.getReview()).isEqualTo("Really good movie with excellent cinematography.");
        assertThat(movieRating.isActive()).isTrue();
        assertThat(movieRating.getCreatedAt()).isEqualTo(createdAt);
        assertThat(movieRating.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should convert MovieRatingEntity to domain MovieRating without review")
    void shouldConvertEntityToDomainMovieRatingWithoutReview() {
        // Given
        MovieRatingEntity entity = new MovieRatingEntity(
                ratingId,
                movieId,
                userId,
                2,
                null,
                true,
                createdAt,
                updatedAt
        );

        // When
        MovieRating movieRating = mapper.toDomain(entity);

        // Then
        assertThat(movieRating).isNotNull();
        assertThat(movieRating.getId()).isEqualTo(ratingId);
        assertThat(movieRating.getMovieId()).isEqualTo(movieId);
        assertThat(movieRating.getUserId()).isEqualTo(userId);
        assertThat(movieRating.getRating()).isEqualTo(2);
        assertThat(movieRating.getReview()).isNull();
        assertThat(movieRating.isActive()).isTrue();
        assertThat(movieRating.getCreatedAt()).isEqualTo(createdAt);
        assertThat(movieRating.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should convert MovieRatingEntity to domain MovieRating when inactive")
    void shouldConvertEntityToDomainMovieRatingWhenInactive() {
        // Given
        MovieRatingEntity entity = new MovieRatingEntity(
                ratingId,
                movieId,
                userId,
                1,
                "Disappointing sequel, not worth the time.",
                false,
                createdAt,
                updatedAt
        );

        // When
        MovieRating movieRating = mapper.toDomain(entity);

        // Then
        assertThat(movieRating).isNotNull();
        assertThat(movieRating.getId()).isEqualTo(ratingId);
        assertThat(movieRating.getMovieId()).isEqualTo(movieId);
        assertThat(movieRating.getUserId()).isEqualTo(userId);
        assertThat(movieRating.getRating()).isEqualTo(1);
        assertThat(movieRating.getReview()).isEqualTo("Disappointing sequel, not worth the time.");
        assertThat(movieRating.isActive()).isFalse();
        assertThat(movieRating.getCreatedAt()).isEqualTo(createdAt);
        assertThat(movieRating.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should handle null isActive in entity conversion by defaulting to true")
    void shouldHandleNullIsActiveInEntityConversion() {
        // Given
        MovieRatingEntity entity = new MovieRatingEntity(
                ratingId,
                movieId,
                userId,
                5,
                "Great movie!",
                null, // null isActive
                createdAt,
                updatedAt
        );

        // When
        MovieRating movieRating = mapper.toDomain(entity);

        // Then
        assertThat(movieRating).isNotNull();
        assertThat(movieRating.isActive()).isTrue(); // Should default to true when null
        assertThat(movieRating.getId()).isEqualTo(ratingId);
        assertThat(movieRating.getMovieId()).isEqualTo(movieId);
        assertThat(movieRating.getUserId()).isEqualTo(userId);
        assertThat(movieRating.getRating()).isEqualTo(5);
        assertThat(movieRating.getReview()).isEqualTo("Great movie!");
    }

    @Test
    @DisplayName("Should handle null movie rating in toEntity conversion")
    void shouldHandleNullMovieRatingInToEntityConversion() {
        // When
        MovieRatingEntity entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should handle null entity in toDomain conversion")
    void shouldHandleNullEntityInToDomainConversion() {
        // When
        MovieRating movieRating = mapper.toDomain(null);

        // Then
        assertThat(movieRating).isNull();
    }

    @Test
    @DisplayName("Should preserve all fields in round-trip conversion")
    void shouldPreserveAllFieldsInRoundTripConversion() {
        // Given
        MovieRating originalRating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(5)
                .review("This is an amazing movie that everyone should watch. The acting, plot, and cinematography are all top-notch.")
                .isActive(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When - Convert to entity and back to domain
        MovieRatingEntity entity = mapper.toEntity(originalRating);
        MovieRating convertedRating = mapper.toDomain(entity);

        // Then
        assertThat(convertedRating).isNotNull();
        assertThat(convertedRating.getId()).isEqualTo(originalRating.getId());
        assertThat(convertedRating.getMovieId()).isEqualTo(originalRating.getMovieId());
        assertThat(convertedRating.getUserId()).isEqualTo(originalRating.getUserId());
        assertThat(convertedRating.getRating()).isEqualTo(originalRating.getRating());
        assertThat(convertedRating.getReview()).isEqualTo(originalRating.getReview());
        assertThat(convertedRating.isActive()).isEqualTo(originalRating.isActive());
        assertThat(convertedRating.getCreatedAt()).isEqualTo(originalRating.getCreatedAt());
        assertThat(convertedRating.getUpdatedAt()).isEqualTo(originalRating.getUpdatedAt());
    }

    @Test
    @DisplayName("Should preserve all fields in round-trip conversion when inactive")
    void shouldPreserveAllFieldsInRoundTripConversionWhenInactive() {
        // Given
        MovieRating originalRating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(2)
                .review("Not my cup of tea. The pacing was too slow and the plot was confusing.")
                .isActive(false)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When - Convert to entity and back to domain
        MovieRatingEntity entity = mapper.toEntity(originalRating);
        MovieRating convertedRating = mapper.toDomain(entity);

        // Then
        assertThat(convertedRating).isNotNull();
        assertThat(convertedRating.getId()).isEqualTo(originalRating.getId());
        assertThat(convertedRating.getMovieId()).isEqualTo(originalRating.getMovieId());
        assertThat(convertedRating.getUserId()).isEqualTo(originalRating.getUserId());
        assertThat(convertedRating.getRating()).isEqualTo(originalRating.getRating());
        assertThat(convertedRating.getReview()).isEqualTo(originalRating.getReview());
        assertThat(convertedRating.isActive()).isEqualTo(originalRating.isActive());
        assertThat(convertedRating.getCreatedAt()).isEqualTo(originalRating.getCreatedAt());
        assertThat(convertedRating.getUpdatedAt()).isEqualTo(originalRating.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle rating with minimal data")
    void shouldHandleRatingWithMinimalData() {
        // Given - MovieRating with only required fields
        MovieRating rating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(4)
                .build(); // Other fields will be set by builder defaults

        // When
        MovieRatingEntity entity = mapper.toEntity(rating);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.id()).isEqualTo(ratingId);
        assertThat(entity.movieId()).isEqualTo(movieId);
        assertThat(entity.userId()).isEqualTo(userId);
        assertThat(entity.rating()).isEqualTo(4);
        assertThat(entity.review()).isNull(); // Should be null by default
        assertThat(entity.isActive()).isTrue(); // Should be true by default
        assertThat(entity.createdAt()).isNotNull();
        assertThat(entity.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle entity with minimal data")
    void shouldHandleEntityWithMinimalData() {
        // Given - Entity with only essential fields
        MovieRatingEntity entity = new MovieRatingEntity(
                ratingId,
                movieId,
                userId,
                3,
                null, // null review
                null, // null isActive
                createdAt,
                null  // null updatedAt
        );

        // When
        MovieRating rating = mapper.toDomain(entity);

        // Then
        assertThat(rating).isNotNull();
        assertThat(rating.getId()).isEqualTo(ratingId);
        assertThat(rating.getMovieId()).isEqualTo(movieId);
        assertThat(rating.getUserId()).isEqualTo(userId);
        assertThat(rating.getRating()).isEqualTo(3);
        assertThat(rating.getReview()).isNull();
        assertThat(rating.isActive()).isTrue(); // Should default to true
        assertThat(rating.getCreatedAt()).isEqualTo(createdAt);
        assertThat(rating.getUpdatedAt()).isEqualTo(createdAt); // MovieRating builder sets updatedAt to createdAt if null
    }

    @Test
    @DisplayName("Should handle extreme rating values")
    void shouldHandleExtremeRatingValues() {
        // Given - Test with minimum and maximum rating values
        MovieRating minRating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(1)
                .review("Worst movie ever!")
                .isActive(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        UUID maxRatingId = UUID.randomUUID();
        MovieRating maxRating = MovieRating.builder()
                .id(maxRatingId)
                .movieId(movieId)
                .userId(userId)
                .rating(5)
                .review("Best movie ever!")
                .isActive(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When
        MovieRatingEntity minEntity = mapper.toEntity(minRating);
        MovieRatingEntity maxEntity = mapper.toEntity(maxRating);

        // Then
        assertThat(minEntity.rating()).isEqualTo(1);
        assertThat(maxEntity.rating()).isEqualTo(5);
        
        // Round-trip test
        MovieRating convertedMinRating = mapper.toDomain(minEntity);
        MovieRating convertedMaxRating = mapper.toDomain(maxEntity);
        
        assertThat(convertedMinRating.getRating()).isEqualTo(1);
        assertThat(convertedMaxRating.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle long review text")
    void shouldHandleLongReviewText() {
        // Given - MovieRating with a very long review
        String longReview = "This is a very long review that goes into great detail about every aspect of the movie. ".repeat(50);
        
        MovieRating rating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(4)
                .review(longReview)
                .isActive(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When
        MovieRatingEntity entity = mapper.toEntity(rating);
        MovieRating convertedRating = mapper.toDomain(entity);

        // Then
        assertThat(entity.review()).isEqualTo(longReview);
        assertThat(convertedRating.getReview()).isEqualTo(longReview);
        assertThat(convertedRating.getReview()).hasSize(longReview.length());
    }
}
