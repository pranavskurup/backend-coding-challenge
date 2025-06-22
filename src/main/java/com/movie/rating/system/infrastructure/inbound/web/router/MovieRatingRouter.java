package com.movie.rating.system.infrastructure.inbound.web.router;

import com.movie.rating.system.infrastructure.inbound.web.handler.MovieRatingHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Router configuration for movie rating-related endpoints.
 */
@Configuration
@Tag(name = "Movie Ratings", description = "Movie rating management operations")
public class MovieRatingRouter {

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/api/v1/ratings", 
            method = RequestMethod.POST,
            operation = @Operation(
                operationId = "createRating",
                summary = "Create a new movie rating",
                description = "Creates a new rating for a movie. Requires authentication.",
                tags = {"Ratings - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                requestBody = @RequestBody(
                    description = "Movie rating data",
                    required = true,
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.request.CreateMovieRatingRequest.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "201", 
                        description = "Rating created successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieRatingResponse.class)
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "409", description = "User has already rated this movie")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/ratings/my", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getMyRatings",
                summary = "Get ratings created by the authenticated user",
                description = "Retrieves all ratings created by the currently authenticated user.",
                tags = {"Ratings - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Ratings retrieved successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieRatingResponse.class)
                        )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/ratings/recent", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getRecentRatings",
                summary = "Get recent movie ratings",
                description = "Retrieves the most recently created movie ratings.",
                tags = {"Ratings - Public"},
                responses = {
                    @ApiResponse(responseCode = "200", description = "Recent ratings retrieved successfully")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/ratings/{id}", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getRatingById",
                summary = "Get rating by ID",
                description = "Retrieves a specific rating by its unique identifier.",
                tags = {"Ratings - Public"},
                parameters = @Parameter(name = "id", in = ParameterIn.PATH, description = "Rating ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Rating found"),
                    @ApiResponse(responseCode = "404", description = "Rating not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/ratings/{id}", 
            method = RequestMethod.PUT,
            operation = @Operation(
                operationId = "updateRating",
                summary = "Update movie rating",
                description = "Updates an existing rating. Only the rating creator can update it.",
                tags = {"Ratings - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "id", in = ParameterIn.PATH, description = "Rating ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Rating updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not the rating creator"),
                    @ApiResponse(responseCode = "404", description = "Rating not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/ratings/{id}", 
            method = RequestMethod.DELETE,
            operation = @Operation(
                operationId = "deleteRating",
                summary = "Delete movie rating",
                description = "Soft deletes a rating. Only the rating creator can delete it.",
                tags = {"Ratings - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "id", in = ParameterIn.PATH, description = "Rating ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "204", description = "Rating deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not the rating creator"),
                    @ApiResponse(responseCode = "404", description = "Rating not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/{movieId}/ratings", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getRatingsByMovie",
                summary = "Get all ratings for a movie",
                description = "Retrieves all ratings for a specific movie.",
                tags = {"Ratings - Public"},
                parameters = @Parameter(name = "movieId", in = ParameterIn.PATH, description = "Movie ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Ratings retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Movie not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/{movieId}/ratings/stats", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getMovieRatingStats",
                summary = "Get rating statistics for a movie",
                description = "Retrieves comprehensive rating statistics for a specific movie including counts by rating value.",
                tags = {"Movie Statistics - Public"},
                parameters = @Parameter(name = "movieId", in = ParameterIn.PATH, description = "Movie ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Rating statistics retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Movie not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/{movieId}/ratings/average", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getMovieAverageRating",
                summary = "Get average rating for a movie",
                description = "Retrieves the average rating for a specific movie.",
                tags = {"Movie Statistics - Public"},
                parameters = @Parameter(name = "movieId", in = ParameterIn.PATH, description = "Movie ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Average rating retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Movie not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/{movieId}/ratings/user", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getUserMovieRating",
                summary = "Get user's rating for a movie",
                description = "Retrieves the authenticated user's rating for a specific movie.",
                tags = {"Ratings - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "movieId", in = ParameterIn.PATH, description = "Movie ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "User rating retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Movie not found or user hasn't rated this movie")
                }
            )
        )
    })
    public RouterFunction<ServerResponse> movieRatingRoutes(MovieRatingHandler ratingHandler) {
        return RouterFunctions
                .route(POST("/api/v1/ratings").and(accept(MediaType.APPLICATION_JSON)), 
                       ratingHandler::createRating)
                // Specific routes must come before generic {id} route
                .andRoute(GET("/api/v1/ratings/my"), 
                         ratingHandler::getMyRatings)
                .andRoute(GET("/api/v1/ratings/recent"), 
                         ratingHandler::getRecentRatings)
                // Generic {id} route must come after specific routes
                .andRoute(GET("/api/v1/ratings/{id}"), 
                         ratingHandler::getRatingById)
                .andRoute(PUT("/api/v1/ratings/{id}").and(accept(MediaType.APPLICATION_JSON)), 
                         ratingHandler::updateRating)
                .andRoute(DELETE("/api/v1/ratings/{id}"), 
                         ratingHandler::deleteRating)
                // Movie-specific rating routes
                .andRoute(GET("/api/v1/movies/{movieId}/ratings"), 
                         ratingHandler::getRatingsByMovie)
                .andRoute(GET("/api/v1/movies/{movieId}/ratings/stats"), 
                         ratingHandler::getMovieRatingStats)
                .andRoute(GET("/api/v1/movies/{movieId}/ratings/average"), 
                         ratingHandler::getMovieAverageRating)
                .andRoute(GET("/api/v1/movies/{movieId}/ratings/user"), 
                         ratingHandler::getUserMovieRating);
    }
}
