package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.domain.exception.*;
import com.movie.rating.system.domain.port.inbound.ManageMovieUseCase;
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
 * Implementation of ManageMovieUseCase for movie management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManageMovieService implements ManageMovieUseCase {

    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public Mono<Movie> createMovie(CreateMovieCommand command) {
        log.info("Creating movie '{}' for user {}", command.title(), command.createdBy());
        
        return movieRepository.existsByTitleAndYearOfRelease(command.title(), command.yearOfRelease())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateMovieException(command.title(), command.yearOfRelease()));
                    }
                    
                    Movie movie = Movie.builder()
                            .title(command.title())
                            .plot(command.plot())
                            .yearOfRelease(command.yearOfRelease())
                            .createdBy(command.createdBy())
                            .build();
                    
                    return movieRepository.save(movie);
                })
                .doOnSuccess(savedMovie -> log.info("Successfully created movie '{}' with ID: {}", 
                    savedMovie.getTitle(), savedMovie.getId()))
                .doOnError(error -> log.error("Failed to create movie '{}' for user {}", 
                    command.title(), command.createdBy(), error));
    }

    @Override
    public Mono<Movie> getMovieById(UUID movieId) {
        log.debug("Retrieving movie by ID: {}", movieId);
        
        return movieRepository.findById(movieId)
                .switchIfEmpty(Mono.error(new MovieNotFoundException(movieId)))
                .doOnSuccess(movie -> log.debug("Successfully retrieved movie: {}", movie.getTitle()))
                .doOnError(error -> log.error("Failed to retrieve movie by ID: {}", movieId, error));
    }

    @Override
    @Transactional
    public Mono<Movie> updateMovie(UpdateMovieCommand command) {
        log.info("Updating movie {} by user {}", command.movieId(), command.updatedBy());
        
        return movieRepository.findById(command.movieId())
                .switchIfEmpty(Mono.error(new MovieNotFoundException(command.movieId())))
                .flatMap(existingMovie -> {
                    // Check authorization - only creator can update
                    if (!existingMovie.getCreatedBy().equals(command.updatedBy())) {
                        return Mono.error(new UnauthorizedMovieOperationException(
                            command.movieId(), command.updatedBy(), "update"));
                    }
                    
                    // Check for duplicate if title or year changed
                    String newTitle = command.title() != null ? command.title() : existingMovie.getTitle();
                    Integer newYear = command.yearOfRelease() != null ? command.yearOfRelease() : existingMovie.getYearOfRelease();
                    
                    if ((command.title() != null && !command.title().equals(existingMovie.getTitle())) ||
                        (command.yearOfRelease() != null && !command.yearOfRelease().equals(existingMovie.getYearOfRelease()))) {
                        
                        return movieRepository.existsByTitleAndYearOfRelease(newTitle, newYear)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new DuplicateMovieException(newTitle, newYear));
                                    }
                                    return updateMovieFields(existingMovie, command);
                                });
                    }
                    
                    return updateMovieFields(existingMovie, command);
                })
                .flatMap(movieRepository::save)
                .doOnSuccess(updatedMovie -> log.info("Successfully updated movie: {}", updatedMovie.getTitle()))
                .doOnError(error -> log.error("Failed to update movie {} by user {}", 
                    command.movieId(), command.updatedBy(), error));
    }

    @Override
    @Transactional
    public Mono<Void> deactivateMovie(DeactivateMovieCommand command) {
        log.info("Deactivating movie {} by user {}", command.movieId(), command.deactivatedBy());
        
        return movieRepository.findById(command.movieId())
                .switchIfEmpty(Mono.error(new MovieNotFoundException(command.movieId())))
                .flatMap(movie -> {
                    // Check authorization - only creator can deactivate
                    if (!movie.getCreatedBy().equals(command.deactivatedBy())) {
                        return Mono.error(new UnauthorizedMovieOperationException(
                            command.movieId(), command.deactivatedBy(), "deactivate"));
                    }
                    return movieRepository.deleteById(command.movieId());
                })
                .doOnSuccess(result -> log.info("Successfully deactivated movie {}", command.movieId()))
                .doOnError(error -> log.error("Failed to deactivate movie {} by user {}", 
                    command.movieId(), command.deactivatedBy(), error));
    }

    @Override
    @Transactional
    public Mono<Movie> reactivateMovie(UUID movieId) {
        log.info("Reactivating movie {}", movieId);
        
        return movieRepository.findById(movieId)
                .switchIfEmpty(Mono.error(new MovieNotFoundException(movieId)))
                .flatMap(movie -> {
                    if (movie.isActive()) {
                        return Mono.just(movie); // Already active
                    }
                    
                    Movie reactivatedMovie = movie.toBuilder()
                            .isActive(true)
                            .updatedAt(Instant.now())
                            .deactivatedAt(null)
                            .deactivatedBy(null)
                            .build();
                    
                    return movieRepository.save(reactivatedMovie);
                })
                .doOnSuccess(movie -> log.info("Successfully reactivated movie: {}", movie.getTitle()))
                .doOnError(error -> log.error("Failed to reactivate movie {}", movieId, error));
    }

    @Override
    public Flux<Movie> getAllActiveMovies() {
        log.debug("Retrieving all active movies");
        
        return movieRepository.findAllActive()
                .doOnComplete(() -> log.debug("Successfully retrieved all active movies"))
                .doOnError(error -> log.error("Failed to retrieve active movies", error));
    }

    @Override
    public Flux<Movie> getMoviesByCreator(UUID userId) {
        log.debug("Retrieving movies by creator: {}", userId);
        
        return movieRepository.findByCreatedBy(userId)
                .doOnComplete(() -> log.debug("Successfully retrieved movies by creator: {}", userId))
                .doOnError(error -> log.error("Failed to retrieve movies by creator: {}", userId, error));
    }

    @Override
    public Flux<Movie> searchMoviesByTitle(String titlePattern) {
        log.debug("Searching movies by title pattern: {}", titlePattern);
        
        return movieRepository.findByTitleContainingIgnoreCase(titlePattern)
                .doOnComplete(() -> log.debug("Successfully searched movies by title pattern: {}", titlePattern))
                .doOnError(error -> log.error("Failed to search movies by title pattern: {}", titlePattern, error));
    }

    @Override
    public Flux<Movie> getMoviesByYear(Integer year) {
        log.debug("Retrieving movies by year: {}", year);
        
        return movieRepository.findByYearOfRelease(year)
                .doOnComplete(() -> log.debug("Successfully retrieved movies by year: {}", year))
                .doOnError(error -> log.error("Failed to retrieve movies by year: {}", year, error));
    }

    @Override
    public Flux<Movie> getMoviesByYearRange(Integer startYear, Integer endYear) {
        log.debug("Retrieving movies between years {} and {}", startYear, endYear);
        
        return movieRepository.findByYearOfReleaseBetween(startYear, endYear)
                .doOnComplete(() -> log.debug("Successfully retrieved movies between years {} and {}", startYear, endYear))
                .doOnError(error -> log.error("Failed to retrieve movies between years {} and {}", startYear, endYear, error));
    }

    @Override
    public Flux<Movie> searchMoviesByPlot(String keyword) {
        log.debug("Searching movies by plot keyword: {}", keyword);
        
        return movieRepository.findByPlotContainingIgnoreCase(keyword)
                .doOnComplete(() -> log.debug("Successfully searched movies by plot keyword: {}", keyword))
                .doOnError(error -> log.error("Failed to search movies by plot keyword: {}", keyword, error));
    }

    @Override
    public Flux<Movie> searchMovies(SearchMoviesCommand command) {
        log.debug("Searching movies with criteria: {}", command);
        
        return movieRepository.searchMovies(command.titlePattern(), command.yearOfRelease(), command.createdBy())
                .doOnComplete(() -> log.debug("Successfully searched movies with criteria"))
                .doOnError(error -> log.error("Failed to search movies with criteria", error));
    }

    @Override
    public Mono<Boolean> movieExists(UUID movieId) {
        log.debug("Checking if movie exists: {}", movieId);
        
        return movieRepository.existsById(movieId)
                .doOnSuccess(exists -> log.debug("Movie exists check for {}: {}", movieId, exists))
                .doOnError(error -> log.error("Failed to check if movie exists: {}", movieId, error));
    }

    @Override
    public Mono<Boolean> canUserModifyMovie(UUID movieId, UUID userId) {
        log.debug("Checking if user {} can modify movie {}", userId, movieId);
        
        return movieRepository.findById(movieId)
                .map(movie -> movie.getCreatedBy().equals(userId))
                .defaultIfEmpty(false)
                .doOnSuccess(canModify -> log.debug("User {} can modify movie {}: {}", userId, movieId, canModify))
                .doOnError(error -> log.error("Failed to check if user {} can modify movie {}", userId, movieId, error));
    }

    @Override
    public Mono<MovieStatistics> getUserMovieStatistics(UUID userId) {
        log.debug("Getting movie statistics for user: {}", userId);
        
        return Mono.zip(
                movieRepository.countByCreatedBy(userId),
                movieRepository.countActiveByCreatedBy(userId)
        ).map(tuple -> {
            long totalCreated = tuple.getT1();
            long activeCreated = tuple.getT2();
            long deactivatedCreated = totalCreated - activeCreated;
            
            return new MovieStatistics(totalCreated, activeCreated, deactivatedCreated);
        })
        .doOnSuccess(stats -> log.debug("Successfully retrieved movie statistics for user: {}", userId))
        .doOnError(error -> log.error("Failed to get movie statistics for user: {}", userId, error));
    }

    // Additional methods for web handler support
    
    /**
     * Get all active movies with pagination.
     */
    public Flux<Movie> getAllActiveMovies(int offset, int limit) {
        log.debug("Getting active movies with offset: {}, limit: {}", offset, limit);
        return movieRepository.findAllActiveWithPagination(offset, limit);
    }
    
    /**
     * Get active movie count.
     */
    public Mono<Long> getActiveMovieCount() {
        log.debug("Getting active movie count");
        return movieRepository.countActive();
    }
    
    /**
     * Delete a movie by ID and user ID (with authorization check).
     */
    public Mono<Void> deleteMovie(UUID movieId, UUID userId) {
        log.info("Deleting movie {} by user {}", movieId, userId);
        
        return movieRepository.findById(movieId)
                .switchIfEmpty(Mono.error(new MovieNotFoundException(movieId)))
                .flatMap(movie -> {
                    if (!movie.getCreatedBy().equals(userId)) {
                        return Mono.error(new UnauthorizedMovieOperationException(movieId, userId, "delete"));
                    }
                    return movieRepository.deleteById(movieId);
                })
                .doOnSuccess(v -> log.info("Successfully deleted movie {}", movieId))
                .doOnError(error -> log.error("Failed to delete movie {} by user {}", movieId, userId, error));
    }

    private Mono<Movie> updateMovieFields(Movie existingMovie, UpdateMovieCommand command) {
        Movie.MovieBuilder builder = existingMovie.toBuilder()
                .updatedAt(Instant.now());
        
        if (command.title() != null) {
            builder.title(command.title());
        }
        if (command.plot() != null) {
            builder.plot(command.plot());
        }
        if (command.yearOfRelease() != null) {
            builder.yearOfRelease(command.yearOfRelease());
        }
        
        return Mono.just(builder.build());
    }
}
