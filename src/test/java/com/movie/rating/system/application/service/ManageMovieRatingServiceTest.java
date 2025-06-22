package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.domain.exception.*;
import com.movie.rating.system.domain.port.inbound.ManageMovieRatingUseCase.*;
import com.movie.rating.system.domain.port.outbound.MovieRatingRepository;
import com.movie.rating.system.domain.port.outbound.MovieRepository;
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
@DisplayName("ManageMovieRatingService Tests")
class ManageMovieRatingServiceTest {

    @Mock
    private MovieRatingRepository movieRatingRepository;

    @Mock
    private MovieRepository movieRepository;

    private ManageMovieRatingService service;

    private UUID movieId;
    private UUID userId;
    private UUID ratingId;
    private MovieRating movieRating;
    private CreateRatingCommand createCommand;
    private UpdateRatingCommand updateCommand;
    private Instant now;

    @BeforeEach
    void setUp() {
        service = new ManageMovieRatingService(movieRatingRepository, movieRepository);
        
        movieId = UUID.randomUUID();
        userId = UUID.randomUUID();
        ratingId = UUID.randomUUID();
        now = Instant.now();
        
        movieRating = MovieRating.builder()
                .id(ratingId)
                .movieId(movieId)
                .userId(userId)
                .rating(5)
                .review("Great movie!")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        createCommand = new CreateRatingCommand(movieId, userId, 5, "Great movie!");
        updateCommand = new UpdateRatingCommand(ratingId, userId, 4, "Good movie!");
    }

