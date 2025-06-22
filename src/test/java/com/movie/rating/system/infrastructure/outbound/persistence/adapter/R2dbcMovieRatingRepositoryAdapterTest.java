package com.movie.rating.system.infrastructure.outbound.persistence.adapter;

import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieRatingEntity;
import com.movie.rating.system.infrastructure.outbound.persistence.mapper.MovieRatingPersistenceMapper;
import com.movie.rating.system.infrastructure.outbound.persistence.repository.R2dbcMovieRatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("R2dbcMovieRatingRepositoryAdapter Tests")
class R2dbcMovieRatingRepositoryAdapterTest {

    @Mock
    private R2dbcMovieRatingRepository r2dbcRepository;

    @Mock
    private MovieRatingPersistenceMapper mapper;

    private R2dbcMovieRatingRepositoryAdapter adapter;

    private UUID movieId;
    private UUID userId;
    private UUID ratingId;
    private MovieRating movieRating;
    private MovieRatingEntity movieRatingEntity;

    @BeforeEach
    void setUp() {
        adapter = new R2dbcMovieRatingRepositoryAdapter(r2dbcRepository, mapper);
        
        movieId = UUID.randomUUID();
        userId = UUID.randomUUID();
        ratingId = UUID.randomUUID();
        
        movieRating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(5)
                .review("Great movie!")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        movieRatingEntity = new MovieRatingEntity(
                ratingId,
                movieId,
                userId,
                5,
                "Great movie!",
                true,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("Should successfully save movie rating")
    void shouldSaveMovieRating() {
        // Given
        when(mapper.toEntity(movieRating)).thenReturn(movieRatingEntity);
        when(r2dbcRepository.save(movieRatingEntity)).thenReturn(Mono.just(movieRatingEntity));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Mono<MovieRating> result = adapter.save(movieRating);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(mapper).toEntity(movieRating);
        verify(r2dbcRepository).save(movieRatingEntity);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find movie rating by ID")
    void shouldFindMovieRatingById() {
        // Given
        when(r2dbcRepository.findById(ratingId)).thenReturn(Mono.just(movieRatingEntity));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Mono<MovieRating> result = adapter.findById(ratingId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findById(ratingId);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should return empty when movie rating not found by ID")
    void shouldReturnEmptyWhenMovieRatingNotFoundById() {
        // Given
        when(r2dbcRepository.findById(ratingId)).thenReturn(Mono.empty());

        // When
        Mono<MovieRating> result = adapter.findById(ratingId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(r2dbcRepository).findById(ratingId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find active ratings by movie ID")
    void shouldFindActiveRatingsByMovieId() {
        // Given
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findActiveByMovieId(movieId)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findActiveByMovieId(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByMovieId(movieId);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find active ratings by user ID")
    void shouldFindActiveRatingsByUserId() {
        // Given
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findActiveByUserId(userId)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findActiveByUserId(userId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByUserId(userId);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find active rating by movie ID and user ID")
    void shouldFindActiveRatingByMovieIdAndUserId() {
        // Given
        when(r2dbcRepository.findActiveByMovieIdAndUserId(movieId, userId)).thenReturn(Mono.just(movieRatingEntity));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Mono<MovieRating> result = adapter.findActiveByMovieIdAndUserId(movieId, userId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByMovieIdAndUserId(movieId, userId);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find ratings by movie ID and rating range")
    void shouldFindRatingsByMovieIdAndRatingBetween() {
        // Given
        Integer minRating = 3;
        Integer maxRating = 5;
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findActiveByMovieIdAndRatingBetween(movieId, minRating, maxRating))
                .thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findActiveByMovieIdAndRatingBetween(movieId, minRating, maxRating);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByMovieIdAndRatingBetween(movieId, minRating, maxRating);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find ratings with reviews by movie ID")
    void shouldFindRatingsWithReviewsByMovieId() {
        // Given
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findActiveByMovieIdWithReviews(movieId)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findActiveByMovieIdWithReviews(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByMovieIdWithReviews(movieId);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find ratings created between dates")
    void shouldFindRatingsCreatedBetweenDates() {
        // Given
        Instant startDate = Instant.now().minusSeconds(3600);
        Instant endDate = Instant.now();
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findActiveByCreatedAtBetween(startDate, endDate)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findActiveByCreatedAtBetween(startDate, endDate);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByCreatedAtBetween(startDate, endDate);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should check if active rating exists by movie ID and user ID")
    void shouldCheckIfActiveRatingExistsByMovieIdAndUserId() {
        // Given
        when(r2dbcRepository.existsActiveByMovieIdAndUserId(movieId, userId)).thenReturn(Mono.just(true));

        // When
        Mono<Boolean> result = adapter.existsActiveByMovieIdAndUserId(movieId, userId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(r2dbcRepository).existsActiveByMovieIdAndUserId(movieId, userId);
    }

    @Test
    @DisplayName("Should soft delete rating by ID")
    void shouldSoftDeleteRatingById() {
        // Given
        MovieRatingEntity inactiveEntity = new MovieRatingEntity(
                movieRatingEntity.id(),
                movieRatingEntity.movieId(),
                movieRatingEntity.userId(),
                movieRatingEntity.rating(),
                movieRatingEntity.review(),
                false, // inactive
                movieRatingEntity.createdAt(),
                Instant.now()
        );
        
        when(r2dbcRepository.findById(ratingId)).thenReturn(Mono.just(movieRatingEntity));
        when(r2dbcRepository.save(any(MovieRatingEntity.class))).thenReturn(Mono.just(inactiveEntity));

        // When
        Mono<Void> result = adapter.deleteById(ratingId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(r2dbcRepository).findById(ratingId);
        verify(r2dbcRepository).save(any(MovieRatingEntity.class));
    }

    @Test
    @DisplayName("Should throw error when trying to delete non-existent rating")
    void shouldThrowErrorWhenDeletingNonExistentRating() {
        // Given
        when(r2dbcRepository.findById(ratingId)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = adapter.deleteById(ratingId);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(r2dbcRepository).findById(ratingId);
        verify(r2dbcRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate average rating by movie ID")
    void shouldCalculateAverageRatingByMovieId() {
        // Given
        Double expectedAverage = 4.5;
        when(r2dbcRepository.calculateAverageRatingByMovieId(movieId)).thenReturn(Mono.just(expectedAverage));

        // When
        Mono<Double> result = adapter.calculateAverageRatingByMovieId(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedAverage)
                .verifyComplete();

        verify(r2dbcRepository).calculateAverageRatingByMovieId(movieId);
    }

    @Test
    @DisplayName("Should count active ratings by movie ID")
    void shouldCountActiveRatingsByMovieId() {
        // Given
        Long expectedCount = 10L;
        when(r2dbcRepository.countActiveByMovieId(movieId)).thenReturn(Mono.just(expectedCount));

        // When
        Mono<Long> result = adapter.countActiveByMovieId(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();

        verify(r2dbcRepository).countActiveByMovieId(movieId);
    }

    @Test
    @DisplayName("Should count active ratings by user ID")
    void shouldCountActiveRatingsByUserId() {
        // Given
        Long expectedCount = 5L;
        when(r2dbcRepository.countActiveByUserId(userId)).thenReturn(Mono.just(expectedCount));

        // When
        Mono<Long> result = adapter.countActiveByUserId(userId);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();

        verify(r2dbcRepository).countActiveByUserId(userId);
    }

    @Test
    @DisplayName("Should find top rated movies")
    void shouldFindTopRatedMovies() {
        // Given
        int limit = 5;
        int minRatingCount = 10;
        R2dbcMovieRatingRepository.TopRatedMovieResult topRatedResult = 
                new R2dbcMovieRatingRepository.TopRatedMovieResult(movieId, 4.5, 20L);
        
        when(r2dbcRepository.findTopRatedMovies(limit, minRatingCount))
                .thenReturn(Flux.just(topRatedResult));

        // When
        Flux<UUID> result = adapter.findTopRatedMovies(limit, minRatingCount);

        // Then
        StepVerifier.create(result)
                .expectNext(movieId)
                .verifyComplete();

        verify(r2dbcRepository).findTopRatedMovies(limit, minRatingCount);
    }

    @Test
    @DisplayName("Should find ratings with pagination by movie ID")
    void shouldFindRatingsWithPaginationByMovieId() {
        // Given
        int offset = 0;
        int limit = 10;
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findActiveByMovieIdWithPagination(movieId, offset, limit))
                .thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findActiveByMovieIdWithPagination(movieId, offset, limit);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByMovieIdWithPagination(movieId, offset, limit);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find ratings with pagination by user ID")
    void shouldFindRatingsWithPaginationByUserId() {
        // Given
        int offset = 0;
        int limit = 10;
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findActiveByUserIdWithPagination(userId, offset, limit))
                .thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findActiveByUserIdWithPagination(userId, offset, limit);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByUserIdWithPagination(userId, offset, limit);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find recent ratings")
    void shouldFindRecentRatings() {
        // Given
        int limit = 10;
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findRecentRatings(limit)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findRecentRatings(limit);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findRecentRatings(limit);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find ratings by rating value")
    void shouldFindRatingsByRatingValue() {
        // Given
        Integer rating = 5;
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findActiveByRating(rating)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findActiveByRating(rating);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findActiveByRating(rating);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find all ratings by movie ID")
    void shouldFindAllRatingsByMovieId() {
        // Given
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findAllByMovieId(movieId)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findAllByMovieId(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findAllByMovieId(movieId);
        verify(mapper).toDomain(movieRatingEntity);
    }

    @Test
    @DisplayName("Should find all ratings by user ID")
    void shouldFindAllRatingsByUserId() {
        // Given
        List<MovieRatingEntity> entities = List.of(movieRatingEntity);
        when(r2dbcRepository.findAllByUserId(userId)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieRatingEntity)).thenReturn(movieRating);

        // When
        Flux<MovieRating> result = adapter.findAllByUserId(userId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(r2dbcRepository).findAllByUserId(userId);
        verify(mapper).toDomain(movieRatingEntity);
    }
}
