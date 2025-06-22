package com.movie.rating.system.domain.port.outbound;

import com.movie.rating.system.domain.entity.Movie;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Movie entity operations.
 * Defines contract for persisting and retrieving movie data.
 */
public interface MovieRepository {

    /**
     * Save a movie to the repository.
     *
     * @param movie the movie to save
     * @return Mono containing the saved movie
     */
    Mono<Movie> save(Movie movie);

    /**
     * Find a movie by its ID.
     *
     * @param id the movie ID
     * @return Mono containing the movie if found, empty if not found
     */
    Mono<Movie> findById(UUID id);

    /**
     * Find all active movies.
     *
     * @return Flux of active movies
     */
    Flux<Movie> findAllActive();

    /**
     * Find all movies created by a specific user.
     *
     * @param userId the user ID
     * @return Flux of movies created by the user
     */
    Flux<Movie> findByCreatedBy(UUID userId);

    /**
     * Find movies by title pattern (case-insensitive).
     *
     * @param titlePattern the title pattern to search for
     * @return Flux of movies matching the title pattern
     */
    Flux<Movie> findByTitleContainingIgnoreCase(String titlePattern);

    /**
     * Find movies by year of release.
     *
     * @param year the year of release
     * @return Flux of movies released in the specified year
     */
    Flux<Movie> findByYearOfRelease(Integer year);

    /**
     * Find movies released within a year range.
     *
     * @param startYear the start year (inclusive)
     * @param endYear the end year (inclusive)
     * @return Flux of movies released within the specified range
     */
    Flux<Movie> findByYearOfReleaseBetween(Integer startYear, Integer endYear);

    /**
     * Find movies by plot content (case-insensitive search).
     *
     * @param plotKeyword the keyword to search in plot
     * @return Flux of movies with plot containing the keyword
     */
    Flux<Movie> findByPlotContainingIgnoreCase(String plotKeyword);

    /**
     * Check if a movie exists by ID.
     *
     * @param id the movie ID
     * @return Mono containing true if movie exists, false otherwise
     */
    Mono<Boolean> existsById(UUID id);

    /**
     * Check if a movie with the same title and year already exists.
     *
     * @param title the movie title
     * @param yearOfRelease the year of release
     * @return Mono containing true if movie exists, false otherwise
     */
    Mono<Boolean> existsByTitleAndYearOfRelease(String title, Integer yearOfRelease);

    /**
     * Delete a movie by ID (soft delete - marks as inactive).
     *
     * @param id the movie ID
     * @return Mono that completes when the movie is deleted
     */
    Mono<Void> deleteById(UUID id);

    /**
     * Count total number of active movies.
     *
     * @return Mono containing the count of active movies
     */
    Mono<Long> countActive();

    /**
     * Count movies created by a specific user.
     *
     * @param userId the user ID
     * @return Mono containing the count of movies created by the user
     */
    Mono<Long> countByCreatedBy(UUID userId);

    /**
     * Count active movies created by a specific user.
     *
     * @param userId the user ID
     * @return Mono containing the count of active movies created by the user
     */
    Mono<Long> countActiveByCreatedBy(UUID userId);

    /**
     * Find movies with pagination support.
     *
     * @param offset the number of records to skip
     * @param limit the maximum number of records to return
     * @return Flux of movies with pagination
     */
    Flux<Movie> findAllActiveWithPagination(int offset, int limit);

    /**
     * Search movies by multiple criteria.
     *
     * @param titlePattern optional title pattern
     * @param yearOfRelease optional year of release
     * @param createdBy optional creator user ID
     * @return Flux of movies matching the criteria
     */
    Flux<Movie> searchMovies(String titlePattern, Integer yearOfRelease, UUID createdBy);
}
