package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.application.service.ManageMovieRatingService;
import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.domain.exception.UnauthorizedMovieOperationException;
import com.movie.rating.system.domain.port.inbound.ManageMovieRatingUseCase;
import com.movie.rating.system.infrastructure.inbound.web.dto.mapper.MovieDtoMapper;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.CreateMovieRatingRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateMovieRatingRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieRatingResponse;
import com.movie.rating.system.infrastructure.inbound.web.router.MovieRatingRouter;
import com.movie.rating.system.infrastructure.inbound.web.util.AuthenticationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for authenticated MovieRating endpoints focusing on authentication behavior.
 * This class tests 401 responses for endpoints that require authentication
 * and uses direct handler testing for authenticated success scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Movie Rating Authentication WebClient Tests")
class MovieRatingAuthenticatedWebClientTest {

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

    // ========== WebTestClient Tests for 401 Responses ==========

    @Test
    @DisplayName("Should return 401 when creating rating without authentication")
    void shouldReturn401WhenCreatingRatingWithoutAuth() {
        // Given
        CreateMovieRatingRequest request = new CreateMovieRatingRequest(testMovieId, 5, "Great movie!");

        // When & Then - Without authentication, should get 401
        webTestClient.post()
                .uri("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService, dtoMapper);
    }

    @Test
    @DisplayName("Should return 401 when updating rating without authentication")
    void shouldReturn401WhenUpdatingRatingWithoutAuth() {
        // Given
        UpdateMovieRatingRequest request = new UpdateMovieRatingRequest(4, "Updated review");

        // When & Then
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
        // When & Then
        webTestClient.delete()
                .uri("/api/v1/ratings/{id}", testRatingId)
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("Should return 401 when getting my ratings without authentication")
    void shouldReturn401WhenGettingMyRatingsWithoutAuth() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/ratings/my")
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("Should return 401 when getting user movie rating without authentication")
    void shouldReturn401WhenGettingUserMovieRatingWithoutAuth() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{movieId}/ratings/user", testMovieId)
                .exchange()
                .expectStatus().isUnauthorized();

        verifyNoInteractions(ratingService);
    }

    // ========== Direct Handler Tests with Authentication Mocking ==========

    @Test
    @DisplayName("Should create rating successfully with proper authentication")
    void shouldCreateRatingSuccessfullyWithAuth() {
        // Given
        CreateMovieRatingRequest request = new CreateMovieRatingRequest(testMovieId, 5, "Great movie!");
        MovieRating rating = createTestMovieRating();
        MovieRatingResponse response = createTestMovieRatingResponse();
        ManageMovieRatingUseCase.CreateRatingCommand command =
                new ManageMovieRatingUseCase.CreateRatingCommand(testMovieId, testUserId, 5, "Great movie!");

        when(dtoMapper.toCreateCommand(eq(request), eq(testUserId))).thenReturn(command);
        when(ratingService.createRating(eq(command))).thenReturn(Mono.just(rating));
        when(dtoMapper.toResponse(eq(rating))).thenReturn(response);

        // Mock ServerRequest with authentication
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.bodyToMono(CreateMovieRatingRequest.class)).thenReturn(Mono.just(request));

        try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
            authUtils.when(() -> AuthenticationUtils.getAuthenticatedUserId(eq(mockRequest)))
                    .thenReturn(testUserId);

            // When
            Mono<ServerResponse> result = ratingHandler.createRating(mockRequest);

            // Then - Verify successful creation
            StepVerifier.create(result)
                    .expectNextMatches(serverResponse ->
                        serverResponse.statusCode().value() == 201) // HTTP 201 Created
                    .verifyComplete();

            verify(dtoMapper).toCreateCommand(eq(request), eq(testUserId));
            verify(ratingService).createRating(eq(command));
            verify(dtoMapper).toResponse(eq(rating));
        }
    }

    @Test
    @DisplayName("Should update rating successfully with proper authentication")
    void shouldUpdateRatingSuccessfullyWithAuth() {
        // Given
        UpdateMovieRatingRequest request = new UpdateMovieRatingRequest(4, "Updated review");
        MovieRating rating = createTestMovieRating();
        MovieRatingResponse response = createTestMovieRatingResponse();
        ManageMovieRatingUseCase.UpdateRatingCommand command =
                new ManageMovieRatingUseCase.UpdateRatingCommand(testRatingId, testUserId, 4, "Updated review");

        when(dtoMapper.toUpdateCommand(eq(request), eq(testRatingId), eq(testUserId))).thenReturn(command);
        when(ratingService.updateRating(eq(command))).thenReturn(Mono.just(rating));
        when(dtoMapper.toResponse(eq(rating))).thenReturn(response);

        // Mock ServerRequest
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.pathVariable("id")).thenReturn(testRatingId.toString());
        when(mockRequest.bodyToMono(UpdateMovieRatingRequest.class)).thenReturn(Mono.just(request));

        try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
            authUtils.when(() -> AuthenticationUtils.getAuthenticatedUserId(eq(mockRequest)))
                    .thenReturn(testUserId);

            // When
            Mono<ServerResponse> result = ratingHandler.updateRating(mockRequest);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(serverResponse ->
                        serverResponse.statusCode().value() == 200) // HTTP 200 OK
                    .verifyComplete();

            verify(dtoMapper).toUpdateCommand(eq(request), eq(testRatingId), eq(testUserId));
            verify(ratingService).updateRating(eq(command));
            verify(dtoMapper).toResponse(eq(rating));
        }
    }

    @Test
    @DisplayName("Should delete rating successfully with proper authentication")
    void shouldDeleteRatingSuccessfullyWithAuth() {
        // Given
        when(ratingService.deleteRating(eq(testRatingId), eq(testUserId))).thenReturn(Mono.empty());

        // Mock ServerRequest
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.pathVariable("id")).thenReturn(testRatingId.toString());

        try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
            authUtils.when(() -> AuthenticationUtils.getAuthenticatedUserId(eq(mockRequest)))
                    .thenReturn(testUserId);

            // When
            Mono<ServerResponse> result = ratingHandler.deleteRating(mockRequest);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(serverResponse ->
                        serverResponse.statusCode().value() == 204) // HTTP 204 No Content
                    .verifyComplete();

            verify(ratingService).deleteRating(eq(testRatingId), eq(testUserId));
        }
    }

    @Test
    @DisplayName("Should get my ratings successfully with proper authentication")
    void shouldGetMyRatingsSuccessfullyWithAuth() {
        // Given
        List<MovieRating> ratings = List.of(createTestMovieRating());
        List<MovieRatingResponse> responses = List.of(createTestMovieRatingResponse());

        when(ratingService.getRatingsByUser(eq(testUserId), eq(0), eq(20)))
                .thenReturn(Flux.fromIterable(ratings));
        when(dtoMapper.toResponse(any(MovieRating.class))).thenReturn(responses.get(0));

        // Mock ServerRequest
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.queryParam("page")).thenReturn(Optional.empty());
        when(mockRequest.queryParam("size")).thenReturn(Optional.empty());

        try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
            authUtils.when(() -> AuthenticationUtils.getAuthenticatedUserId(eq(mockRequest)))
                    .thenReturn(testUserId);

            // When
            Mono<ServerResponse> result = ratingHandler.getMyRatings(mockRequest);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(serverResponse ->
                        serverResponse.statusCode().value() == 200) // HTTP 200 OK
                    .verifyComplete();

            verify(ratingService).getRatingsByUser(eq(testUserId), eq(0), eq(20));
        }
    }

    @Test
    @DisplayName("Should get user movie rating successfully with proper authentication")
    void shouldGetUserMovieRatingSuccessfullyWithAuth() {
        // Given
        MovieRating rating = createTestMovieRating();
        MovieRatingResponse response = createTestMovieRatingResponse();

        when(ratingService.getUserRatingForMovie(eq(testMovieId), eq(testUserId)))
                .thenReturn(Mono.just(rating));
        when(dtoMapper.toResponse(eq(rating))).thenReturn(response);

        // Mock ServerRequest
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.pathVariable("movieId")).thenReturn(testMovieId.toString());

        try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
            authUtils.when(() -> AuthenticationUtils.getAuthenticatedUserId(eq(mockRequest)))
                    .thenReturn(testUserId);

            // When
            Mono<ServerResponse> result = ratingHandler.getUserMovieRating(mockRequest);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(serverResponse ->
                        serverResponse.statusCode().value() == 200)
                    .verifyComplete();

            verify(ratingService).getUserRatingForMovie(eq(testMovieId), eq(testUserId));
            verify(dtoMapper).toResponse(eq(rating));
        }
    }

    @Test
    @DisplayName("Should handle unauthorized operation gracefully in handler")
    void shouldHandleUnauthorizedOperationGracefullyInHandler() {
        // Given
        UpdateMovieRatingRequest request = new UpdateMovieRatingRequest(4, "Updated review");
        ManageMovieRatingUseCase.UpdateRatingCommand command =
                new ManageMovieRatingUseCase.UpdateRatingCommand(testRatingId, testUserId, 4, "Updated review");

        when(dtoMapper.toUpdateCommand(eq(request), eq(testRatingId), eq(testUserId))).thenReturn(command);
        when(ratingService.updateRating(eq(command)))
                .thenReturn(Mono.error(new UnauthorizedMovieOperationException("User not authorized to update this rating")));

        // Mock ServerRequest
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.pathVariable("id")).thenReturn(testRatingId.toString());
        when(mockRequest.bodyToMono(UpdateMovieRatingRequest.class)).thenReturn(Mono.just(request));

        try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
            authUtils.when(() -> AuthenticationUtils.getAuthenticatedUserId(eq(mockRequest)))
                    .thenReturn(testUserId);

            // When
            Mono<ServerResponse> result = ratingHandler.updateRating(mockRequest);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(serverResponse ->
                        serverResponse.statusCode().value() == 403) // HTTP 403 Forbidden
                    .verifyComplete();

            verify(ratingService).updateRating(eq(command));
        }
    }

    @Test
    @DisplayName("Should handle validation error in handler with authentication")
    void shouldHandleValidationErrorInHandlerWithAuth() {
        // Given - Invalid rating value
        CreateMovieRatingRequest request = new CreateMovieRatingRequest(testMovieId, 6, "Great movie!");

        // Mock ServerRequest
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.bodyToMono(CreateMovieRatingRequest.class)).thenReturn(Mono.just(request));

        try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
            authUtils.when(() -> AuthenticationUtils.getAuthenticatedUserId(eq(mockRequest)))
                    .thenReturn(testUserId);

            // When
            Mono<ServerResponse> result = ratingHandler.createRating(mockRequest);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(serverResponse ->
                        serverResponse.statusCode().value() == 400) // HTTP 400 Bad Request
                    .verifyComplete();

            verifyNoInteractions(ratingService, dtoMapper);
        }
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
