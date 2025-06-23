package com.movie.rating.system.domain.port.outbound;

import com.movie.rating.system.domain.entity.MovieRating;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Repository interface for MovieRating entity operations.
 * Defines contract for persisting and retrieving movie rating data.
 */
public interface MovieRatingRepository {

    /**
     * Save a movie rating to the repository.
     *
     * @param movieRating the movie rating to save
     * @return Mono containing the saved movie rating
     */
    Mono<MovieRating> save(MovieRating movieRating);

    /**
     * Find a movie rating by its ID.
     *
     * @param id the rating ID
     * @return Mono containing the rating if found, empty if not found
     */
    Mono<MovieRating> findById(UUID id);

    /**
     * Find all active ratings for a specific movie.
     *
     * @param movieId the movie ID
     * @return Flux of active ratings for the movie
     */
    Flux<MovieRating> findActiveByMovieId(UUID movieId);

    /**
     * Find all active ratings by a specific user.
     *
     * @param userId the user ID
     * @return Flux of active ratings by the user
     */
    Flux<MovieRating> findActiveByUserId(UUID userId);

    /**
     * Find a specific user's rating for a specific movie.
     *
     * @param movieId the movie ID
     * @param userId the user ID
     * @return Mono containing the rating if found, empty if not found
     */
    Mono<MovieRating> findActiveByMovieIdAndUserId(UUID movieId, UUID userId);

    /**
     * Find all ratings (active and inactive) for a specific movie.
     *
     * @param movieId the movie ID
     * @return Flux of all ratings for the movie
     */
    Flux<MovieRating> findAllByMovieId(UUID movieId);

    /**
     * Find all ratings (active and inactive) by a specific user.
     *
     * @param userId the user ID
     * @return Flux of all ratings by the user
     */
    Flux<MovieRating> findAllByUserId(UUID userId);

    /**
     * Find ratings within a specific rating range for a movie.
     *
     * @param movieId the movie ID
     * @param minRating the minimum rating (inclusive)
     * @param maxRating the maximum rating (inclusive)
     * @return Flux of ratings within the specified range
     */
    Flux<MovieRating> findActiveByMovieIdAndRatingBetween(UUID movieId, Integer minRating, Integer maxRating);

    /**
     * Find ratings with reviews for a specific movie.
     *
     * @param movieId the movie ID
     * @return Flux of ratings that have non-empty reviews
     */
    Flux<MovieRating> findActiveByMovieIdWithReviews(UUID movieId);

    /**
     * Find ratings created within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return Flux of ratings created within the date range
     */
    Flux<MovieRating> findActiveByCreatedAtBetween(Instant startDate, Instant endDate);

    /**
     * Check if a rating exists for a specific movie by a specific user.
     *
     * @param movieId the movie ID
     * @param userId the user ID
     * @return Mono containing true if rating exists, false otherwise
     */
    Mono<Boolean> existsActiveByMovieIdAndUserId(UUID movieId, UUID userId);

    /**
     * Delete a rating by ID (soft delete - marks as inactive).
     *
     * @param id the rating ID
     * @return Mono that completes when the rating is deleted
     */
    Mono<Void> deleteById(UUID id);

    /**
     * Calculate average rating for a specific movie.
     *
     * @param movieId the movie ID
     * @return Mono containing the average rating, empty if no ratings exist
     */
    Mono<Double> calculateAverageRatingByMovieId(UUID movieId);

    /**
     * Count total number of active ratings for a specific movie.
     *
     * @param movieId the movie ID
     * @return Mono containing the count of active ratings
     */
    Mono<Long> countActiveByMovieId(UUID movieId);

    /**
     * Count total number of active ratings by a specific user.
     *
     * @param userId the user ID
     * @return Mono containing the count of active ratings by the user
     */
    Mono<Long> countActiveByUserId(UUID userId);

    /**
     * Find top-rated movies based on average rating.
     *
     * @param limit the maximum number of movies to return
     * @param minRatingCount the minimum number of ratings required for a movie to be included
     * @return Flux of movie IDs ordered by average rating (highest first)
     */
    Flux<UUID> findTopRatedMovies(int limit, int minRatingCount);

    /**
     * Find ratings for a movie with pagination support.
     *
     * @param movieId the movie ID
     * @param offset the number of records to skip
     * @param limit the maximum number of records to return
     * @return Flux of ratings with pagination
     */
    Flux<MovieRating> findActiveByMovieIdWithPagination(UUID movieId, int offset, int limit);

    /**
     * Find ratings by a user with pagination support.
     *
     * @param userId the user ID
     * @param offset the number of records to skip
     * @param limit the maximum number of records to return
     * @return Flux of ratings with pagination
     */
    Flux<MovieRating> findActiveByUserIdWithPagination(UUID userId, int offset, int limit);

    /**
     * Find recent ratings across all users.
     *
     * @param limit the maximum number of ratings to return
     * @return Flux of recent ratings ordered by creation date (newest first)
     */
    Flux<MovieRating> findRecentRatings(int limit);

    /**
     * Find ratings by rating value.
     *
     * @param rating the specific rating value
     * @return Flux of ratings with the specified rating value
     */
    Flux<MovieRating> findActiveByRating(Integer rating);
}
