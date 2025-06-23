package com.movie.rating.system.infrastructure.outbound.persistence.repository;

import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieRatingEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC repository interface for MovieRatingEntity.
 * Provides database operations for movie rating persistence.
 */
public interface R2dbcMovieRatingRepository extends R2dbcRepository<MovieRatingEntity, UUID> {

    /**
     * Find all active ratings for a specific movie.
     */
    @Query("SELECT * FROM movie_ratings WHERE movie_id = :movieId AND is_active = true ORDER BY created_at DESC")
    Flux<MovieRatingEntity> findActiveByMovieId(@Param("movieId") UUID movieId);

    /**
     * Find all active ratings by a specific user.
     */
    @Query("SELECT * FROM movie_ratings WHERE user_id = :userId AND is_active = true ORDER BY created_at DESC")
    Flux<MovieRatingEntity> findActiveByUserId(@Param("userId") UUID userId);

    /**
     * Find a specific user's rating for a specific movie.
     */
    @Query("SELECT * FROM movie_ratings WHERE movie_id = :movieId AND user_id = :userId AND is_active = true")
    Mono<MovieRatingEntity> findActiveByMovieIdAndUserId(@Param("movieId") UUID movieId, @Param("userId") UUID userId);

    /**
     * Find all ratings (active and inactive) for a specific movie.
     */
    @Query("SELECT * FROM movie_ratings WHERE movie_id = :movieId ORDER BY created_at DESC")
    Flux<MovieRatingEntity> findAllByMovieId(@Param("movieId") UUID movieId);

    /**
     * Find all ratings (active and inactive) by a specific user.
     */
    @Query("SELECT * FROM movie_ratings WHERE user_id = :userId ORDER BY created_at DESC")
    Flux<MovieRatingEntity> findAllByUserId(@Param("userId") UUID userId);

    /**
     * Find ratings within a specific rating range for a movie.
     */
    @Query("SELECT * FROM movie_ratings WHERE movie_id = :movieId AND rating BETWEEN :minRating AND :maxRating AND is_active = true ORDER BY rating DESC")
    Flux<MovieRatingEntity> findActiveByMovieIdAndRatingBetween(
            @Param("movieId") UUID movieId,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating
    );

    /**
     * Find ratings with reviews for a specific movie.
     */
    @Query("SELECT * FROM movie_ratings WHERE movie_id = :movieId AND review IS NOT NULL AND TRIM(review) != '' AND is_active = true ORDER BY created_at DESC")
    Flux<MovieRatingEntity> findActiveByMovieIdWithReviews(@Param("movieId") UUID movieId);

    /**
     * Find ratings created within a date range.
     */
    @Query("SELECT * FROM movie_ratings WHERE created_at BETWEEN :startDate AND :endDate AND is_active = true ORDER BY created_at DESC")
    Flux<MovieRatingEntity> findActiveByCreatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Check if a rating exists for a specific movie by a specific user.
     */
    @Query("SELECT COUNT(*) > 0 FROM movie_ratings WHERE movie_id = :movieId AND user_id = :userId AND is_active = true")
    Mono<Boolean> existsActiveByMovieIdAndUserId(@Param("movieId") UUID movieId, @Param("userId") UUID userId);

    /**
     * Calculate average rating for a specific movie.
     */
    @Query("SELECT AVG(CAST(rating AS FLOAT)) FROM movie_ratings WHERE movie_id = :movieId AND is_active = true")
    Mono<Double> calculateAverageRatingByMovieId(@Param("movieId") UUID movieId);

    /**
     * Count total number of active ratings for a specific movie.
     */
    @Query("SELECT COUNT(*) FROM movie_ratings WHERE movie_id = :movieId AND is_active = true")
    Mono<Long> countActiveByMovieId(@Param("movieId") UUID movieId);

    /**
     * Count total number of active ratings by a specific user.
     */
    @Query("SELECT COUNT(*) FROM movie_ratings WHERE user_id = :userId AND is_active = true")
    Mono<Long> countActiveByUserId(@Param("userId") UUID userId);

    /**
     * Find top-rated movies based on average rating.
     */
    @Query("""
            SELECT movie_id, AVG(CAST(rating AS FLOAT)) as avg_rating, COUNT(*) as rating_count
            FROM movie_ratings 
            WHERE is_active = true 
            GROUP BY movie_id 
            HAVING COUNT(*) >= :minRatingCount 
            ORDER BY avg_rating DESC 
            LIMIT :limit
            """)
    Flux<TopRatedMovieResult> findTopRatedMovies(@Param("limit") int limit, @Param("minRatingCount") int minRatingCount);

