package com.movie.rating.system.infrastructure.outbound.persistence.adapter;

import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieEntity;
import com.movie.rating.system.infrastructure.outbound.persistence.mapper.MoviePersistenceMapper;
import com.movie.rating.system.infrastructure.outbound.persistence.repository.R2dbcMovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("R2dbcMovieRepositoryAdapter Tests")
class R2dbcMovieRepositoryAdapterTest {

    @Mock
    private R2dbcMovieRepository r2dbcRepository;

    @Mock
    private MoviePersistenceMapper mapper;

    private R2dbcMovieRepositoryAdapter adapter;

    private UUID movieId;
    private UUID userId;
    private Movie movie;
    private MovieEntity movieEntity;
    private Instant now;

    @BeforeEach
    void setUp() {
        adapter = new R2dbcMovieRepositoryAdapter(r2dbcRepository, mapper);
        
        movieId = UUID.randomUUID();
        userId = UUID.randomUUID();
        now = Instant.now();
        
        movie = Movie.builder()
                .id(movieId)
                .title("The Shawshank Redemption")
                .plot("Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.")
                .yearOfRelease(1994)
                .isActive(true)
                .createdBy(userId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        movieEntity = new MovieEntity(
                movieId,
                "The Shawshank Redemption",
                "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
                1994,
                true,
                userId,
                now,
                now,
                null,
                null
        );
    }

    @Test
    @DisplayName("Should successfully save movie")
    void shouldSaveMovie() {
        // Given
        when(mapper.toEntity(movie)).thenReturn(movieEntity);
        when(r2dbcRepository.save(movieEntity)).thenReturn(Mono.just(movieEntity));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Mono<Movie> result = adapter.save(movie);

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(mapper).toEntity(movie);
        verify(r2dbcRepository).save(movieEntity);
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should find movie by ID")
    void shouldFindMovieById() {
        // Given
        when(r2dbcRepository.findById(movieId)).thenReturn(Mono.just(movieEntity));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Mono<Movie> result = adapter.findById(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(r2dbcRepository).findById(movieId);
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should return empty when movie not found by ID")
    void shouldReturnEmptyWhenMovieNotFoundById() {
        // Given
        when(r2dbcRepository.findById(movieId)).thenReturn(Mono.empty());

        // When
        Mono<Movie> result = adapter.findById(movieId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(r2dbcRepository).findById(movieId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find all active movies")
    void shouldFindAllActiveMovies() {
        // Given
        List<MovieEntity> entities = List.of(movieEntity);
        when(r2dbcRepository.findAllActive()).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Flux<Movie> result = adapter.findAllActive();

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(r2dbcRepository).findAllActive();
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should find movies by creator")
    void shouldFindMoviesByCreator() {
        // Given
        List<MovieEntity> entities = List.of(movieEntity);
        when(r2dbcRepository.findByCreatedByAndIsActiveTrue(userId)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Flux<Movie> result = adapter.findByCreatedBy(userId);

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(r2dbcRepository).findByCreatedByAndIsActiveTrue(userId);
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should find movies by title pattern")
    void shouldFindMoviesByTitlePattern() {
        // Given
        String titlePattern = "Shawshank";
        List<MovieEntity> entities = List.of(movieEntity);
        when(r2dbcRepository.findByTitleContainingIgnoreCaseAndIsActiveTrue(titlePattern))
                .thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Flux<Movie> result = adapter.findByTitleContainingIgnoreCase(titlePattern);

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(r2dbcRepository).findByTitleContainingIgnoreCaseAndIsActiveTrue(titlePattern);
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should find movies by year of release")
    void shouldFindMoviesByYearOfRelease() {
        // Given
        Integer year = 1994;
        List<MovieEntity> entities = List.of(movieEntity);
        when(r2dbcRepository.findByYearOfReleaseAndIsActiveTrue(year)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Flux<Movie> result = adapter.findByYearOfRelease(year);

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(r2dbcRepository).findByYearOfReleaseAndIsActiveTrue(year);
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should find movies by year range")
    void shouldFindMoviesByYearRange() {
        // Given
        Integer startYear = 1990;
        Integer endYear = 2000;
        List<MovieEntity> entities = List.of(movieEntity);
        when(r2dbcRepository.findByYearOfReleaseBetweenAndIsActiveTrue(startYear, endYear))
                .thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Flux<Movie> result = adapter.findByYearOfReleaseBetween(startYear, endYear);

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(r2dbcRepository).findByYearOfReleaseBetweenAndIsActiveTrue(startYear, endYear);
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should find movies by plot keyword")
    void shouldFindMoviesByPlotKeyword() {
        // Given
        String plotKeyword = "redemption";
        List<MovieEntity> entities = List.of(movieEntity);
        when(r2dbcRepository.findByPlotContainingIgnoreCaseAndIsActiveTrue(plotKeyword))
                .thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Flux<Movie> result = adapter.findByPlotContainingIgnoreCase(plotKeyword);

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(r2dbcRepository).findByPlotContainingIgnoreCaseAndIsActiveTrue(plotKeyword);
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should check if movie exists by ID")
    void shouldCheckIfMovieExistsById() {
        // Given
        when(r2dbcRepository.existsById(movieId)).thenReturn(Mono.just(true));

        // When
        Mono<Boolean> result = adapter.existsById(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(r2dbcRepository).existsById(movieId);
    }

    @Test
    @DisplayName("Should check if movie exists by title and year")
    void shouldCheckIfMovieExistsByTitleAndYear() {
        // Given
        String title = "The Shawshank Redemption";
        Integer year = 1994;
        when(r2dbcRepository.existsByTitleAndYearOfReleaseAndIsActiveTrue(title, year)).thenReturn(Mono.just(true));

        // When
        Mono<Boolean> result = adapter.existsByTitleAndYearOfRelease(title, year);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(r2dbcRepository).existsByTitleAndYearOfReleaseAndIsActiveTrue(title, year);
    }

    @Test
    @DisplayName("Should soft delete movie by ID")
    void shouldSoftDeleteMovieById() {
        // Given
        MovieEntity deactivatedEntity = new MovieEntity(
                movieEntity.id(),
                movieEntity.title(),
                movieEntity.plot(),
                movieEntity.yearOfRelease(),
                false, // inactive
                movieEntity.createdBy(),
                movieEntity.createdAt(),
                Instant.now(),
                Instant.now(),
                null
        );
        
        when(r2dbcRepository.findById(movieId)).thenReturn(Mono.just(movieEntity));
        when(r2dbcRepository.save(any(MovieEntity.class))).thenReturn(Mono.just(deactivatedEntity));

        // When
        Mono<Void> result = adapter.deleteById(movieId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(r2dbcRepository).findById(movieId);
        verify(r2dbcRepository).save(any(MovieEntity.class));
    }

    @Test
    @DisplayName("Should throw error when trying to delete non-existent movie")
    void shouldThrowErrorWhenDeletingNonExistentMovie() {
        // Given
        when(r2dbcRepository.findById(movieId)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = adapter.deleteById(movieId);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(r2dbcRepository).findById(movieId);
        verify(r2dbcRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should count active movies")
    void shouldCountActiveMovies() {
        // Given
        Long expectedCount = 100L;
        when(r2dbcRepository.countByIsActiveTrue()).thenReturn(Mono.just(expectedCount));

        // When
        Mono<Long> result = adapter.countActive();

        // Then
        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();

        verify(r2dbcRepository).countByIsActiveTrue();
    }

    @Test
    @DisplayName("Should count movies by creator")
    void shouldCountMoviesByCreator() {
        // Given
        Long expectedCount = 5L;
        when(r2dbcRepository.countAllByCreatedBy(userId)).thenReturn(Mono.just(expectedCount));

        // When
        Mono<Long> result = adapter.countByCreatedBy(userId);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();

        verify(r2dbcRepository).countAllByCreatedBy(userId);
    }

    @Test
    @DisplayName("Should count active movies by creator")
    void shouldCountActiveMoviesByCreator() {
        // Given
        Long expectedCount = 3L;
        when(r2dbcRepository.countActiveByCreatedBy(userId)).thenReturn(Mono.just(expectedCount));

        // When
        Mono<Long> result = adapter.countActiveByCreatedBy(userId);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();

        verify(r2dbcRepository).countActiveByCreatedBy(userId);
    }

    @Test
    @DisplayName("Should find active movies with pagination")
    void shouldFindActiveMoviesWithPagination() {
        // Given
        int offset = 0;
        int limit = 10;
        List<MovieEntity> entities = List.of(movieEntity);
        when(r2dbcRepository.findAllActiveWithPagination(offset, limit)).thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Flux<Movie> result = adapter.findAllActiveWithPagination(offset, limit);

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(r2dbcRepository).findAllActiveWithPagination(offset, limit);
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should search movies with criteria")
    void shouldSearchMoviesWithCriteria() {
        // Given
        String titlePattern = "Shawshank";
        Integer yearOfRelease = 1994;
        UUID createdBy = userId;
        List<MovieEntity> entities = List.of(movieEntity);
        when(r2dbcRepository.searchMovies(titlePattern, yearOfRelease, createdBy))
                .thenReturn(Flux.fromIterable(entities));
        when(mapper.toDomain(movieEntity)).thenReturn(movie);

        // When
        Flux<Movie> result = adapter.searchMovies(titlePattern, yearOfRelease, createdBy);

        // Then
        StepVerifier.create(result)
                .expectNext(movie)
                .verifyComplete();

        verify(r2dbcRepository).searchMovies(titlePattern, yearOfRelease, createdBy);
        verify(mapper).toDomain(movieEntity);
    }

    @Test
    @DisplayName("Should handle save error gracefully")
    void shouldHandleSaveErrorGracefully() {
        // Given
        RuntimeException saveError = new RuntimeException("Database error");
        when(mapper.toEntity(movie)).thenReturn(movieEntity);
        when(r2dbcRepository.save(movieEntity)).thenReturn(Mono.error(saveError));

        // When
        Mono<Movie> result = adapter.save(movie);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(mapper).toEntity(movie);
        verify(r2dbcRepository).save(movieEntity);
    }

    @Test
    @DisplayName("Should handle find error gracefully")
    void shouldHandleFindErrorGracefully() {
        // Given
        RuntimeException findError = new RuntimeException("Database error");
        when(r2dbcRepository.findById(movieId)).thenReturn(Mono.error(findError));

        // When
        Mono<Movie> result = adapter.findById(movieId);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(r2dbcRepository).findById(movieId);
    }

    @Test
    @DisplayName("Should handle empty results for list operations")
    void shouldHandleEmptyResultsForListOperations() {
        // Given
        when(r2dbcRepository.findAllActive()).thenReturn(Flux.empty());

        // When
        Flux<Movie> result = adapter.findAllActive();

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(r2dbcRepository).findAllActive();
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should handle null movie in save operation")
    void shouldHandleNullMovieInSaveOperation() {
        // When
        Mono<Movie> result = adapter.save(null);

        // Then
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();

        // Verify that repository methods are not called with null
        verify(mapper, never()).toEntity(any());
        verify(r2dbcRepository, never()).save(any());
    }
}
