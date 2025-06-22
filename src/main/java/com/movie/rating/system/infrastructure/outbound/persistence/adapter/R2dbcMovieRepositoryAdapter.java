package com.movie.rating.system.infrastructure.outbound.persistence.adapter;

import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.domain.port.outbound.MovieRepository;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieEntity;
import com.movie.rating.system.infrastructure.outbound.persistence.mapper.MoviePersistenceMapper;
import com.movie.rating.system.infrastructure.outbound.persistence.repository.R2dbcMovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC implementation of MovieRepository.
 * Adapts the domain repository interface to R2DBC operations.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class R2dbcMovieRepositoryAdapter implements MovieRepository {

    private final R2dbcMovieRepository r2dbcRepository;
    private final MoviePersistenceMapper mapper;

    @Override
    public Mono<Movie> save(Movie movie) {
        if (movie == null) {
            log.error("Cannot save null movie");
            return Mono.error(new IllegalArgumentException("Movie cannot be null"));
        }
        
        log.debug("Saving movie: {}", movie.getTitle());
        
        MovieEntity entity = mapper.toEntity(movie);
        return r2dbcRepository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(savedMovie -> log.debug("Successfully saved movie with ID: {}", savedMovie.getId()))
                .doOnError(error -> log.error("Failed to save movie: {}", movie.getTitle(), error));
    }

    @Override
    public Mono<Movie> findById(UUID id) {
        log.debug("Finding movie by ID: {}", id);
        
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain)
                .doOnSuccess(movie -> log.debug("Found movie: {}", movie != null ? movie.getTitle() : "null"))
                .doOnError(error -> log.error("Failed to find movie by ID: {}", id, error));
    }

    @Override
    public Flux<Movie> findAllActive() {
        log.debug("Finding all active movies");
        
        return r2dbcRepository.findAllActive()
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding all active movies"))
                .doOnError(error -> log.error("Failed to find all active movies", error));
    }

    @Override
    public Flux<Movie> findByCreatedBy(UUID userId) {
        log.debug("Finding movies created by user: {}", userId);
        
        return r2dbcRepository.findByCreatedByAndIsActiveTrue(userId)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding movies by creator: {}", userId))
                .doOnError(error -> log.error("Failed to find movies by creator: {}", userId, error));
    }

    @Override
    public Flux<Movie> findByTitleContainingIgnoreCase(String titlePattern) {
        log.debug("Finding movies by title pattern: {}", titlePattern);
        
        return r2dbcRepository.findByTitleContainingIgnoreCaseAndIsActiveTrue(titlePattern)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding movies by title pattern: {}", titlePattern))
                .doOnError(error -> log.error("Failed to find movies by title pattern: {}", titlePattern, error));
    }

    @Override
    public Flux<Movie> findByYearOfRelease(Integer year) {
        log.debug("Finding movies by year: {}", year);
        
        return r2dbcRepository.findByYearOfReleaseAndIsActiveTrue(year)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding movies by year: {}", year))
                .doOnError(error -> log.error("Failed to find movies by year: {}", year, error));
    }

    @Override
    public Flux<Movie> findByYearOfReleaseBetween(Integer startYear, Integer endYear) {
        log.debug("Finding movies between years {} and {}", startYear, endYear);
        
        return r2dbcRepository.findByYearOfReleaseBetweenAndIsActiveTrue(startYear, endYear)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding movies between years {} and {}", startYear, endYear))
                .doOnError(error -> log.error("Failed to find movies between years {} and {}", startYear, endYear, error));
    }

    @Override
    public Flux<Movie> findByPlotContainingIgnoreCase(String plotKeyword) {
        log.debug("Finding movies by plot keyword: {}", plotKeyword);
        
        return r2dbcRepository.findByPlotContainingIgnoreCaseAndIsActiveTrue(plotKeyword)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding movies by plot keyword: {}", plotKeyword))
                .doOnError(error -> log.error("Failed to find movies by plot keyword: {}", plotKeyword, error));
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        log.debug("Checking if movie exists by ID: {}", id);
        
        return r2dbcRepository.existsById(id)
                .doOnSuccess(exists -> log.debug("Movie exists check for ID {}: {}", id, exists))
                .doOnError(error -> log.error("Failed to check if movie exists by ID: {}", id, error));
    }

    @Override
    public Mono<Boolean> existsByTitleAndYearOfRelease(String title, Integer yearOfRelease) {
        log.debug("Checking if movie exists by title '{}' and year {}", title, yearOfRelease);
        
        return r2dbcRepository.existsByTitleAndYearOfReleaseAndIsActiveTrue(title, yearOfRelease)
                .doOnSuccess(exists -> log.debug("Movie exists check for title '{}' and year {}: {}", title, yearOfRelease, exists))
                .doOnError(error -> log.error("Failed to check if movie exists by title '{}' and year {}", title, yearOfRelease, error));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        log.debug("Soft deleting movie by ID: {}", id);
        
        return r2dbcRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Movie not found with ID: " + id)))
                .map(entity -> entity.deactivate(null)) // Ideally we'd want to pass the user who's deactivating
                .flatMap(r2dbcRepository::save)
                .then()
                .doOnSuccess(v -> log.debug("Successfully soft deleted movie with ID: {}", id))
                .doOnError(error -> log.error("Failed to soft delete movie by ID: {}", id, error));
    }

    @Override
    public Mono<Long> countActive() {
        log.debug("Counting active movies");
        
        return r2dbcRepository.countByIsActiveTrue()
                .doOnSuccess(count -> log.debug("Active movies count: {}", count))
                .doOnError(error -> log.error("Failed to count active movies", error));
    }

    @Override
    public Mono<Long> countByCreatedBy(UUID userId) {
        log.debug("Counting movies created by user: {}", userId);
        
        return r2dbcRepository.countByCreatedByAndIsActiveTrue(userId)
                .doOnSuccess(count -> log.debug("Movies created by user {} count: {}", userId, count))
                .doOnError(error -> log.error("Failed to count movies by creator: {}", userId, error));
    }

    @Override
    public Flux<Movie> findAllActiveWithPagination(int offset, int limit) {
        log.debug("Finding active movies with pagination: offset={}, limit={}", offset, limit);
        
        return r2dbcRepository.findAllActiveWithPagination(offset, limit)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding active movies with pagination"))
                .doOnError(error -> log.error("Failed to find active movies with pagination", error));
    }

    @Override
    public Flux<Movie> searchMovies(String titlePattern, Integer yearOfRelease, UUID createdBy) {
        log.debug("Searching movies with criteria: title={}, year={}, creator={}", titlePattern, yearOfRelease, createdBy);
        
        return r2dbcRepository.searchMovies(titlePattern, yearOfRelease, createdBy)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed searching movies with criteria"))
                .doOnError(error -> log.error("Failed to search movies with criteria", error));
    }
}
