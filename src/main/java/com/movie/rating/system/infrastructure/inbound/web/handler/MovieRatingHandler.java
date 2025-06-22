package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.application.service.ManageMovieRatingService;
import com.movie.rating.system.infrastructure.inbound.web.dto.mapper.MovieDtoMapper;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.CreateMovieRatingRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateMovieRatingRequest;
import com.movie.rating.system.infrastructure.inbound.web.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Handler for movie rating-related HTTP requests.
 * Handles CRUD operations for movie ratings.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieRatingHandler {

    private final ManageMovieRatingService ratingService;
    private final MovieDtoMapper dtoMapper;

    /**
     * Create a new movie rating.
     * Requires authentication.
     */
    public Mono<ServerResponse> createRating(ServerRequest request) {
        UUID userId = AuthenticationUtils.getAuthenticatedUserId(request);
        if (userId == null) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        return request.bodyToMono(CreateMovieRatingRequest.class)
                .doOnNext(this::validateCreateRequest)
                .map(req -> dtoMapper.toCreateCommand(req, userId))
                .flatMap(ratingService::createRating)
                .map(dtoMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> log.debug("Successfully created movie rating"))
                .onErrorResume(this::handleError);
    }

    /**
     * Get a movie rating by ID.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> getRatingById(ServerRequest request) {
        String ratingIdStr = request.pathVariable("id");
        UUID ratingId = UUID.fromString(ratingIdStr);

        return ratingService.getRatingById(ratingId)
                .map(dtoMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnSuccess(response -> log.debug("Successfully retrieved rating: {}", ratingId))
                .onErrorResume(this::handleError);
    }

    /**
     * Update a movie rating.
     * Requires authentication and ownership validation.
     */
    public Mono<ServerResponse> updateRating(ServerRequest request) {
        String ratingIdStr = request.pathVariable("id");
        UUID ratingId = UUID.fromString(ratingIdStr);
        UUID userId = AuthenticationUtils.getAuthenticatedUserId(request);
        
        if (userId == null) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        return request.bodyToMono(UpdateMovieRatingRequest.class)
                .doOnNext(this::validateUpdateRequest)
                .map(req -> dtoMapper.toUpdateCommand(req, ratingId, userId))
                .flatMap(ratingService::updateRating)
                .map(dtoMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> log.debug("Successfully updated rating: {}", ratingId))
                .onErrorResume(this::handleError);
    }

    /**
     * Delete a movie rating.
     * Requires authentication and ownership validation.
     */
    public Mono<ServerResponse> deleteRating(ServerRequest request) {
        String ratingIdStr = request.pathVariable("id");
        UUID ratingId = UUID.fromString(ratingIdStr);
        UUID userId = AuthenticationUtils.getAuthenticatedUserId(request);
        
        if (userId == null) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ratingService.deleteRating(ratingId, userId)
                .then(ServerResponse.noContent().build())
                .doOnSuccess(response -> log.debug("Successfully deleted rating: {}", ratingId))
                .onErrorResume(this::handleError);
    }

    /**
     * Get all ratings for a specific movie.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> getRatingsByMovie(ServerRequest request) {
        String movieIdStr = request.pathVariable("movieId");
        UUID movieId = UUID.fromString(movieIdStr);
        
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);
        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(20);

        // Validate pagination parameters
        if (page < 0 || size <= 0 || size > 100) {
            return ServerResponse.badRequest()
                    .bodyValue("Invalid pagination parameters");
        }

        int offset = page * size;

        return ratingService.getRatingsByMovie(movieId, offset, size)
                .map(dtoMapper::toResponse)
                .collectList()
                .flatMap(ratings -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ratings))
                .doOnSuccess(response -> log.debug("Successfully retrieved ratings for movie: {}", movieId))
                .onErrorResume(this::handleError);
    }

    /**
     * Get all ratings by the current user.
     * Requires authentication.
     */
    public Mono<ServerResponse> getMyRatings(ServerRequest request) {
        UUID userId = AuthenticationUtils.getAuthenticatedUserId(request);
        
        if (userId == null) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);
        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(20);

        // Validate pagination parameters
        if (page < 0 || size <= 0 || size > 100) {
            return ServerResponse.badRequest()
                    .bodyValue("Invalid pagination parameters");
        }

        int offset = page * size;

        return ratingService.getRatingsByUser(userId, offset, size)
                .map(dtoMapper::toResponse)
                .collectList()
                .flatMap(ratings -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ratings))
                .doOnSuccess(response -> log.debug("Successfully retrieved user's ratings"))
                .onErrorResume(this::handleError);
    }

    /**
     * Get rating statistics for a movie.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> getMovieRatingStats(ServerRequest request) {
        String movieIdStr = request.pathVariable("movieId");
        UUID movieId = UUID.fromString(movieIdStr);

        return ratingService.getMovieRatingStatistics(movieId)
                .flatMap(stats -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(stats))
                .doOnSuccess(response -> log.debug("Successfully retrieved rating stats for movie: {}", movieId))
                .onErrorResume(this::handleError);
    }

    /**
     * Get average rating for a movie.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> getMovieAverageRating(ServerRequest request) {
        String movieIdStr = request.pathVariable("movieId");
        UUID movieId = UUID.fromString(movieIdStr);

        return ratingService.getAverageRating(movieId)
                .flatMap(average -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(java.util.Map.of("averageRating", average)))
                .switchIfEmpty(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(java.util.Map.of("averageRating", 0.0)))
                .doOnSuccess(response -> log.debug("Successfully retrieved average rating for movie: {}", movieId))
                .onErrorResume(this::handleError);
    }

    /**
     * Get user's rating for a specific movie.
     * Requires authentication.
     */
    public Mono<ServerResponse> getUserMovieRating(ServerRequest request) {
        String movieIdStr = request.pathVariable("movieId");
        UUID movieId = UUID.fromString(movieIdStr);
        UUID userId = AuthenticationUtils.getAuthenticatedUserId(request);
        
        if (userId == null) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ratingService.getUserRatingForMovie(movieId, userId)
                .map(dtoMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnSuccess(response -> log.debug("Successfully retrieved user rating for movie: {}", movieId))
                .onErrorResume(this::handleError);
    }

    /**
     * Get recent ratings across all movies.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> getRecentRatings(ServerRequest request) {
        int limit = request.queryParam("limit")
                .map(Integer::parseInt)
                .orElse(10);

        // Validate limit parameter
        if (limit <= 0 || limit > 100) {
            return ServerResponse.badRequest()
                    .bodyValue("Invalid limit parameter (must be 1-100)");
        }

        return ratingService.getRecentRatings(limit)
                .map(dtoMapper::toResponse)
                .collectList()
                .flatMap(ratings -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ratings))
                .doOnSuccess(response -> log.debug("Successfully retrieved {} recent ratings", limit))
                .onErrorResume(this::handleError);
    }

    /**
     * Validate create rating request.
     */
    private void validateCreateRequest(CreateMovieRatingRequest request) {
        if (request.movieId() == null) {
            throw new IllegalArgumentException("Movie ID is required");
        }
        if (request.rating() == null) {
            throw new IllegalArgumentException("Rating is required");
        }
        if (request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    /**
     * Validate update rating request.
     */
    private void validateUpdateRequest(UpdateMovieRatingRequest request) {
        if (request.rating() != null && (request.rating() < 1 || request.rating() > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    /**
     * Handle errors and convert them to appropriate HTTP responses.
     */
    private Mono<ServerResponse> handleError(Throwable error) {
        log.error("Error in movie rating handler", error);

        if (error instanceof IllegalArgumentException) {
            return ServerResponse.badRequest()
                    .bodyValue(error.getMessage());
        }

        if (error.getMessage() != null && error.getMessage().contains("not found")) {
            return ServerResponse.notFound().build();
        }

        if (error.getMessage() != null && error.getMessage().contains("not authorized")) {
            return ServerResponse.status(HttpStatus.FORBIDDEN)
                    .bodyValue(error.getMessage());
        }

        if (error.getMessage() != null && error.getMessage().contains("already exists")) {
            return ServerResponse.status(HttpStatus.CONFLICT)
                    .bodyValue(error.getMessage());
        }

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Internal server error");
    }
}
