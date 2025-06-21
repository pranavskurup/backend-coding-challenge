package com.movie.rating.system.domain.port.inbound;

import com.movie.rating.system.domain.entity.MovieRating;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case interface for managing movie ratings.
 * Defines all business operations related to movie rating management.
 */
public interface ManageMovieRatingUseCase {

    /**
     * Command for creating a new movie rating.
     */
    record CreateRatingCommand(
            UUID movieId,
            UUID userId,
            Integer rating,
            String review
    ) {}

    /**
     * Command for updating a movie rating.
     */
    record UpdateRatingCommand(
            UUID ratingId,
            UUID userId,
            Integer rating,
            String review
    ) {}

    /**
     * Command for searching ratings.
     */
    record SearchRatingsCommand(
            UUID movieId,
            UUID userId,
            Integer minRating,
            Integer maxRating,
            Boolean hasReview,
            Instant startDate,
            Instant endDate,
            Integer offset,
            Integer limit
    ) {}

    /**
     * Create a new movie rating.
     *
     * @param command the create rating command
     * @return Mono containing the created rating
     */
    Mono<MovieRating> createRating(CreateRatingCommand command);

    /**
     * Get a rating by its ID.
     *
     * @param ratingId the rating ID
     * @return Mono containing the rating if found
     */
    Mono<MovieRating> getRatingById(UUID ratingId);

    /**
     * Update an existing movie rating.
     *
     * @param command the update rating command
     * @return Mono containing the updated rating
     */
    Mono<MovieRating> updateRating(UpdateRatingCommand command);

    /**
     * Delete a movie rating (soft delete).
     *
     * @param ratingId the rating ID
     * @param userId the user ID (for authorization)
     * @return Mono that completes when the rating is deleted
     */
    Mono<Void> deleteRating(UUID ratingId, UUID userId);

    /**
     * Get all ratings for a specific movie.
     *
     * @param movieId the movie ID
     * @return Flux of ratings for the movie
     */
    Flux<MovieRating> getRatingsByMovie(UUID movieId);

    /**
     * Get all ratings by a specific user.
     *
     * @param userId the user ID
     * @return Flux of ratings by the user
     */
    Flux<MovieRating> getRatingsByUser(UUID userId);

    /**
     * Get a specific user's rating for a specific movie.
     *
     * @param movieId the movie ID
     * @param userId the user ID
     * @return Mono containing the rating if found
     */
    Mono<MovieRating> getUserRatingForMovie(UUID movieId, UUID userId);

    /**
     * Get ratings within a specific rating range for a movie.
     *
     * @param movieId the movie ID
     * @param minRating the minimum rating (inclusive)
     * @param maxRating the maximum rating (inclusive)
     * @return Flux of ratings within the specified range
     */
    Flux<MovieRating> getRatingsByMovieAndRange(UUID movieId, Integer minRating, Integer maxRating);

    /**
     * Get ratings with reviews for a specific movie.
     *
     * @param movieId the movie ID
     * @return Flux of ratings that have non-empty reviews
     */
    Flux<MovieRating> getRatingsWithReviewsByMovie(UUID movieId);

    /**
     * Search ratings using multiple criteria.
     *
     * @param command the search command with criteria
     * @return Flux of ratings matching the criteria
     */
    Flux<MovieRating> searchRatings(SearchRatingsCommand command);

    /**
     * Calculate movie rating statistics.
     *
     * @param movieId the movie ID
     * @return Mono containing rating statistics
     */
    Mono<MovieRatingStatistics> getMovieRatingStatistics(UUID movieId);

    /**
     * Get user rating statistics.
     *
     * @param userId the user ID
     * @return Mono containing user rating statistics
     */
    Mono<UserRatingStatistics> getUserRatingStatistics(UUID userId);

    /**
     * Get recent ratings across all users.
     *
     * @param limit the maximum number of ratings to return
     * @return Flux of recent ratings
     */
    Flux<MovieRating> getRecentRatings(int limit);

    /**
     * Get top-rated movies.
     *
     * @param limit the maximum number of movies to return
     * @param minRatingCount the minimum number of ratings required
     * @return Flux of top-rated movie IDs with their average ratings
     */
    Flux<TopRatedMovie> getTopRatedMovies(int limit, int minRatingCount);

    /**
     * Check if a user has rated a specific movie.
     *
     * @param movieId the movie ID
     * @param userId the user ID
     * @return Mono containing true if user has rated the movie, false otherwise
     */
    Mono<Boolean> hasUserRatedMovie(UUID movieId, UUID userId);

    /**
     * Check if a user can modify a rating (must be the rating creator).
     *
     * @param ratingId the rating ID
     * @param userId the user ID
     * @return Mono containing true if user can modify, false otherwise
     */
    Mono<Boolean> canUserModifyRating(UUID ratingId, UUID userId);

    /**
     * Statistics about ratings for a movie.
     */
    record MovieRatingStatistics(
            UUID movieId,
            long totalRatings,
            double averageRating,
            int minRating,
            int maxRating,
            long ratingsWithReviews,
            RatingDistribution distribution
    ) {}

    /**
     * Distribution of ratings (1-10).
     */
    record RatingDistribution(
            long rating1,
            long rating2,
            long rating3,
            long rating4,
            long rating5,
            long rating6,
            long rating7,
            long rating8,
            long rating9,
            long rating10
    ) {}

    /**
     * Statistics about a user's rating activity.
     */
    record UserRatingStatistics(
            UUID userId,
            long totalRatings,
            double averageRatingGiven,
            int minRatingGiven,
            int maxRatingGiven,
            long ratingsWithReviews,
            Instant firstRatingDate,
            Instant lastRatingDate
    ) {}

    /**
     * Top-rated movie information.
     */
    record TopRatedMovie(
            UUID movieId,
            double averageRating,
            long totalRatings
    ) {}
}
