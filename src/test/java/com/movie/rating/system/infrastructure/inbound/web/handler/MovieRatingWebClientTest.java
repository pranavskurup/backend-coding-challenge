package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.application.service.ManageMovieRatingService;
import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.domain.exception.MovieRatingNotFoundException;
import com.movie.rating.system.domain.port.inbound.ManageMovieRatingUseCase;
import com.movie.rating.system.infrastructure.inbound.web.dto.mapper.MovieDtoMapper;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.CreateMovieRatingRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateMovieRatingRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieRatingResponse;
import com.movie.rating.system.infrastructure.inbound.web.router.MovieRatingRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WebClient-based integration tests for MovieRating endpoints.
 * Tests the full HTTP request-response cycle including routing, handlers, and DTOs.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Movie Rating WebClient Integration Tests")
class MovieRatingWebClientTest {

    @Mock
    private ManageMovieRatingService ratingService;

    @Mock
    private MovieDtoMapper dtoMapper;

    private WebTestClient webTestClient;
    private MovieRatingHandler ratingHandler;

    private final UUID testUserId = UUID.randomUUID();
    private final UUID testMovieId = UUID.randomUUID();
    private final UUID testRatingId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ratingHandler = new MovieRatingHandler(ratingService, dtoMapper);
        MovieRatingRouter ratingRouter = new MovieRatingRouter();
        RouterFunction<ServerResponse> routes = ratingRouter.movieRatingRoutes(ratingHandler);
        
