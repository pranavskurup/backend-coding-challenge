package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.application.service.ManageMovieService;
import com.movie.rating.system.infrastructure.inbound.web.dto.mapper.MovieDtoMapper;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.CreateMovieRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateMovieRequest;
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
 * Handler for movie-related HTTP requests.
 * Handles CRUD operations and search functionality for movies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieHandler {

    private final ManageMovieService movieService;
    private final MovieDtoMapper dtoMapper;

    /**
     * Create a new movie.
     * Requires authentication.
     */
    public Mono<ServerResponse> createMovie(ServerRequest request) {
        UUID userId = AuthenticationUtils.getAuthenticatedUserId(request);
        if (userId == null) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        return request.bodyToMono(CreateMovieRequest.class)
                .doOnNext(this::validateCreateRequest)
                .map(req -> dtoMapper.toCreateCommand(req, userId))
                .flatMap(movieService::createMovie)
                .map(dtoMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> log.debug("Successfully created movie"))
                .onErrorResume(this::handleError);
    }

    /**
     * Get a movie by ID.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> getMovieById(ServerRequest request) {
        String movieIdStr = request.pathVariable("id");
        UUID movieId = UUID.fromString(movieIdStr);

        return movieService.getMovieById(movieId)
                .map(dtoMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnSuccess(response -> log.debug("Successfully retrieved movie: {}", movieId))
                .onErrorResume(this::handleError);
    }

    /**
     * Update a movie.
     * Requires authentication and ownership validation.
     */
    public Mono<ServerResponse> updateMovie(ServerRequest request) {
        String movieIdStr = request.pathVariable("id");
        UUID movieId = UUID.fromString(movieIdStr);
        UUID userId = AuthenticationUtils.getAuthenticatedUserId(request);
        
        if (userId == null) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        return request.bodyToMono(UpdateMovieRequest.class)
                .doOnNext(this::validateUpdateRequest)
                .map(req -> dtoMapper.toUpdateCommand(req, movieId, userId))
                .flatMap(movieService::updateMovie)
                .map(dtoMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> log.debug("Successfully updated movie: {}", movieId))
                .onErrorResume(this::handleError);
    }

    /**
     * Delete a movie.
     * Requires authentication and ownership validation.
     */
    public Mono<ServerResponse> deleteMovie(ServerRequest request) {
        String movieIdStr = request.pathVariable("id");
        UUID movieId = UUID.fromString(movieIdStr);
        UUID userId = AuthenticationUtils.getAuthenticatedUserId(request);
        
        if (userId == null) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        return movieService.deleteMovie(movieId, userId)
                .then(ServerResponse.noContent().build())
                .doOnSuccess(response -> log.debug("Successfully deleted movie: {}", movieId))
                .onErrorResume(this::handleError);
    }

    /**
     * Get all active movies with optional pagination.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> getAllMovies(ServerRequest request) {
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

        return movieService.getAllActiveMovies(offset, size)
                .map(dtoMapper::toResponse)
                .collectList()
                .flatMap(movies -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(movies))
                .doOnSuccess(response -> log.debug("Successfully retrieved movies page: {}, size: {}", page, size))
                .onErrorResume(this::handleError);
    }

    /**
     * Get movies created by the current user.
     * Requires authentication.
     */
    public Mono<ServerResponse> getMyMovies(ServerRequest request) {
        UUID userId = AuthenticationUtils.getAuthenticatedUserId(request);
        
        if (userId == null) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        return movieService.getMoviesByCreator(userId)
                .map(dtoMapper::toResponse)
                .collectList()
                .flatMap(movies -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(movies))
                .doOnSuccess(response -> log.debug("Successfully retrieved user's movies"))
                .onErrorResume(this::handleError);
    }

    /**
     * Search movies by title.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> searchMoviesByTitle(ServerRequest request) {
        String title = request.queryParam("title")
                .orElse("");

        if (title.trim().isEmpty()) {
            return ServerResponse.badRequest()
                    .bodyValue("Title parameter is required");
        }

        return movieService.searchMoviesByTitle(title)
                .map(dtoMapper::toResponse)
                .collectList()
                .flatMap(movies -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(movies))
                .doOnSuccess(response -> log.debug("Successfully searched movies by title: {}", title))
                .onErrorResume(this::handleError);
    }

    /**
     * Search movies by year of release.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> searchMoviesByYear(ServerRequest request) {
        String yearStr = request.pathVariable("year");
        
        try {
            Integer year = Integer.parseInt(yearStr);
            
            return movieService.getMoviesByYear(year)
                    .map(dtoMapper::toResponse)
                    .collectList()
                    .flatMap(movies -> ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(movies))
                    .doOnSuccess(response -> log.debug("Successfully searched movies by year: {}", year))
                    .onErrorResume(this::handleError);
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest()
                    .bodyValue("Invalid year format");
        }
    }

    /**
     * Search movies by year range.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> searchMoviesByYearRange(ServerRequest request) {
        String startYearStr = request.queryParam("startYear").orElse("");
        String endYearStr = request.queryParam("endYear").orElse("");

        if (startYearStr.isEmpty() || endYearStr.isEmpty()) {
            return ServerResponse.badRequest()
                    .bodyValue("Both startYear and endYear parameters are required");
        }

        try {
            Integer startYear = Integer.parseInt(startYearStr);
            Integer endYear = Integer.parseInt(endYearStr);

            if (startYear > endYear) {
                return ServerResponse.badRequest()
                        .bodyValue("startYear cannot be greater than endYear");
            }

            return movieService.getMoviesByYearRange(startYear, endYear)
                    .map(dtoMapper::toResponse)
                    .collectList()
                    .flatMap(movies -> ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(movies))
                    .doOnSuccess(response -> log.debug("Successfully searched movies by year range: {}-{}", startYear, endYear))
                    .onErrorResume(this::handleError);
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest()
                    .bodyValue("Invalid year format");
        }
    }

    /**
     * Search movies by plot keywords.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> searchMoviesByPlot(ServerRequest request) {
        String plot = request.queryParam("plot")
                .orElse("");

        if (plot.trim().isEmpty()) {
            return ServerResponse.badRequest()
                    .bodyValue("Plot parameter is required");
        }

        return movieService.searchMoviesByPlot(plot)
                .map(dtoMapper::toResponse)
                .collectList()
                .flatMap(movies -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(movies))
                .doOnSuccess(response -> log.debug("Successfully searched movies by plot: {}", plot))
                .onErrorResume(this::handleError);
    }

    /**
     * Get movie count statistics.
     * Public endpoint - no authentication required.
     */
    public Mono<ServerResponse> getMovieCount(ServerRequest request) {
        return movieService.getActiveMovieCount()
                .flatMap(count -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(java.util.Map.of("count", count)))
                .doOnSuccess(response -> log.debug("Successfully retrieved movie count"))
                .onErrorResume(this::handleError);
    }

    /**
     * Validate create movie request.
     */
    private void validateCreateRequest(CreateMovieRequest request) {
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.yearOfRelease() == null) {
            throw new IllegalArgumentException("Year of release is required");
        }
    }

    /**
     * Validate update movie request.
     */
    private void validateUpdateRequest(UpdateMovieRequest request) {
        if (request.title() != null && request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
    }

    /**
     * Handle errors and convert them to appropriate HTTP responses.
     */
    private Mono<ServerResponse> handleError(Throwable error) {
        log.error("Error in movie handler", error);

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