    /**
     * Find ratings for a movie with pagination support.
     */
    @Query("SELECT * FROM movie_ratings WHERE movie_id = :movieId AND is_active = true ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<MovieRatingEntity> findActiveByMovieIdWithPagination(
            @Param("movieId") UUID movieId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * Find ratings by a user with pagination support.
     */
    @Query("SELECT * FROM movie_ratings WHERE user_id = :userId AND is_active = true ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<MovieRatingEntity> findActiveByUserIdWithPagination(
            @Param("userId") UUID userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * Find recent ratings across all users.
     */
    @Query("SELECT * FROM movie_ratings WHERE is_active = true ORDER BY created_at DESC LIMIT :limit")
    Flux<MovieRatingEntity> findRecentRatings(@Param("limit") int limit);

    /**
     * Find ratings by rating value.
     */
    @Query("SELECT * FROM movie_ratings WHERE rating = :rating AND is_active = true ORDER BY created_at DESC")
    Flux<MovieRatingEntity> findActiveByRating(@Param("rating") Integer rating);

    /**
     * Get rating statistics for a movie.
     */
    @Query("""
            SELECT 
                COUNT(*) as total_ratings,
                AVG(CAST(rating AS FLOAT)) as average_rating,
                MIN(rating) as min_rating,
                MAX(rating) as max_rating,
                COUNT(CASE WHEN review IS NOT NULL AND TRIM(review) != '' THEN 1 END) as ratings_with_reviews
            FROM movie_ratings 
            WHERE movie_id = :movieId AND is_active = true
            """)
    Mono<MovieRatingStats> getMovieRatingStatistics(@Param("movieId") UUID movieId);

    /**
     * Get user rating statistics.
     */
    @Query("""
            SELECT 
                COUNT(*) as total_ratings,
                AVG(CAST(rating AS FLOAT)) as average_rating_given,
                MIN(rating) as min_rating_given,
                MAX(rating) as max_rating_given,
                COUNT(CASE WHEN review IS NOT NULL AND TRIM(review) != '' THEN 1 END) as ratings_with_reviews,
                MIN(created_at) as first_rating_date,
                MAX(created_at) as last_rating_date
            FROM movie_ratings 
            WHERE user_id = :userId AND is_active = true
            """)
    Mono<UserRatingStats> getUserRatingStatistics(@Param("userId") UUID userId);

    /**
     * Get rating distribution for a movie.
     */
    @Query("""
            SELECT 
                rating,
                COUNT(*) as count
            FROM movie_ratings 
            WHERE movie_id = :movieId AND is_active = true 
            GROUP BY rating 
            ORDER BY rating
            """)
    Flux<RatingDistributionResult> getRatingDistribution(@Param("movieId") UUID movieId);

    /**
     * Count total active ratings.
     */
    @Query("SELECT COUNT(*) FROM movie_ratings WHERE is_active = true")
    Mono<Long> countAllActive();

    /**
     * Get overall average rating across all movies.
     */
    @Query("SELECT AVG(CAST(rating AS FLOAT)) FROM movie_ratings WHERE is_active = true")
    Mono<Double> getOverallAverageRating();

    /**
     * Count movies that have at least one rating.
     */
    @Query("SELECT COUNT(DISTINCT movie_id) FROM movie_ratings WHERE is_active = true")
    Mono<Long> countMoviesWithRatings();

    /**
     * Record for top-rated movie query result.
     */
    record TopRatedMovieResult(UUID movieId, Double avgRating, Long ratingCount) {}

    /**
     * Record for movie rating statistics query result.
     */
    record MovieRatingStats(
            Long totalRatings,
            Double averageRating,
            Integer minRating,
            Integer maxRating,
            Long ratingsWithReviews
    ) {}

    /**
     * Record for user rating statistics query result.
     */
    record UserRatingStats(
            Long totalRatings,
            Double averageRatingGiven,
            Integer minRatingGiven,
            Integer maxRatingGiven,
            Long ratingsWithReviews,
            Instant firstRatingDate,
            Instant lastRatingDate
    ) {}

    /**
     * Record for rating distribution query result.
     */
    record RatingDistributionResult(Integer rating, Long count) {}
}