    @Test
    @DisplayName("Should successfully create movie rating")
    void shouldCreateMovieRating() {
        // Given
        when(movieRepository.existsById(movieId)).thenReturn(Mono.just(true));
        when(movieRatingRepository.existsActiveByMovieIdAndUserId(movieId, userId)).thenReturn(Mono.just(false));
        when(movieRatingRepository.save(any(MovieRating.class))).thenReturn(Mono.just(movieRating));

        // When
        Mono<MovieRating> result = service.createRating(createCommand);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRepository).existsById(movieId);
        verify(movieRatingRepository).existsActiveByMovieIdAndUserId(movieId, userId);
        verify(movieRatingRepository).save(any(MovieRating.class));
    }

    @Test
    @DisplayName("Should throw MovieNotFoundException when movie does not exist")
    void shouldThrowMovieNotFoundExceptionWhenMovieDoesNotExist() {
        // Given
        when(movieRepository.existsById(movieId)).thenReturn(Mono.just(false));

        // When
        Mono<MovieRating> result = service.createRating(createCommand);

        // Then
        StepVerifier.create(result)
                .expectError(MovieNotFoundException.class)
                .verify();

        verify(movieRepository).existsById(movieId);
        verify(movieRatingRepository, never()).existsActiveByMovieIdAndUserId(any(), any());
        verify(movieRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateRatingException when user already rated movie")
    void shouldThrowDuplicateRatingExceptionWhenUserAlreadyRatedMovie() {
        // Given
        when(movieRepository.existsById(movieId)).thenReturn(Mono.just(true));
        when(movieRatingRepository.existsActiveByMovieIdAndUserId(movieId, userId)).thenReturn(Mono.just(true));

        // When
        Mono<MovieRating> result = service.createRating(createCommand);

        // Then
        StepVerifier.create(result)
                .expectError(DuplicateRatingException.class)
                .verify();

        verify(movieRepository).existsById(movieId);
        verify(movieRatingRepository).existsActiveByMovieIdAndUserId(movieId, userId);
        verify(movieRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully get rating by ID")
    void shouldGetRatingById() {
        // Given
        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.just(movieRating));

        // When
        Mono<MovieRating> result = service.getRatingById(ratingId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findById(ratingId);
    }

    @Test
    @DisplayName("Should throw MovieRatingNotFoundException when rating not found")
    void shouldThrowMovieRatingNotFoundExceptionWhenRatingNotFound() {
        // Given
        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.empty());

        // When
        Mono<MovieRating> result = service.getRatingById(ratingId);

        // Then
        StepVerifier.create(result)
                .expectError(MovieRatingNotFoundException.class)
                .verify();

        verify(movieRatingRepository).findById(ratingId);
    }

    @Test
    @DisplayName("Should successfully update movie rating")
    void shouldUpdateMovieRating() {
        // Given
        MovieRating updatedRating = movieRating.toBuilder()
                .rating(4)
                .review("Good movie!")
                .updatedAt(Instant.now())
                .build();

        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.just(movieRating));
        when(movieRatingRepository.save(any(MovieRating.class))).thenReturn(Mono.just(updatedRating));

        // When
        Mono<MovieRating> result = service.updateRating(updateCommand);

        // Then
        StepVerifier.create(result)
                .expectNext(updatedRating)
                .verifyComplete();

        verify(movieRatingRepository).findById(ratingId);
        verify(movieRatingRepository).save(any(MovieRating.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedMovieOperationException when user not authorized to update")
    void shouldThrowUnauthorizedExceptionWhenUserNotAuthorizedToUpdate() {
        // Given
        UUID unauthorizedUserId = UUID.randomUUID();
        UpdateRatingCommand unauthorizedCommand = new UpdateRatingCommand(ratingId, unauthorizedUserId, 4, "Good movie!");
        
        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.just(movieRating));

        // When
        Mono<MovieRating> result = service.updateRating(unauthorizedCommand);

        // Then
        StepVerifier.create(result)
                .expectError(UnauthorizedMovieOperationException.class)
                .verify();

        verify(movieRatingRepository).findById(ratingId);
        verify(movieRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully delete movie rating")
    void shouldDeleteMovieRating() {
        // Given
        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.just(movieRating));
        when(movieRatingRepository.deleteById(ratingId)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = service.deleteRating(ratingId, userId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(movieRatingRepository).findById(ratingId);
        verify(movieRatingRepository).deleteById(ratingId);
    }

    @Test
    @DisplayName("Should throw UnauthorizedMovieOperationException when user not authorized to delete")
    void shouldThrowUnauthorizedExceptionWhenUserNotAuthorizedToDelete() {
        // Given
        UUID unauthorizedUserId = UUID.randomUUID();
        
        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.just(movieRating));

        // When
        Mono<Void> result = service.deleteRating(ratingId, unauthorizedUserId);

        // Then
        StepVerifier.create(result)
                .expectError(UnauthorizedMovieOperationException.class)
                .verify();

        verify(movieRatingRepository).findById(ratingId);
        verify(movieRatingRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should get ratings by movie")
    void shouldGetRatingsByMovie() {
        // Given
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByMovieId(movieId)).thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.getRatingsByMovie(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByMovieId(movieId);
    }

    @Test
    @DisplayName("Should get ratings by user")
    void shouldGetRatingsByUser() {
        // Given
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByUserId(userId)).thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.getRatingsByUser(userId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByUserId(userId);
    }

    @Test
    @DisplayName("Should get user rating for movie")
    void shouldGetUserRatingForMovie() {
        // Given
        when(movieRatingRepository.findActiveByMovieIdAndUserId(movieId, userId)).thenReturn(Mono.just(movieRating));

        // When
        Mono<MovieRating> result = service.getUserRatingForMovie(movieId, userId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByMovieIdAndUserId(movieId, userId);
    }

    @Test
    @DisplayName("Should return empty when user has not rated movie")
    void shouldReturnEmptyWhenUserHasNotRatedMovie() {
        // Given
        when(movieRatingRepository.findActiveByMovieIdAndUserId(movieId, userId)).thenReturn(Mono.empty());

        // When
        Mono<MovieRating> result = service.getUserRatingForMovie(movieId, userId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByMovieIdAndUserId(movieId, userId);
    }

    @Test
    @DisplayName("Should get ratings by movie and rating range")
    void shouldGetRatingsByMovieAndRange() {
        // Given
        Integer minRating = 3;
        Integer maxRating = 5;
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByMovieIdAndRatingBetween(movieId, minRating, maxRating))
                .thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.getRatingsByMovieAndRange(movieId, minRating, maxRating);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByMovieIdAndRatingBetween(movieId, minRating, maxRating);
    }

    @Test
    @DisplayName("Should get ratings with reviews by movie")
    void shouldGetRatingsWithReviewsByMovie() {
        // Given
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByMovieIdWithReviews(movieId)).thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.getRatingsWithReviewsByMovie(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByMovieIdWithReviews(movieId);
    }

    @Test
    @DisplayName("Should search ratings by movie ID")
    void shouldSearchRatingsByMovieId() {
        // Given
        SearchRatingsCommand searchCommand = new SearchRatingsCommand(
                movieId, null, null, null, null, null, null, null, null);
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByMovieId(movieId)).thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.searchRatings(searchCommand);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByMovieId(movieId);
    }

    @Test
    @DisplayName("Should search ratings by user ID")
    void shouldSearchRatingsByUserId() {
        // Given
        SearchRatingsCommand searchCommand = new SearchRatingsCommand(
                null, userId, null, null, null, null, null, null, null);
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByUserId(userId)).thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.searchRatings(searchCommand);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByUserId(userId);
    }

    @Test
    @DisplayName("Should search ratings by date range")
    void shouldSearchRatingsByDateRange() {
        // Given
        Instant startDate = now.minusSeconds(3600);
        Instant endDate = now;
        SearchRatingsCommand searchCommand = new SearchRatingsCommand(
                null, null, null, null, null, startDate, endDate, null, null);
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByCreatedAtBetween(startDate, endDate))
                .thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.searchRatings(searchCommand);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByCreatedAtBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should get recent ratings with default limit")
    void shouldGetRecentRatingsWithDefaultLimit() {
        // Given
        SearchRatingsCommand searchCommand = new SearchRatingsCommand(
                null, null, null, null, null, null, null, null, null);
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findRecentRatings(50)).thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.searchRatings(searchCommand);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findRecentRatings(50);
    }

    @Test
    @DisplayName("Should get movie rating statistics")
    void shouldGetMovieRatingStatistics() {
        // Given
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.countActiveByMovieId(movieId)).thenReturn(Mono.just(1L));
        when(movieRatingRepository.calculateAverageRatingByMovieId(movieId)).thenReturn(Mono.just(5.0));
        when(movieRatingRepository.findActiveByMovieId(movieId)).thenReturn(Flux.fromIterable(ratings));

        // When
        Mono<MovieRatingStatistics> result = service.getMovieRatingStatistics(movieId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(stats -> 
                    stats.movieId().equals(movieId) &&
                    stats.totalRatings() == 1L &&
                    stats.averageRating() == 5.0 &&
                    stats.minRating() == 5 &&
                    stats.maxRating() == 5 &&
                    stats.ratingsWithReviews() == 1L
                )
                .verifyComplete();

        verify(movieRatingRepository).countActiveByMovieId(movieId);
        verify(movieRatingRepository).calculateAverageRatingByMovieId(movieId);
        verify(movieRatingRepository).findActiveByMovieId(movieId);
    }

    @Test
    @DisplayName("Should get movie rating statistics for movie with no ratings")
    void shouldGetMovieRatingStatisticsForMovieWithNoRatings() {
        // Given
        when(movieRatingRepository.countActiveByMovieId(movieId)).thenReturn(Mono.just(0L));
        when(movieRatingRepository.calculateAverageRatingByMovieId(movieId)).thenReturn(Mono.empty());
        when(movieRatingRepository.findActiveByMovieId(movieId)).thenReturn(Flux.empty());

        // When
        Mono<MovieRatingStatistics> result = service.getMovieRatingStatistics(movieId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(stats -> 
                    stats.movieId().equals(movieId) &&
                    stats.totalRatings() == 0L &&
                    stats.averageRating() == 0.0 &&
                    stats.minRating() == 0 &&
                    stats.maxRating() == 0 &&
                    stats.ratingsWithReviews() == 0L
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get user rating statistics")
    void shouldGetUserRatingStatistics() {
        // Given
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByUserId(userId)).thenReturn(Flux.fromIterable(ratings));

        // When
        Mono<UserRatingStatistics> result = service.getUserRatingStatistics(userId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(stats -> 
                    stats.userId().equals(userId) &&
                    stats.totalRatings() == 1 &&
                    stats.averageRatingGiven() == 5.0 &&
                    stats.minRatingGiven() == 5 &&
                    stats.maxRatingGiven() == 5 &&
                    stats.ratingsWithReviews() == 1L
                )
                .verifyComplete();

        verify(movieRatingRepository).findActiveByUserId(userId);
    }

    @Test
    @DisplayName("Should get user rating statistics for user with no ratings")
    void shouldGetUserRatingStatisticsForUserWithNoRatings() {
        // Given
        when(movieRatingRepository.findActiveByUserId(userId)).thenReturn(Flux.empty());

        // When
        Mono<UserRatingStatistics> result = service.getUserRatingStatistics(userId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(stats -> 
                    stats.userId().equals(userId) &&
                    stats.totalRatings() == 0 &&
                    stats.averageRatingGiven() == 0.0 &&
                    stats.minRatingGiven() == 0 &&
                    stats.maxRatingGiven() == 0 &&
                    stats.ratingsWithReviews() == 0L
                )
                .verifyComplete();

        verify(movieRatingRepository).findActiveByUserId(userId);
    }

    @Test
    @DisplayName("Should get recent ratings")
    void shouldGetRecentRatings() {
        // Given
        int limit = 10;
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findRecentRatings(limit)).thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.getRecentRatings(limit);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findRecentRatings(limit);
    }

    @Test
    @DisplayName("Should get top rated movies")
    void shouldGetTopRatedMovies() {
        // Given
        int limit = 5;
        int minRatingCount = 10;
        when(movieRatingRepository.findTopRatedMovies(limit, minRatingCount)).thenReturn(Flux.just(movieId));
        when(movieRatingRepository.calculateAverageRatingByMovieId(movieId)).thenReturn(Mono.just(4.5));
        when(movieRatingRepository.countActiveByMovieId(movieId)).thenReturn(Mono.just(20L));

        // When
        Flux<TopRatedMovie> result = service.getTopRatedMovies(limit, minRatingCount);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(topRated -> 
                    topRated.movieId().equals(movieId) &&
                    topRated.averageRating() == 4.5 &&
                    topRated.totalRatings() == 20L
                )
                .verifyComplete();

        verify(movieRatingRepository).findTopRatedMovies(limit, minRatingCount);
        verify(movieRatingRepository).calculateAverageRatingByMovieId(movieId);
        verify(movieRatingRepository).countActiveByMovieId(movieId);
    }

    @Test
    @DisplayName("Should check if user has rated movie")
    void shouldCheckIfUserHasRatedMovie() {
        // Given
        when(movieRatingRepository.existsActiveByMovieIdAndUserId(movieId, userId)).thenReturn(Mono.just(true));

        // When
        Mono<Boolean> result = service.hasUserRatedMovie(movieId, userId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(movieRatingRepository).existsActiveByMovieIdAndUserId(movieId, userId);
    }

    @Test
    @DisplayName("Should check if user can modify rating")
    void shouldCheckIfUserCanModifyRating() {
        // Given
        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.just(movieRating));

        // When
        Mono<Boolean> result = service.canUserModifyRating(ratingId, userId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(movieRatingRepository).findById(ratingId);
    }

    @Test
    @DisplayName("Should return false when user cannot modify rating")
    void shouldReturnFalseWhenUserCannotModifyRating() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.just(movieRating));

        // When
        Mono<Boolean> result = service.canUserModifyRating(ratingId, differentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(movieRatingRepository).findById(ratingId);
    }

    @Test
    @DisplayName("Should return false when rating not found for modification check")
    void shouldReturnFalseWhenRatingNotFoundForModificationCheck() {
        // Given
        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.empty());

        // When
        Mono<Boolean> result = service.canUserModifyRating(ratingId, userId);

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(movieRatingRepository).findById(ratingId);
    }

    // Additional web handler support method tests

    @Test
    @DisplayName("Should get ratings by movie with pagination")
    void shouldGetRatingsByMovieWithPagination() {
        // Given
        int offset = 0;
        int limit = 10;
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByMovieIdWithPagination(movieId, offset, limit))
                .thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.getRatingsByMovie(movieId, offset, limit);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByMovieIdWithPagination(movieId, offset, limit);
    }

    @Test
    @DisplayName("Should get ratings by user with pagination")
    void shouldGetRatingsByUserWithPagination() {
        // Given
        int offset = 0;
        int limit = 10;
        List<MovieRating> ratings = List.of(movieRating);
        when(movieRatingRepository.findActiveByUserIdWithPagination(userId, offset, limit))
                .thenReturn(Flux.fromIterable(ratings));

        // When
        Flux<MovieRating> result = service.getRatingsByUser(userId, offset, limit);

        // Then
        StepVerifier.create(result)
                .expectNext(movieRating)
                .verifyComplete();

        verify(movieRatingRepository).findActiveByUserIdWithPagination(userId, offset, limit);
    }

    @Test
    @DisplayName("Should get average rating for movie")
    void shouldGetAverageRatingForMovie() {
        // Given
        Double expectedAverage = 4.5;
        when(movieRatingRepository.calculateAverageRatingByMovieId(movieId)).thenReturn(Mono.just(expectedAverage));

        // When
        Mono<Double> result = service.getAverageRating(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedAverage)
                .verifyComplete();

        verify(movieRatingRepository).calculateAverageRatingByMovieId(movieId);
    }

    @Test
    @DisplayName("Should handle errors in create rating")
    void shouldHandleErrorsInCreateRating() {
        // Given
        when(movieRepository.existsById(movieId)).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When
        Mono<MovieRating> result = service.createRating(createCommand);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(movieRepository).existsById(movieId);
    }

    @Test
    @DisplayName("Should handle errors in update rating")
    void shouldHandleErrorsInUpdateRating() {
        // Given
        when(movieRatingRepository.findById(ratingId)).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When
        Mono<MovieRating> result = service.updateRating(updateCommand);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(movieRatingRepository).findById(ratingId);
    }
}
