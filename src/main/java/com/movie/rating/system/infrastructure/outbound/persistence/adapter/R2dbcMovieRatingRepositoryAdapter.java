package com.movie.rating.system.infrastructure.outbound.persistence.adapter;

import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.domain.port.outbound.MovieRatingRepository;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieRatingEntity;
import com.movie.rating.system.infrastructure.outbound.persistence.mapper.MovieRatingPersistenceMapper;
import com.movie.rating.system.infrastructure.outbound.persistence.repository.R2dbcMovieRatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC implementation of MovieRatingRepository.
 * Adapts the domain repository interface to R2DBC operations.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class R2dbcMovieRatingRepositoryAdapter implements MovieRatingRepository {

    private final R2dbcMovieRatingRepository r2dbcRepository;
    private final MovieRatingPersistenceMapper mapper;

    @Override
    public Mono<MovieRating> save(MovieRating movieRating) {
        log.debug("Saving movie rating for movie: {} by user: {}", movieRating.getMovieId(), movieRating.getUserId());
        
        MovieRatingEntity entity = mapper.toEntity(movieRating);
        return r2dbcRepository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(savedRating -> log.debug("Successfully saved movie rating with ID: {}", savedRating.getId()))
                .doOnError(error -> log.error("Failed to save movie rating for movie: {} by user: {}", 
                    movieRating.getMovieId(), movieRating.getUserId(), error));
    }

    @Override
    public Mono<MovieRating> findById(UUID id) {
        log.debug("Finding movie rating by ID: {}", id);
        
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain)
                .doOnSuccess(rating -> log.debug("Found movie rating: {}", rating != null ? rating.getId() : "null"))
                .doOnError(error -> log.error("Failed to find movie rating by ID: {}", id, error));
    }

    @Override
    public Flux<MovieRating> findActiveByMovieId(UUID movieId) {
        log.debug("Finding active ratings for movie: {}", movieId);
        
        return r2dbcRepository.findActiveByMovieId(movieId)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding active ratings for movie: {}", movieId))
                .doOnError(error -> log.error("Failed to find active ratings for movie: {}", movieId, error));
    }

    @Override
    public Flux<MovieRating> findActiveByUserId(UUID userId) {
        log.debug("Finding active ratings by user: {}", userId);
        
        return r2dbcRepository.findActiveByUserId(userId)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding active ratings by user: {}", userId))
                .doOnError(error -> log.error("Failed to find active ratings by user: {}", userId, error));
    }

    @Override
    public Mono<MovieRating> findActiveByMovieIdAndUserId(UUID movieId, UUID userId) {
        log.debug("Finding active rating for movie: {} by user: {}", movieId, userId);
        
        return r2dbcRepository.findActiveByMovieIdAndUserId(movieId, userId)
                .map(mapper::toDomain)
                .doOnSuccess(rating -> log.debug("Found active rating for movie: {} by user: {}: {}", 
                    movieId, userId, rating != null ? rating.getId() : "null"))
                .doOnError(error -> log.error("Failed to find active rating for movie: {} by user: {}", 
                    movieId, userId, error));
    }

    @Override
    public Flux<MovieRating> findAllByMovieId(UUID movieId) {
        log.debug("Finding all ratings for movie: {}", movieId);
        
        return r2dbcRepository.findAllByMovieId(movieId)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding all ratings for movie: {}", movieId))
                .doOnError(error -> log.error("Failed to find all ratings for movie: {}", movieId, error));
    }

    @Override
    public Flux<MovieRating> findAllByUserId(UUID userId) {
        log.debug("Finding all ratings by user: {}", userId);
        
        return r2dbcRepository.findAllByUserId(userId)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding all ratings by user: {}", userId))
                .doOnError(error -> log.error("Failed to find all ratings by user: {}", userId, error));
    }

    @Override
    public Flux<MovieRating> findActiveByMovieIdAndRatingBetween(UUID movieId, Integer minRating, Integer maxRating) {
        log.debug("Finding ratings for movie: {} between {} and {}", movieId, minRating, maxRating);
        
        return r2dbcRepository.findActiveByMovieIdAndRatingBetween(movieId, minRating, maxRating)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding ratings for movie: {} between {} and {}", 
                    movieId, minRating, maxRating))
                .doOnError(error -> log.error("Failed to find ratings for movie: {} between {} and {}", 
                    movieId, minRating, maxRating, error));
    }

    @Override
    public Flux<MovieRating> findActiveByMovieIdWithReviews(UUID movieId) {
        log.debug("Finding ratings with reviews for movie: {}", movieId);
        
        return r2dbcRepository.findActiveByMovieIdWithReviews(movieId)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding ratings with reviews for movie: {}", movieId))
                .doOnError(error -> log.error("Failed to find ratings with reviews for movie: {}", movieId, error));
    }

    @Override
    public Flux<MovieRating> findActiveByCreatedAtBetween(Instant startDate, Instant endDate) {
        log.debug("Finding ratings created between {} and {}", startDate, endDate);
        
        return r2dbcRepository.findActiveByCreatedAtBetween(startDate, endDate)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding ratings created between {} and {}", startDate, endDate))
                .doOnError(error -> log.error("Failed to find ratings created between {} and {}", startDate, endDate, error));
    }

    @Override
    public Mono<Boolean> existsActiveByMovieIdAndUserId(UUID movieId, UUID userId) {
        log.debug("Checking if active rating exists for movie: {} by user: {}", movieId, userId);
        
        return r2dbcRepository.existsActiveByMovieIdAndUserId(movieId, userId)
                .doOnSuccess(exists -> log.debug("Active rating exists check for movie: {} by user: {}: {}", 
                    movieId, userId, exists))
                .doOnError(error -> log.error("Failed to check if active rating exists for movie: {} by user: {}", 
                    movieId, userId, error));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        log.debug("Soft deleting movie rating by ID: {}", id);
        
        return r2dbcRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Movie rating not found with ID: " + id)))
                .map(entity -> new MovieRatingEntity(
                    entity.id(),
                    entity.movieId(),
                    entity.userId(),
                    entity.rating(),
                    entity.review(),
                    false, // Set as inactive for soft delete
                    entity.createdAt(),
                    Instant.now() // Update the updatedAt timestamp
                ))
                .flatMap(r2dbcRepository::save)
                .then()
                .doOnSuccess(v -> log.debug("Successfully soft deleted movie rating with ID: {}", id))
                .doOnError(error -> log.error("Failed to soft delete movie rating by ID: {}", id, error));
    }

    @Override
    public Mono<Double> calculateAverageRatingByMovieId(UUID movieId) {
        log.debug("Calculating average rating for movie: {}", movieId);
        
        return r2dbcRepository.calculateAverageRatingByMovieId(movieId)
                .doOnSuccess(average -> log.debug("Average rating for movie: {}: {}", movieId, average))
                .doOnError(error -> log.error("Failed to calculate average rating for movie: {}", movieId, error));
    }

    @Override
    public Mono<Long> countActiveByMovieId(UUID movieId) {
        log.debug("Counting active ratings for movie: {}", movieId);
        
        return r2dbcRepository.countActiveByMovieId(movieId)
                .doOnSuccess(count -> log.debug("Active ratings count for movie: {}: {}", movieId, count))
                .doOnError(error -> log.error("Failed to count active ratings for movie: {}", movieId, error));
    }

    @Override
    public Mono<Long> countActiveByUserId(UUID userId) {
        log.debug("Counting active ratings by user: {}", userId);
        
        return r2dbcRepository.countActiveByUserId(userId)
                .doOnSuccess(count -> log.debug("Active ratings count by user: {}: {}", userId, count))
                .doOnError(error -> log.error("Failed to count active ratings by user: {}", userId, error));
    }

    @Override
    public Flux<UUID> findTopRatedMovies(int limit, int minRatingCount) {
        log.debug("Finding top {} rated movies with minimum {} ratings", limit, minRatingCount);
        
        return r2dbcRepository.findTopRatedMovies(limit, minRatingCount)
                .map(R2dbcMovieRatingRepository.TopRatedMovieResult::movieId)
                .doOnComplete(() -> log.debug("Completed finding top {} rated movies", limit))
                .doOnError(error -> log.error("Failed to find top rated movies", error));
    }

    @Override
    public Flux<MovieRating> findActiveByMovieIdWithPagination(UUID movieId, int offset, int limit) {
        log.debug("Finding ratings for movie: {} with pagination: offset={}, limit={}", movieId, offset, limit);
        
        return r2dbcRepository.findActiveByMovieIdWithPagination(movieId, offset, limit)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding ratings for movie: {} with pagination", movieId))
                .doOnError(error -> log.error("Failed to find ratings for movie: {} with pagination", movieId, error));
    }

    @Override
    public Flux<MovieRating> findActiveByUserIdWithPagination(UUID userId, int offset, int limit) {
        log.debug("Finding ratings by user: {} with pagination: offset={}, limit={}", userId, offset, limit);
        
        return r2dbcRepository.findActiveByUserIdWithPagination(userId, offset, limit)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding ratings by user: {} with pagination", userId))
                .doOnError(error -> log.error("Failed to find ratings by user: {} with pagination", userId, error));
    }

    @Override
    public Flux<MovieRating> findRecentRatings(int limit) {
        log.debug("Finding {} recent ratings", limit);
        
        return r2dbcRepository.findRecentRatings(limit)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding {} recent ratings", limit))
                .doOnError(error -> log.error("Failed to find recent ratings", error));
    }

    @Override
    public Flux<MovieRating> findActiveByRating(Integer rating) {
        log.debug("Finding ratings by rating value: {}", rating);
        
        return r2dbcRepository.findActiveByRating(rating)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding ratings by rating value: {}", rating))
                .doOnError(error -> log.error("Failed to find ratings by rating value: {}", rating, error));
    }
}
