package com.movie.rating.system.infrastructure.inbound.web.router;

import com.movie.rating.system.infrastructure.inbound.web.handler.MovieHandler;
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
 * Router configuration for movie-related endpoints.
 */
@Configuration
@Tag(name = "Movies", description = "Movie management operations")
public class MovieRouter {

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/api/v1/movies", 
            method = RequestMethod.POST,
            operation = @Operation(
                operationId = "createMovie",
                summary = "Create a new movie",
                description = "Creates a new movie in the system. Requires authentication.",
                tags = {"Movies - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                requestBody = @RequestBody(
                    description = "Movie creation data",
                    required = true,
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.request.CreateMovieRequest.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "201", 
                        description = "Movie created successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieResponse.class)
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/my", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getMyMovies",
                summary = "Get my movies",
                description = "Retrieves all movies created by the currently authenticated user.",
                tags = {"Movies - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Movies retrieved successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieResponse.class)
                        )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/{id}", 
            method = RequestMethod.PUT,
            operation = @Operation(
                operationId = "updateMovie",
                summary = "Update movie",
                description = "Updates an existing movie. Only the movie creator can update it.",
                tags = {"Movies - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "id", in = ParameterIn.PATH, description = "Movie ID", required = true),
                requestBody = @RequestBody(
                    description = "Movie update data",
                    required = true,
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateMovieRequest.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Movie updated successfully",
                        content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieResponse.class)
                        )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not the movie creator"),
                    @ApiResponse(responseCode = "404", description = "Movie not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/{id}", 
            method = RequestMethod.DELETE,
            operation = @Operation(
                operationId = "deleteMovie",
                summary = "Delete movie",
                description = "Soft deletes a movie. Only the movie creator can delete it.",
                tags = {"Movies - Secured"},
                security = @SecurityRequirement(name = "bearerAuth"),
                parameters = @Parameter(name = "id", in = ParameterIn.PATH, description = "Movie ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "204", description = "Movie deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not the movie creator"),
                    @ApiResponse(responseCode = "404", description = "Movie not found")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/search", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "searchMoviesByTitle",
                summary = "Search movies by title",
                description = "Search for movies by title (case-insensitive partial match). Public endpoint.",
                tags = {"Movies - Public"},
                parameters = @Parameter(name = "title", in = ParameterIn.QUERY, description = "Title to search for", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Movies found"),
                    @ApiResponse(responseCode = "400", description = "Invalid search parameters")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/year/{year}", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "searchMoviesByYear",
                summary = "Search movies by release year",
                description = "Find all movies released in a specific year. Public endpoint.",
                tags = {"Movies - Public"},
                parameters = @Parameter(name = "year", in = ParameterIn.PATH, description = "Release year", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Movies found"),
                    @ApiResponse(responseCode = "400", description = "Invalid year")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/year-range", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "searchMoviesByYearRange",
                summary = "Search movies by year range",
                description = "Find all movies released within a specific year range. Public endpoint.",
                tags = {"Movies - Public"},
                parameters = {
                    @Parameter(name = "startYear", in = ParameterIn.QUERY, description = "Start year (inclusive)", required = true),
                    @Parameter(name = "endYear", in = ParameterIn.QUERY, description = "End year (inclusive)", required = true)
                },
                responses = {
                    @ApiResponse(responseCode = "200", description = "Movies found"),
                    @ApiResponse(responseCode = "400", description = "Invalid year range")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/search-plot", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "searchMoviesByPlot",
                summary = "Search movies by plot",
                description = "Search for movies by plot description (case-insensitive partial match). Public endpoint.",
                tags = {"Movies - Public"},
                parameters = @Parameter(name = "plot", in = ParameterIn.QUERY, description = "Plot text to search for", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Movies found"),
                    @ApiResponse(responseCode = "400", description = "Invalid search parameters")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/count", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getMovieCount",
                summary = "Get total movie count",
                description = "Returns the total number of active movies in the system. Public endpoint.",
                tags = {"Movies - Public"},
                responses = {
                    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getAllMovies",
                summary = "Get all movies",
                description = "Retrieves all active movies in the system. Public endpoint with pagination support.",
                tags = {"Movies - Public"},
                responses = {
                    @ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/movies/{id}", 
            method = RequestMethod.GET,
            operation = @Operation(
                operationId = "getMovieById",
                summary = "Get movie by ID",
                description = "Retrieves a specific movie by its unique identifier. Public endpoint.",
                tags = {"Movies - Public"},
                parameters = @Parameter(name = "id", in = ParameterIn.PATH, description = "Movie ID", required = true),
                responses = {
                    @ApiResponse(responseCode = "200", description = "Movie found"),
                    @ApiResponse(responseCode = "404", description = "Movie not found")
                }
            )
        )
    })
    public RouterFunction<ServerResponse> movieRoutes(MovieHandler movieHandler) {
        return RouterFunctions
                .route(POST("/api/v1/movies").and(accept(MediaType.APPLICATION_JSON)), 
                       movieHandler::createMovie)
                // Specific routes must come before generic {id} route
                .andRoute(GET("/api/v1/movies/my"), 
                         movieHandler::getMyMovies)
                .andRoute(GET("/api/v1/movies/search").and(queryParam("title", t -> true)), 
                         movieHandler::searchMoviesByTitle)
                .andRoute(GET("/api/v1/movies/year/{year}"), 
                         movieHandler::searchMoviesByYear)
                .andRoute(GET("/api/v1/movies/year-range")
                         .and(queryParam("startYear", s -> true))
                         .and(queryParam("endYear", e -> true)), 
                         movieHandler::searchMoviesByYearRange)
                .andRoute(GET("/api/v1/movies/search-plot").and(queryParam("plot", p -> true)), 
                         movieHandler::searchMoviesByPlot)
                .andRoute(GET("/api/v1/movies/count"), 
                         movieHandler::getMovieCount)
                .andRoute(GET("/api/v1/movies"), 
                         movieHandler::getAllMovies)
                // Generic {id} route must come after all specific routes
                .andRoute(GET("/api/v1/movies/{id}"), 
                         movieHandler::getMovieById)
                .andRoute(PUT("/api/v1/movies/{id}").and(accept(MediaType.APPLICATION_JSON)), 
                         movieHandler::updateMovie)
                .andRoute(DELETE("/api/v1/movies/{id}"), 
                         movieHandler::deleteMovie);
    }
}
