package com.movie.rating.system.domain.port.inbound;

import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.domain.entity.MovieRating;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Use case interface for comprehensive movie operations.
 * Combines movie and rating operations for complex business scenarios.
 */
public interface MovieCatalogUseCase {

    /**
     * Movie with its rating information.
     */
    record MovieWithRatings(
            Movie movie,
            double averageRating,
            long totalRatings,
            MovieRating userRating // null if user hasn't rated
    ) {}

    /**
     * Movie recommendation based on user preferences.
     */
    record MovieRecommendation(
            Movie movie,
            double recommendationScore,
            String reason
    ) {}

    /**
     * Popular movie information.
     */
    record PopularMovie(
            Movie movie,
            double averageRating,
            long totalRatings,
            long recentRatings // ratings in the last 30 days
    ) {}

    /**
     * Get movie with its rating information.
     *
     * @param movieId the movie ID
     * @param userId the user ID (to include user's rating)
     * @return Mono containing movie with ratings
     */
    Mono<MovieWithRatings> getMovieWithRatings(UUID movieId, UUID userId);

    /**
     * Get all movies with their rating information.
     *
     * @param userId the user ID (to include user's ratings)
     * @return Flux of movies with ratings
     */
    Flux<MovieWithRatings> getAllMoviesWithRatings(UUID userId);

    /**
     * Search movies with their rating information.
     *
     * @param titlePattern the title pattern to search for
     * @param userId the user ID (to include user's ratings)
     * @return Flux of movies with ratings matching the search
     */
    Flux<MovieWithRatings> searchMoviesWithRatings(String titlePattern, UUID userId);

    /**
     * Get popular movies based on ratings and recent activity.
     *
     * @param limit the maximum number of movies to return
     * @return Flux of popular movies
     */
    Flux<PopularMovie> getPopularMovies(int limit);

    /**
     * Get movie recommendations for a user based on their rating history.
     *
     * @param userId the user ID
     * @param limit the maximum number of recommendations
     * @return Flux of movie recommendations
     */
    Flux<MovieRecommendation> getMovieRecommendations(UUID userId, int limit);

    /**
     * Get movies by year with their rating information.
     *
     * @param year the year of release
     * @param userId the user ID (to include user's ratings)
     * @return Flux of movies from the specified year with ratings
     */
    Flux<MovieWithRatings> getMoviesByYearWithRatings(Integer year, UUID userId);

    /**
     * Get similar movies based on user's rating patterns.
     *
     * @param movieId the reference movie ID
     * @param userId the user ID
     * @param limit the maximum number of similar movies
     * @return Flux of similar movies with ratings
     */
    Flux<MovieWithRatings> getSimilarMovies(UUID movieId, UUID userId, int limit);

    /**
     * Get user's rated movies with full information.
     *
     * @param userId the user ID
     * @return Flux of movies rated by the user with full information
     */
    Flux<MovieWithRatings> getUserRatedMovies(UUID userId);

    /**
     * Get movies that a user hasn't rated yet.
     *
     * @param userId the user ID
     * @param limit the maximum number of movies to return
     * @return Flux of unrated movies for the user
     */
    Flux<Movie> getUnratedMoviesForUser(UUID userId, int limit);

    /**
     * Get movies with highest ratings (best rated).
     *
     * @param minRatingCount the minimum number of ratings required
     * @param limit the maximum number of movies to return
     * @return Flux of highest rated movies
     */
    Flux<MovieWithRatings> getHighestRatedMovies(int minRatingCount, int limit);

    /**
     * Get movies with most ratings (most popular by volume).
     *
     * @param limit the maximum number of movies to return
     * @return Flux of most rated movies
     */
    Flux<MovieWithRatings> getMostRatedMovies(int limit);

    /**
     * Get comprehensive movie statistics.
     *
     * @return Mono containing overall catalog statistics
     */
    Mono<MovieCatalogStatistics> getCatalogStatistics();

    /**
     * Overall catalog statistics.
     */
    record MovieCatalogStatistics(
            long totalMovies,
            long activeMovies,
            long totalRatings,
            double overallAverageRating,
            long moviesWithRatings,
            long moviesWithoutRatings,
            int oldestMovieYear,
            int newestMovieYear,
            long totalUsers,
            long activeUsers
    ) {}
}
