package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.domain.exception.*;
import com.movie.rating.system.domain.port.inbound.ManageMovieRatingUseCase;
import com.movie.rating.system.domain.port.outbound.MovieRatingRepository;
import com.movie.rating.system.domain.port.outbound.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementation of ManageMovieRatingUseCase for movie rating management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManageMovieRatingService implements ManageMovieRatingUseCase {

    private final MovieRatingRepository movieRatingRepository;
    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public Mono<MovieRating> createRating(CreateRatingCommand command) {
        log.info("Creating rating for movie {} by user {}", command.movieId(), command.userId());
        
        // Check if movie exists
        return movieRepository.existsById(command.movieId())
                .flatMap(movieExists -> {
                    if (!movieExists) {
                        return Mono.error(new MovieNotFoundException(command.movieId()));
                    }
                    
                    // Check if user already rated this movie
                    return movieRatingRepository.existsActiveByMovieIdAndUserId(command.movieId(), command.userId());
                })
                .flatMap(ratingExists -> {
                    if (ratingExists) {
                        return Mono.error(new DuplicateRatingException(command.movieId(), command.userId()));
                    }
                    
                    MovieRating rating = MovieRating.builder()
                            .movieId(command.movieId())
                            .userId(command.userId())
                            .rating(command.rating())
                            .review(command.review())
                            .build();
                    
                    return movieRatingRepository.save(rating);
                })
                .doOnSuccess(savedRating -> log.info("Successfully created rating with ID: {}", savedRating.getId()))
                .doOnError(error -> log.error("Failed to create rating for movie {} by user {}", 
                    command.movieId(), command.userId(), error));
    }

    @Override
    public Mono<MovieRating> getRatingById(UUID ratingId) {
        log.debug("Retrieving rating by ID: {}", ratingId);
        
        return movieRatingRepository.findById(ratingId)
                .switchIfEmpty(Mono.error(new MovieRatingNotFoundException(ratingId)))
                .doOnSuccess(rating -> log.debug("Successfully retrieved rating: {}", rating.getId()))
                .doOnError(error -> log.error("Failed to retrieve rating by ID: {}", ratingId, error));
    }

    @Override
    @Transactional
    public Mono<MovieRating> updateRating(UpdateRatingCommand command) {
        log.info("Updating rating {} by user {}", command.ratingId(), command.userId());
        
        return movieRatingRepository.findById(command.ratingId())
                .switchIfEmpty(Mono.error(new MovieRatingNotFoundException(command.ratingId())))
                .flatMap(existingRating -> {
                    // Check authorization - only the rating creator can update
                    if (!existingRating.getUserId().equals(command.userId())) {
                        return Mono.error(new UnauthorizedMovieOperationException(
                            existingRating.getMovieId(), command.userId(), "update rating"));
                    }
                    
                    MovieRating.MovieRatingBuilder builder = existingRating.toBuilder()
                            .updatedAt(Instant.now());
                    
                    if (command.rating() != null) {
                        builder.rating(command.rating());
                    }
                    if (command.review() != null) {
                        builder.review(command.review());
                    }
                    
                    return movieRatingRepository.save(builder.build());
                })
                .doOnSuccess(updatedRating -> log.info("Successfully updated rating: {}", updatedRating.getId()))
                .doOnError(error -> log.error("Failed to update rating {} by user {}", 
                    command.ratingId(), command.userId(), error));
    }

    @Override
    @Transactional
    public Mono<Void> deleteRating(UUID ratingId, UUID userId) {
        log.info("Deleting rating {} by user {}", ratingId, userId);
        
        return movieRatingRepository.findById(ratingId)
                .switchIfEmpty(Mono.error(new MovieRatingNotFoundException(ratingId)))
                .flatMap(rating -> {
                    // Check authorization - only the rating creator can delete
                    if (!rating.getUserId().equals(userId)) {
                        return Mono.error(new UnauthorizedMovieOperationException(
                            rating.getMovieId(), userId, "delete rating"));
                    }
                    return movieRatingRepository.deleteById(ratingId);
                })
                .doOnSuccess(result -> log.info("Successfully deleted rating {}", ratingId))
                .doOnError(error -> log.error("Failed to delete rating {} by user {}", ratingId, userId, error));
    }

    @Override
    public Flux<MovieRating> getRatingsByMovie(UUID movieId) {
        log.debug("Retrieving ratings for movie: {}", movieId);
        
        return movieRatingRepository.findActiveByMovieId(movieId)
                .doOnComplete(() -> log.debug("Successfully retrieved ratings for movie: {}", movieId))
                .doOnError(error -> log.error("Failed to retrieve ratings for movie: {}", movieId, error));
    }

    @Override
    public Flux<MovieRating> getRatingsByUser(UUID userId) {
        log.debug("Retrieving ratings by user: {}", userId);
        
        return movieRatingRepository.findActiveByUserId(userId)
                .doOnComplete(() -> log.debug("Successfully retrieved ratings by user: {}", userId))
                .doOnError(error -> log.error("Failed to retrieve ratings by user: {}", userId, error));
    }

    @Override
    public Mono<MovieRating> getUserRatingForMovie(UUID movieId, UUID userId) {
        log.debug("Retrieving rating for movie {} by user {}", movieId, userId);
        
        return movieRatingRepository.findActiveByMovieIdAndUserId(movieId, userId)
                .doOnSuccess(rating -> log.debug("Found rating for movie {} by user {}: {}", 
                    movieId, userId, rating != null ? rating.getId() : "null"))
                .doOnError(error -> log.error("Failed to retrieve rating for movie {} by user {}", 
                    movieId, userId, error));
    }

    @Override
    public Flux<MovieRating> getRatingsByMovieAndRange(UUID movieId, Integer minRating, Integer maxRating) {
        log.debug("Retrieving ratings for movie {} between {} and {}", movieId, minRating, maxRating);
        
        return movieRatingRepository.findActiveByMovieIdAndRatingBetween(movieId, minRating, maxRating)
                .doOnComplete(() -> log.debug("Successfully retrieved ratings for movie {} in range", movieId))
                .doOnError(error -> log.error("Failed to retrieve ratings for movie {} in range", movieId, error));
    }

    @Override
    public Flux<MovieRating> getRatingsWithReviewsByMovie(UUID movieId) {
        log.debug("Retrieving ratings with reviews for movie: {}", movieId);
        
        return movieRatingRepository.findActiveByMovieIdWithReviews(movieId)
                .doOnComplete(() -> log.debug("Successfully retrieved ratings with reviews for movie: {}", movieId))
                .doOnError(error -> log.error("Failed to retrieve ratings with reviews for movie: {}", movieId, error));
    }

    @Override
    public Flux<MovieRating> searchRatings(SearchRatingsCommand command) {
        log.debug("Searching ratings with criteria: {}", command);
        
        // This is a simplified implementation - in a real application, you'd have more complex query logic
        if (command.movieId() != null) {
            return movieRatingRepository.findActiveByMovieId(command.movieId());
        } else if (command.userId() != null) {
            return movieRatingRepository.findActiveByUserId(command.userId());
        } else if (command.startDate() != null && command.endDate() != null) {
            return movieRatingRepository.findActiveByCreatedAtBetween(command.startDate(), command.endDate());
        } else {
            return movieRatingRepository.findRecentRatings(command.limit() != null ? command.limit() : 50);
        }
    }

    @Override
    public Mono<MovieRatingStatistics> getMovieRatingStatistics(UUID movieId) {
        log.debug("Getting rating statistics for movie: {}", movieId);
        
        return Mono.zip(
                movieRatingRepository.countActiveByMovieId(movieId),
                movieRatingRepository.calculateAverageRatingByMovieId(movieId).defaultIfEmpty(0.0),
                movieRatingRepository.findActiveByMovieId(movieId)
                        .collectList()
                        .map(ratings -> {
                            if (ratings.isEmpty()) {
                                return new MovieRatingStatistics(
                                    movieId, 0, 0.0, 0, 0, 0,
                                    new RatingDistribution(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                );
                            }
                            
                            int min = ratings.stream().mapToInt(MovieRating::getRating).min().orElse(0);
                            int max = ratings.stream().mapToInt(MovieRating::getRating).max().orElse(0);
                            long withReviews = ratings.stream()
                                    .filter(r -> r.getReview() != null && !r.getReview().trim().isEmpty())
                                    .count();
                            
                            // Create distribution
                            long[] distribution = new long[10];
                            ratings.forEach(r -> {
                                if (r.getRating() >= 1 && r.getRating() <= 10) {
                                    distribution[r.getRating() - 1]++;
                                }
                            });
                            
                            RatingDistribution dist = new RatingDistribution(
                                distribution[0], distribution[1], distribution[2], distribution[3], distribution[4],
                                distribution[5], distribution[6], distribution[7], distribution[8], distribution[9]
                            );
                            
                            return new MovieRatingStatistics(movieId, ratings.size(), 0.0, min, max, withReviews, dist);
                        })
        ).map(tuple -> {
            long totalRatings = tuple.getT1();
            double averageRating = tuple.getT2();
            MovieRatingStatistics baseStats = tuple.getT3();
            
            return new MovieRatingStatistics(
                movieId, totalRatings, averageRating,
                baseStats.minRating(), baseStats.maxRating(),
                baseStats.ratingsWithReviews(), baseStats.distribution()
            );
        })
        .doOnSuccess(stats -> log.debug("Successfully retrieved rating statistics for movie: {}", movieId))
        .doOnError(error -> log.error("Failed to get rating statistics for movie: {}", movieId, error));
    }

    @Override
    public Mono<UserRatingStatistics> getUserRatingStatistics(UUID userId) {
        log.debug("Getting rating statistics for user: {}", userId);
        
        return movieRatingRepository.findActiveByUserId(userId)
                .collectList()
                .map(ratings -> {
                    if (ratings.isEmpty()) {
                        return new UserRatingStatistics(
                            userId, 0, 0.0, 0, 0, 0, null, null
                        );
                    }
                    
                    double avgRating = ratings.stream().mapToInt(MovieRating::getRating).average().orElse(0.0);
                    int minRating = ratings.stream().mapToInt(MovieRating::getRating).min().orElse(0);
                    int maxRating = ratings.stream().mapToInt(MovieRating::getRating).max().orElse(0);
                    long withReviews = ratings.stream()
                            .filter(r -> r.getReview() != null && !r.getReview().trim().isEmpty())
                            .count();
                    
                    Instant firstRating = ratings.stream()
                            .map(MovieRating::getCreatedAt)
                            .min(Instant::compareTo)
                            .orElse(null);
                    
                    Instant lastRating = ratings.stream()
                            .map(MovieRating::getCreatedAt)
                            .max(Instant::compareTo)
                            .orElse(null);
                    
                    return new UserRatingStatistics(
                        userId, ratings.size(), avgRating, minRating, maxRating,
                        withReviews, firstRating, lastRating
                    );
                })
                .doOnSuccess(stats -> log.debug("Successfully retrieved rating statistics for user: {}", userId))
                .doOnError(error -> log.error("Failed to get rating statistics for user: {}", userId, error));
    }

    @Override
    public Flux<MovieRating> getRecentRatings(int limit) {
        log.debug("Retrieving {} recent ratings", limit);
        
        return movieRatingRepository.findRecentRatings(limit)
                .doOnComplete(() -> log.debug("Successfully retrieved {} recent ratings", limit))
                .doOnError(error -> log.error("Failed to retrieve recent ratings", error));
    }

    @Override
    public Flux<TopRatedMovie> getTopRatedMovies(int limit, int minRatingCount) {
        log.debug("Retrieving top {} rated movies with minimum {} ratings", limit, minRatingCount);
        
        return movieRatingRepository.findTopRatedMovies(limit, minRatingCount)
                .flatMap(movieId -> 
                    Mono.zip(
                        Mono.just(movieId),
                        movieRatingRepository.calculateAverageRatingByMovieId(movieId).defaultIfEmpty(0.0),
                        movieRatingRepository.countActiveByMovieId(movieId)
                    ).map(tuple -> new TopRatedMovie(tuple.getT1(), tuple.getT2(), tuple.getT3()))
                )
                .doOnComplete(() -> log.debug("Successfully retrieved top rated movies"))
                .doOnError(error -> log.error("Failed to retrieve top rated movies", error));
    }

    @Override
    public Mono<Boolean> hasUserRatedMovie(UUID movieId, UUID userId) {
        log.debug("Checking if user {} has rated movie {}", userId, movieId);
        
        return movieRatingRepository.existsActiveByMovieIdAndUserId(movieId, userId)
                .doOnSuccess(hasRated -> log.debug("User {} has rated movie {}: {}", userId, movieId, hasRated))
                .doOnError(error -> log.error("Failed to check if user {} has rated movie {}", userId, movieId, error));
    }

    @Override
    public Mono<Boolean> canUserModifyRating(UUID ratingId, UUID userId) {
        log.debug("Checking if user {} can modify rating {}", userId, ratingId);
        
        return movieRatingRepository.findById(ratingId)
                .map(rating -> rating.getUserId().equals(userId))
                .defaultIfEmpty(false)
                .doOnSuccess(canModify -> log.debug("User {} can modify rating {}: {}", userId, ratingId, canModify))
                .doOnError(error -> log.error("Failed to check if user {} can modify rating {}", userId, ratingId, error));
    }
    
    // Additional methods for web handler support
    
    /**
     * Get ratings by movie with pagination.
     */
    public Flux<MovieRating> getRatingsByMovie(UUID movieId, int offset, int limit) {
        log.debug("Getting ratings for movie {} with offset: {}, limit: {}", movieId, offset, limit);
        return movieRatingRepository.findActiveByMovieIdWithPagination(movieId, offset, limit);
    }
    
    /**
     * Get ratings by user with pagination.
     */
    public Flux<MovieRating> getRatingsByUser(UUID userId, int offset, int limit) {
        log.debug("Getting ratings for user {} with offset: {}, limit: {}", userId, offset, limit);
        return movieRatingRepository.findActiveByUserIdWithPagination(userId, offset, limit);
    }
    
    /**
     * Get average rating for a movie.
     */
    public Mono<Double> getAverageRating(UUID movieId) {
        log.debug("Getting average rating for movie {}", movieId);
        return movieRatingRepository.calculateAverageRatingByMovieId(movieId);
    }
}
