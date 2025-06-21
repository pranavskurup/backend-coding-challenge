package com.movie.rating.system.domain.port.inbound;

import com.movie.rating.system.domain.entity.Movie;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use case interface for managing movies.
 * Defines all business operations related to movie management.
 */
public interface ManageMovieUseCase {

    /**
     * Command for creating a new movie.
     */
    record CreateMovieCommand(
            String title,
            String plot,
            Integer yearOfRelease,
            UUID createdBy
    ) {}

    /**
     * Command for updating movie details.
     */
    record UpdateMovieCommand(
            UUID movieId,
            String title,
            String plot,
            Integer yearOfRelease,
            UUID updatedBy
    ) {}

    /**
     * Command for deactivating a movie.
     */
    record DeactivateMovieCommand(
            UUID movieId,
            UUID deactivatedBy
    ) {}

    /**
     * Command for searching movies.
     */
    record SearchMoviesCommand(
            String titlePattern,
            Integer yearOfRelease,
            UUID createdBy,
            Integer offset,
            Integer limit
    ) {}

    /**
     * Create a new movie.
     *
     * @param command the create movie command
     * @return Mono containing the created movie
     */
    Mono<Movie> createMovie(CreateMovieCommand command);

    /**
     * Get a movie by its ID.
     *
     * @param movieId the movie ID
     * @return Mono containing the movie if found
     */
    Mono<Movie> getMovieById(UUID movieId);

    /**
     * Update movie details.
     *
     * @param command the update movie command
     * @return Mono containing the updated movie
     */
    Mono<Movie> updateMovie(UpdateMovieCommand command);

    /**
     * Deactivate a movie (soft delete).
     *
     * @param command the deactivate movie command
     * @return Mono that completes when the movie is deactivated
     */
    Mono<Void> deactivateMovie(DeactivateMovieCommand command);

    /**
     * Reactivate a previously deactivated movie.
     *
     * @param movieId the movie ID
     * @return Mono containing the reactivated movie
     */
    Mono<Movie> reactivateMovie(UUID movieId);

    /**
     * Get all active movies.
     *
     * @return Flux of all active movies
     */
    Flux<Movie> getAllActiveMovies();

    /**
     * Get movies created by a specific user.
     *
     * @param userId the user ID
     * @return Flux of movies created by the user
     */
    Flux<Movie> getMoviesByCreator(UUID userId);

    /**
     * Search movies by title pattern.
     *
     * @param titlePattern the title pattern to search for
     * @return Flux of movies matching the title pattern
     */
    Flux<Movie> searchMoviesByTitle(String titlePattern);

    /**
     * Get movies by year of release.
     *
     * @param year the year of release
     * @return Flux of movies released in the specified year
     */
    Flux<Movie> getMoviesByYear(Integer year);

    /**
     * Get movies within a year range.
     *
     * @param startYear the start year (inclusive)
     * @param endYear the end year (inclusive)
     * @return Flux of movies released within the specified range
     */
    Flux<Movie> getMoviesByYearRange(Integer startYear, Integer endYear);

    /**
     * Search movies by plot keywords.
     *
     * @param keyword the keyword to search in plot
     * @return Flux of movies with plot containing the keyword
     */
    Flux<Movie> searchMoviesByPlot(String keyword);

    /**
     * Search movies using multiple criteria.
     *
     * @param command the search command with criteria
     * @return Flux of movies matching the criteria
     */
    Flux<Movie> searchMovies(SearchMoviesCommand command);

    /**
     * Check if a movie exists.
     *
     * @param movieId the movie ID
     * @return Mono containing true if movie exists, false otherwise
     */
    Mono<Boolean> movieExists(UUID movieId);

    /**
     * Check if a user can modify a movie (e.g., creator or admin).
     *
     * @param movieId the movie ID
     * @param userId the user ID
     * @return Mono containing true if user can modify, false otherwise
     */
    Mono<Boolean> canUserModifyMovie(UUID movieId, UUID userId);

    /**
     * Get movie statistics for a user.
     *
     * @param userId the user ID
     * @return Mono containing movie statistics
     */
    Mono<MovieStatistics> getUserMovieStatistics(UUID userId);

    /**
     * Statistics about movies.
     */
    record MovieStatistics(
            long totalMoviesCreated,
            long activeMoviesCreated,
            long deactivatedMoviesCreated
    ) {}
}