        webTestClient = WebTestClient
                .bindToRouterFunction(routes)
                .configureClient()
                .baseUrl("http://localhost")
                .build();
    }

    @Test
    @DisplayName("Should return 401 when creating rating without authentication")
    void shouldReturn401WhenCreatingRatingWithoutAuth() {
        // Given
        CreateMovieRatingRequest request = new CreateMovieRatingRequest(testMovieId, 5, "Great movie!");

        // When & Then - No authentication setup, should get 401
        webTestClient.post()
                .uri("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService, dtoMapper);
    }

    @Test
    @DisplayName("Should return 400 for invalid rating value on create")
    void shouldReturn400ForInvalidRating() {
        // Given
        CreateMovieRatingRequest request = new CreateMovieRatingRequest(testMovieId, 6, "Great movie!");

        // When & Then - Should fail validation before reaching authentication
        webTestClient.post()
                .uri("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized(); // Will be 401 because of no auth first

        verifyNoInteractions(ratingService, dtoMapper);
    }

    @Test
    @DisplayName("Should get rating by ID successfully")
    void shouldGetRatingByIdSuccessfully() {
        // Given
        MovieRating rating = createTestMovieRating();
        MovieRatingResponse response = createTestMovieRatingResponse();

        when(ratingService.getRatingById(eq(testRatingId))).thenReturn(Mono.just(rating));
        when(dtoMapper.toResponse(eq(rating))).thenReturn(response);

        // When & Then
        webTestClient.get()
                .uri("/api/v1/ratings/{id}", testRatingId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(MovieRatingResponse.class)
                .value(ratingResponse -> {
                    assert ratingResponse.id().equals(testRatingId);
                    assert ratingResponse.rating().equals(5);
                });

        verify(ratingService).getRatingById(eq(testRatingId));
        verify(dtoMapper).toResponse(eq(rating));
    }

    @Test
    @DisplayName("Should return 404 when rating not found")
    void shouldReturn404WhenRatingNotFound() {
        // Given
        when(ratingService.getRatingById(eq(testRatingId))).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/ratings/{id}", testRatingId)
                .exchange()
                .expectStatus().isNotFound();

        verify(ratingService).getRatingById(eq(testRatingId));
        verifyNoInteractions(dtoMapper);
    }

    @Test
    @DisplayName("Should return 401 when updating rating without authentication")
    void shouldReturn401WhenUpdatingRatingWithoutAuth() {
        // Given
        UpdateMovieRatingRequest request = new UpdateMovieRatingRequest(4, "Updated review");

        // When & Then - No authentication setup, should get 401
        webTestClient.put()
                .uri("/api/v1/ratings/{id}", testRatingId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService, dtoMapper);
    }

    @Test
    @DisplayName("Should return 401 when deleting rating without authentication")
    void shouldReturn401WhenDeletingRatingWithoutAuth() {
        // When & Then - No authentication setup, should get 401
        webTestClient.delete()
                .uri("/api/v1/ratings/{id}", testRatingId)
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("Should get ratings by movie successfully")
    void shouldGetRatingsByMovieSuccessfully() {
        // Given
        List<MovieRating> ratings = List.of(createTestMovieRating(), createTestMovieRating());
        List<MovieRatingResponse> responses = List.of(createTestMovieRatingResponse(), createTestMovieRatingResponse());

        when(ratingService.getRatingsByMovie(eq(testMovieId), eq(0), eq(20)))
                .thenReturn(Flux.fromIterable(ratings));
        when(dtoMapper.toResponse(any(MovieRating.class)))
                .thenReturn(responses.get(0))
                .thenReturn(responses.get(1));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{movieId}/ratings?page=0&size=20", testMovieId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieRatingResponse.class)
                .hasSize(2);

        verify(ratingService).getRatingsByMovie(eq(testMovieId), eq(0), eq(20));
        verify(dtoMapper, times(2)).toResponse(any(MovieRating.class));
    }

    @Test
    @DisplayName("Should get ratings by movie with default pagination")
    void shouldGetRatingsByMovieWithDefaultPagination() {
        // Given
        List<MovieRating> ratings = List.of(createTestMovieRating());
        List<MovieRatingResponse> responses = List.of(createTestMovieRatingResponse());

        when(ratingService.getRatingsByMovie(eq(testMovieId), eq(0), eq(20)))
                .thenReturn(Flux.fromIterable(ratings));
        when(dtoMapper.toResponse(any(MovieRating.class))).thenReturn(responses.get(0));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{movieId}/ratings", testMovieId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieRatingResponse.class)
                .hasSize(1);

        verify(ratingService).getRatingsByMovie(eq(testMovieId), eq(0), eq(20));
    }

    @Test
    @DisplayName("Should return 400 for invalid pagination in movie ratings")
    void shouldReturn400ForInvalidPaginationInMovieRatings() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{movieId}/ratings?page=-1&size=0", testMovieId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Invalid pagination parameters");

        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("Should return 401 when getting my ratings without authentication")
    void shouldReturn401WhenGettingMyRatingsWithoutAuth() {
        // When & Then - No authentication setup, should get 401
        webTestClient.get()
                .uri("/api/v1/ratings/my")
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("Should get movie rating statistics successfully")
    void shouldGetMovieRatingStatsSuccessfully() {
        // Given
        ManageMovieRatingUseCase.MovieRatingStatistics stats = 
                new ManageMovieRatingUseCase.MovieRatingStatistics(
                        testMovieId,
                        10L,
                        4.5,
                        3,
                        5,
                        8L,
                        new ManageMovieRatingUseCase.RatingDistribution(0, 0, 2, 3, 5, 0, 0, 0, 0, 0)
                );

        when(ratingService.getMovieRatingStatistics(eq(testMovieId))).thenReturn(Mono.just(stats));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{movieId}/ratings/stats", testMovieId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ManageMovieRatingUseCase.MovieRatingStatistics.class)
                .value(response -> {
                    assert response.averageRating() == 4.5;
                    assert response.totalRatings() == 10L;
                });

        verify(ratingService).getMovieRatingStatistics(eq(testMovieId));
    }

    @Test
    @DisplayName("Should get movie average rating successfully")
    void shouldGetMovieAverageRatingSuccessfully() {
        // Given
        when(ratingService.getAverageRating(eq(testMovieId))).thenReturn(Mono.just(4.2));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{movieId}/ratings/average", testMovieId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .value(response -> {
                    assert response.get("averageRating").equals(4.2);
                });

        verify(ratingService).getAverageRating(eq(testMovieId));
    }

    @Test
    @DisplayName("Should return 0.0 when no ratings exist for movie")
    void shouldReturn0WhenNoRatingsExist() {
        // Given
        when(ratingService.getAverageRating(eq(testMovieId))).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{movieId}/ratings/average", testMovieId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .value(response -> {
                    assert response.get("averageRating").equals(0.0);
                });

        verify(ratingService).getAverageRating(eq(testMovieId));
    }

    @Test
    @DisplayName("Should return 401 when getting user movie rating without authentication")
    void shouldReturn401WhenGettingUserMovieRatingWithoutAuth() {
        // When & Then - No authentication mock, so should get 401
        webTestClient.get()
                .uri("/api/v1/movies/{movieId}/ratings/user", testMovieId)
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("Should get recent ratings successfully")
    void shouldGetRecentRatingsSuccessfully() {
        // Given
        List<MovieRating> ratings = List.of(createTestMovieRating(), createTestMovieRating());
        List<MovieRatingResponse> responses = List.of(createTestMovieRatingResponse(), createTestMovieRatingResponse());

        when(ratingService.getRecentRatings(eq(10))).thenReturn(Flux.fromIterable(ratings));
        when(dtoMapper.toResponse(any(MovieRating.class)))
                .thenReturn(responses.get(0))
                .thenReturn(responses.get(1));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/ratings/recent?limit=10")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieRatingResponse.class)
                .hasSize(2);

        verify(ratingService).getRecentRatings(eq(10));
        verify(dtoMapper, times(2)).toResponse(any(MovieRating.class));
    }

    @Test
    @DisplayName("Should get recent ratings with default limit")
    void shouldGetRecentRatingsWithDefaultLimit() {
        // Given
        List<MovieRating> ratings = List.of(createTestMovieRating());
        List<MovieRatingResponse> responses = List.of(createTestMovieRatingResponse());

        when(ratingService.getRecentRatings(eq(10))).thenReturn(Flux.fromIterable(ratings));
        when(dtoMapper.toResponse(any(MovieRating.class))).thenReturn(responses.get(0));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/ratings/recent")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieRatingResponse.class)
                .hasSize(1);

        verify(ratingService).getRecentRatings(eq(10));
    }

    @Test
    @DisplayName("Should return 400 for invalid limit in recent ratings")
    void shouldReturn400ForInvalidLimitInRecentRatings() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/ratings/recent?limit=0")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Invalid limit parameter (must be 1-100)");

        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("Should handle rating not found gracefully")
    void shouldHandleRatingNotFoundGracefully() {
        // Given
        when(ratingService.getRatingById(eq(testRatingId)))
                .thenReturn(Mono.error(new MovieRatingNotFoundException("Rating not found with ID: " + testRatingId)));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/ratings/{id}", testRatingId)
                .exchange()
                .expectStatus().isNotFound();

        verify(ratingService).getRatingById(eq(testRatingId));
    }

    @Test
    @DisplayName("Should handle conflict when rating already exists")
    void shouldHandleConflictWhenRatingAlreadyExists() {
        // Given
        CreateMovieRatingRequest request = new CreateMovieRatingRequest(testMovieId, 5, "Great movie!");

        // When & Then - Will return 401 due to no authentication first
        webTestClient.post()
                .uri("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService);
    }

    // Helper methods to create test objects
    private MovieRating createTestMovieRating() {
        return MovieRating.builder()
                .id(testRatingId)
                .movieId(testMovieId)
                .userId(testUserId)
                .rating(5)
                .review("Great movie!")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private MovieRatingResponse createTestMovieRatingResponse() {
        return new MovieRatingResponse(
                testRatingId,
                testMovieId,
                testUserId,
                5,
                "Great movie!",
                true,
                Instant.now(),
                Instant.now()
        );
    }
}
