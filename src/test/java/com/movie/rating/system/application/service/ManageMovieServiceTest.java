package com.movie.rating.system.application.service;

import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.domain.exception.DuplicateMovieException;
import com.movie.rating.system.domain.exception.MovieNotFoundException;
import com.movie.rating.system.domain.exception.UnauthorizedMovieOperationException;
import com.movie.rating.system.domain.port.inbound.ManageMovieUseCase.CreateMovieCommand;
import com.movie.rating.system.domain.port.inbound.ManageMovieUseCase.UpdateMovieCommand;
import com.movie.rating.system.domain.port.inbound.ManageMovieUseCase.DeactivateMovieCommand;
import com.movie.rating.system.domain.port.inbound.ManageMovieUseCase.SearchMoviesCommand;
import com.movie.rating.system.domain.port.inbound.ManageMovieUseCase.MovieStatistics;
import com.movie.rating.system.domain.port.outbound.MovieRepository;
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
@DisplayName("ManageMovieService Tests")
class ManageMovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    private ManageMovieService movieService;

    private UUID movieId;
    private UUID userId;
    private UUID otherUserId;
    private Movie testMovie;
    private CreateMovieCommand createCommand;
    private UpdateMovieCommand updateCommand;
    private DeactivateMovieCommand deactivateCommand;
    private Instant now;

    @BeforeEach
    void setUp() {
        movieService = new ManageMovieService(movieRepository);
        
        movieId = UUID.randomUUID();
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        now = Instant.now();
        
        testMovie = Movie.builder()
                .id(movieId)
                .title("The Shawshank Redemption")
                .plot("Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.")
                .yearOfRelease(1994)
                .isActive(true)
                .createdBy(userId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        createCommand = new CreateMovieCommand(
                "The Shawshank Redemption",
                "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
                1994,
                userId
        );

        updateCommand = new UpdateMovieCommand(
                movieId,
                "The Shawshank Redemption - Updated",
                "Updated plot",
                1995,
                userId
        );

        deactivateCommand = new DeactivateMovieCommand(movieId, userId);
    }

    @Test
    @DisplayName("Should successfully create movie when no duplicate exists")
    void shouldCreateMovieWhenNoDuplicateExists() {
        // Given
        when(movieRepository.existsByTitleAndYearOfRelease("The Shawshank Redemption", 1994))
                .thenReturn(Mono.just(false));
        when(movieRepository.save(any(Movie.class))).thenReturn(Mono.just(testMovie));

        // When
        Mono<Movie> result = movieService.createMovie(createCommand);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).existsByTitleAndYearOfRelease("The Shawshank Redemption", 1994);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should throw DuplicateMovieException when movie already exists")
    void shouldThrowDuplicateMovieExceptionWhenMovieExists() {
        // Given
        when(movieRepository.existsByTitleAndYearOfRelease("The Shawshank Redemption", 1994))
                .thenReturn(Mono.just(true));

        // When
        Mono<Movie> result = movieService.createMovie(createCommand);

        // Then
        StepVerifier.create(result)
                .expectError(DuplicateMovieException.class)
                .verify();

        verify(movieRepository).existsByTitleAndYearOfRelease("The Shawshank Redemption", 1994);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should successfully get movie by ID when movie exists")
    void shouldGetMovieByIdWhenMovieExists() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));

        // When
        Mono<Movie> result = movieService.getMovieById(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).findById(movieId);
    }

    @Test
    @DisplayName("Should throw MovieNotFoundException when movie does not exist")
    void shouldThrowMovieNotFoundExceptionWhenMovieDoesNotExist() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.empty());

        // When
        Mono<Movie> result = movieService.getMovieById(movieId);

        // Then
        StepVerifier.create(result)
                .expectError(MovieNotFoundException.class)
                .verify();

        verify(movieRepository).findById(movieId);
    }

    @Test
    @DisplayName("Should successfully update movie when user is authorized")
    void shouldUpdateMovieWhenUserIsAuthorized() {
        // Given
        Movie updatedMovie = testMovie.toBuilder()
                .title("The Shawshank Redemption - Updated")
                .plot("Updated plot")
                .yearOfRelease(1995)
                .updatedAt(Instant.now())
                .build();

        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));
        when(movieRepository.existsByTitleAndYearOfRelease("The Shawshank Redemption - Updated", 1995))
                .thenReturn(Mono.just(false));
        when(movieRepository.save(any(Movie.class))).thenReturn(Mono.just(updatedMovie));

        // When
        Mono<Movie> result = movieService.updateMovie(updateCommand);

        // Then
        StepVerifier.create(result)
                .expectNext(updatedMovie)
                .verifyComplete();

        verify(movieRepository).findById(movieId);
        verify(movieRepository).existsByTitleAndYearOfRelease("The Shawshank Redemption - Updated", 1995);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedMovieOperationException when user is not authorized to update")
    void shouldThrowUnauthorizedExceptionWhenUserNotAuthorizedToUpdate() {
        // Given
        UpdateMovieCommand unauthorizedCommand = new UpdateMovieCommand(
                movieId, "Updated Title", "Updated Plot", 1995, otherUserId);
        
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));

        // When
        Mono<Movie> result = movieService.updateMovie(unauthorizedCommand);

        // Then
        StepVerifier.create(result)
                .expectError(UnauthorizedMovieOperationException.class)
                .verify();

        verify(movieRepository).findById(movieId);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should throw DuplicateMovieException when updating to existing title and year")
    void shouldThrowDuplicateExceptionWhenUpdatingToExistingTitleAndYear() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));
        when(movieRepository.existsByTitleAndYearOfRelease("The Shawshank Redemption - Updated", 1995))
                .thenReturn(Mono.just(true));

        // When
        Mono<Movie> result = movieService.updateMovie(updateCommand);

        // Then
        StepVerifier.create(result)
                .expectError(DuplicateMovieException.class)
                .verify();

        verify(movieRepository).findById(movieId);
        verify(movieRepository).existsByTitleAndYearOfRelease("The Shawshank Redemption - Updated", 1995);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should successfully deactivate movie when user is authorized")
    void shouldDeactivateMovieWhenUserIsAuthorized() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));
        when(movieRepository.deleteById(movieId)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = movieService.deactivateMovie(deactivateCommand);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(movieRepository).findById(movieId);
        verify(movieRepository).deleteById(movieId);
    }

    @Test
    @DisplayName("Should throw UnauthorizedMovieOperationException when user is not authorized to deactivate")
    void shouldThrowUnauthorizedExceptionWhenUserNotAuthorizedToDeactivate() {
        // Given
        DeactivateMovieCommand unauthorizedCommand = new DeactivateMovieCommand(movieId, otherUserId);
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));

        // When
        Mono<Void> result = movieService.deactivateMovie(unauthorizedCommand);

        // Then
        StepVerifier.create(result)
                .expectError(UnauthorizedMovieOperationException.class)
                .verify();

        verify(movieRepository).findById(movieId);
        verify(movieRepository, never()).deleteById(movieId);
    }

    @Test
    @DisplayName("Should successfully reactivate inactive movie")
    void shouldReactivateInactiveMovie() {
        // Given
        Movie inactiveMovie = testMovie.toBuilder()
                .isActive(false)
                .deactivatedAt(now)
                .deactivatedBy(userId)
                .build();
        
        Movie reactivatedMovie = inactiveMovie.toBuilder()
                .isActive(true)
                .deactivatedAt(null)
                .deactivatedBy(null)
                .updatedAt(Instant.now())
                .build();

        when(movieRepository.findById(movieId)).thenReturn(Mono.just(inactiveMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(Mono.just(reactivatedMovie));

        // When
        Mono<Movie> result = movieService.reactivateMovie(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(reactivatedMovie)
                .verifyComplete();

        verify(movieRepository).findById(movieId);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should return active movie when trying to reactivate already active movie")
    void shouldReturnActiveMovieWhenAlreadyActive() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));

        // When
        Mono<Movie> result = movieService.reactivateMovie(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).findById(movieId);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should get all active movies")
    void shouldGetAllActiveMovies() {
        // Given
        List<Movie> movies = List.of(testMovie);
        when(movieRepository.findAllActive()).thenReturn(Flux.fromIterable(movies));

        // When
        Flux<Movie> result = movieService.getAllActiveMovies();

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).findAllActive();
    }

    @Test
    @DisplayName("Should get movies by creator")
    void shouldGetMoviesByCreator() {
        // Given
        List<Movie> movies = List.of(testMovie);
        when(movieRepository.findByCreatedBy(userId)).thenReturn(Flux.fromIterable(movies));

        // When
        Flux<Movie> result = movieService.getMoviesByCreator(userId);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).findByCreatedBy(userId);
    }

    @Test
    @DisplayName("Should search movies by title")
    void shouldSearchMoviesByTitle() {
        // Given
        String titlePattern = "Shawshank";
        List<Movie> movies = List.of(testMovie);
        when(movieRepository.findByTitleContainingIgnoreCase(titlePattern))
                .thenReturn(Flux.fromIterable(movies));

        // When
        Flux<Movie> result = movieService.searchMoviesByTitle(titlePattern);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).findByTitleContainingIgnoreCase(titlePattern);
    }

    @Test
    @DisplayName("Should get movies by year")
    void shouldGetMoviesByYear() {
        // Given
        Integer year = 1994;
        List<Movie> movies = List.of(testMovie);
        when(movieRepository.findByYearOfRelease(year)).thenReturn(Flux.fromIterable(movies));

        // When
        Flux<Movie> result = movieService.getMoviesByYear(year);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).findByYearOfRelease(year);
    }

    @Test
    @DisplayName("Should get movies by year range")
    void shouldGetMoviesByYearRange() {
        // Given
        Integer startYear = 1990;
        Integer endYear = 2000;
        List<Movie> movies = List.of(testMovie);
        when(movieRepository.findByYearOfReleaseBetween(startYear, endYear))
                .thenReturn(Flux.fromIterable(movies));

        // When
        Flux<Movie> result = movieService.getMoviesByYearRange(startYear, endYear);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).findByYearOfReleaseBetween(startYear, endYear);
    }

    @Test
    @DisplayName("Should search movies by plot")
    void shouldSearchMoviesByPlot() {
        // Given
        String keyword = "redemption";
        List<Movie> movies = List.of(testMovie);
        when(movieRepository.findByPlotContainingIgnoreCase(keyword))
                .thenReturn(Flux.fromIterable(movies));

        // When
        Flux<Movie> result = movieService.searchMoviesByPlot(keyword);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).findByPlotContainingIgnoreCase(keyword);
    }

    @Test
    @DisplayName("Should search movies with criteria")
    void shouldSearchMoviesWithCriteria() {
        // Given
        SearchMoviesCommand searchCommand = new SearchMoviesCommand(
                "Shawshank", 1994, userId, 0, 10);
        List<Movie> movies = List.of(testMovie);
        when(movieRepository.searchMovies("Shawshank", 1994, userId))
                .thenReturn(Flux.fromIterable(movies));

        // When
        Flux<Movie> result = movieService.searchMovies(searchCommand);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).searchMovies("Shawshank", 1994, userId);
    }

    @Test
    @DisplayName("Should check if movie exists")
    void shouldCheckIfMovieExists() {
        // Given
        when(movieRepository.existsById(movieId)).thenReturn(Mono.just(true));

        // When
        Mono<Boolean> result = movieService.movieExists(movieId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(movieRepository).existsById(movieId);
    }

    @Test
    @DisplayName("Should check if user can modify movie")
    void shouldCheckIfUserCanModifyMovie() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));

        // When
        Mono<Boolean> result = movieService.canUserModifyMovie(movieId, userId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(movieRepository).findById(movieId);
    }

    @Test
    @DisplayName("Should return false when user cannot modify movie")
    void shouldReturnFalseWhenUserCannotModifyMovie() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));

        // When
        Mono<Boolean> result = movieService.canUserModifyMovie(movieId, otherUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(movieRepository).findById(movieId);
    }

    @Test
    @DisplayName("Should return false when movie does not exist for modification check")
    void shouldReturnFalseWhenMovieDoesNotExistForModificationCheck() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.empty());

        // When
        Mono<Boolean> result = movieService.canUserModifyMovie(movieId, userId);

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(movieRepository).findById(movieId);
    }

    @Test
    @DisplayName("Should get user movie statistics")
    void shouldGetUserMovieStatistics() {
        // Given
        when(movieRepository.countByCreatedBy(userId)).thenReturn(Mono.just(5L));
        when(movieRepository.countActiveByCreatedBy(userId)).thenReturn(Mono.just(3L));

        // When
        Mono<MovieStatistics> result = movieService.getUserMovieStatistics(userId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(stats -> 
                    stats.totalMoviesCreated() == 5L &&
                    stats.activeMoviesCreated() == 3L &&
                    stats.deactivatedMoviesCreated() == 2L)
                .verifyComplete();

        verify(movieRepository).countByCreatedBy(userId);
        verify(movieRepository).countActiveByCreatedBy(userId);
    }

    @Test
    @DisplayName("Should get all active movies with pagination")
    void shouldGetAllActiveMoviesWithPagination() {
        // Given
        int offset = 0;
        int limit = 10;
        List<Movie> movies = List.of(testMovie);
        when(movieRepository.findAllActiveWithPagination(offset, limit))
                .thenReturn(Flux.fromIterable(movies));

        // When
        Flux<Movie> result = movieService.getAllActiveMovies(offset, limit);

        // Then
        StepVerifier.create(result)
                .expectNext(testMovie)
                .verifyComplete();

        verify(movieRepository).findAllActiveWithPagination(offset, limit);
    }

    @Test
    @DisplayName("Should get active movie count")
    void shouldGetActiveMovieCount() {
        // Given
        when(movieRepository.countActive()).thenReturn(Mono.just(100L));

        // When
        Mono<Long> result = movieService.getActiveMovieCount();

        // Then
        StepVerifier.create(result)
                .expectNext(100L)
                .verifyComplete();

        verify(movieRepository).countActive();
    }

    @Test
    @DisplayName("Should delete movie when user is authorized")
    void shouldDeleteMovieWhenUserIsAuthorized() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));
        when(movieRepository.deleteById(movieId)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = movieService.deleteMovie(movieId, userId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(movieRepository).findById(movieId);
        verify(movieRepository).deleteById(movieId);
    }

    @Test
    @DisplayName("Should throw UnauthorizedMovieOperationException when user is not authorized to delete")
    void shouldThrowUnauthorizedExceptionWhenUserNotAuthorizedToDelete() {
        // Given
        when(movieRepository.findById(movieId)).thenReturn(Mono.just(testMovie));

        // When
        Mono<Void> result = movieService.deleteMovie(movieId, otherUserId);

        // Then
        StepVerifier.create(result)
                .expectError(UnauthorizedMovieOperationException.class)
                .verify();

        verify(movieRepository).findById(movieId);
        verify(movieRepository, never()).deleteById(movieId);
    }

    @Test
    @DisplayName("Should handle repository errors gracefully")
    void shouldHandleRepositoryErrorsGracefully() {
        // Given
        RuntimeException repositoryError = new RuntimeException("Database connection failed");
        when(movieRepository.findById(movieId)).thenReturn(Mono.error(repositoryError));

        // When
        Mono<Movie> result = movieService.getMovieById(movieId);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(movieRepository).findById(movieId);
    }

    @Test
    @DisplayName("Should handle empty results for list operations")
    void shouldHandleEmptyResultsForListOperations() {
        // Given
        when(movieRepository.findAllActive()).thenReturn(Flux.empty());

        // When
        Flux<Movie> result = movieService.getAllActiveMovies();

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(movieRepository).findAllActive();
    }
}
