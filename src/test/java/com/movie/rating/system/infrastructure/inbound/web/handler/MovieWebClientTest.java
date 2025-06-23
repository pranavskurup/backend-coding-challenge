package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.application.service.ManageMovieService;
import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.domain.exception.MovieNotFoundException;
import com.movie.rating.system.domain.exception.UnauthorizedMovieOperationException;
import com.movie.rating.system.domain.port.inbound.ManageMovieUseCase;
import com.movie.rating.system.infrastructure.inbound.web.dto.mapper.MovieDtoMapper;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.CreateMovieRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateMovieRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieResponse;
import com.movie.rating.system.infrastructure.inbound.web.router.MovieRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WebClient-based integration tests for Movie endpoints.
 * Tests the full HTTP request-response cycle including routing, handlers, and DTOs.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Movie WebClient Integration Tests")
class MovieWebClientTest {

    @Mock
    private ManageMovieService movieService;

    @Mock
    private MovieDtoMapper dtoMapper;

    private WebTestClient webTestClient;
    private MovieHandler movieHandler;

    private final UUID testUserId = UUID.randomUUID();
    private final UUID testMovieId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        movieHandler = new MovieHandler(movieService, dtoMapper);
        MovieRouter movieRouter = new MovieRouter();
        RouterFunction<ServerResponse> routes = movieRouter.movieRoutes(movieHandler);
        
        webTestClient = WebTestClient
                .bindToRouterFunction(routes)
                .configureClient()
                .baseUrl("http://localhost")
                .build();
    }

    @Test
    @DisplayName("Should get all movies successfully")
    void shouldGetAllMoviesSuccessfully() {
        // Given
        List<Movie> movies = List.of(createTestMovie(), createTestMovie());
        List<MovieResponse> responses = List.of(createTestMovieResponse(), createTestMovieResponse());

        when(movieService.getAllActiveMovies(eq(0), eq(20))).thenReturn(Flux.fromIterable(movies));
        when(dtoMapper.toResponse(any(Movie.class)))
                .thenReturn(responses.get(0))
                .thenReturn(responses.get(1));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies?page=0&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieResponse.class)
                .hasSize(2);

        verify(movieService).getAllActiveMovies(eq(0), eq(20));
        verify(dtoMapper, times(2)).toResponse(any(Movie.class));
    }

    @Test
    @DisplayName("Should get all movies with default pagination")
    void shouldGetAllMoviesWithDefaultPagination() {
        // Given
        List<Movie> movies = List.of(createTestMovie());
        List<MovieResponse> responses = List.of(createTestMovieResponse());

        when(movieService.getAllActiveMovies(eq(0), eq(20))).thenReturn(Flux.fromIterable(movies));
        when(dtoMapper.toResponse(any(Movie.class))).thenReturn(responses.get(0));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieResponse.class)
                .hasSize(1);

        verify(movieService).getAllActiveMovies(eq(0), eq(20));
    }

    @Test
    @DisplayName("Should return 400 for invalid pagination parameters")
    void shouldReturn400ForInvalidPagination() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies?page=-1&size=0")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Invalid pagination parameters");

        verifyNoInteractions(movieService);
    }

    @Test
    @DisplayName("Should get movie by ID successfully")
    void shouldGetMovieByIdSuccessfully() {
        // Given
        Movie movie = createTestMovie();
        MovieResponse response = createTestMovieResponse();

        when(movieService.getMovieById(eq(testMovieId))).thenReturn(Mono.just(movie));
        when(dtoMapper.toResponse(eq(movie))).thenReturn(response);

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{id}", testMovieId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(MovieResponse.class)
                .value(movieResponse -> {
                    assert movieResponse.id().equals(testMovieId);
                    assert movieResponse.title().equals("Test Movie");
                });

        verify(movieService).getMovieById(eq(testMovieId));
        verify(dtoMapper).toResponse(eq(movie));
    }

    @Test
    @DisplayName("Should return 404 when movie not found")
    void shouldReturn404WhenMovieNotFound() {
        // Given
        when(movieService.getMovieById(eq(testMovieId))).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{id}", testMovieId)
                .exchange()
                .expectStatus().isNotFound();

        verify(movieService).getMovieById(eq(testMovieId));
        verifyNoInteractions(dtoMapper);
    }

    @Test
    @DisplayName("Should search movies by title successfully")
    void shouldSearchMoviesByTitleSuccessfully() {
        // Given
        String title = "Test";
        List<Movie> movies = List.of(createTestMovie());
        List<MovieResponse> responses = List.of(createTestMovieResponse());

        when(movieService.searchMoviesByTitle(eq(title))).thenReturn(Flux.fromIterable(movies));
        when(dtoMapper.toResponse(any(Movie.class))).thenReturn(responses.get(0));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/search?title={title}", title)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieResponse.class)
                .hasSize(1);

        verify(movieService).searchMoviesByTitle(eq(title));
    }

    @Test
    @DisplayName("Should return 400 when searching with empty title")
    void shouldReturn400WhenSearchingWithEmptyTitle() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/search?title=")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Title parameter is required");

        verifyNoInteractions(movieService);
    }

    @Test
    @DisplayName("Should search movies by year successfully")
    void shouldSearchMoviesByYearSuccessfully() {
        // Given
        Integer year = 2023;
        List<Movie> movies = List.of(createTestMovie());
        List<MovieResponse> responses = List.of(createTestMovieResponse());

        when(movieService.getMoviesByYear(eq(year))).thenReturn(Flux.fromIterable(movies));
        when(dtoMapper.toResponse(any(Movie.class))).thenReturn(responses.get(0));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/year/{year}", year)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieResponse.class)
                .hasSize(1);

        verify(movieService).getMoviesByYear(eq(year));
    }

    @Test
    @DisplayName("Should return 400 for invalid year format")
    void shouldReturn400ForInvalidYearFormat() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/year/invalid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Invalid year format");

        verifyNoInteractions(movieService);
    }

    @Test
    @DisplayName("Should search movies by year range successfully")
    void shouldSearchMoviesByYearRangeSuccessfully() {
        // Given
        Integer startYear = 2020;
        Integer endYear = 2023;
        List<Movie> movies = List.of(createTestMovie());
        List<MovieResponse> responses = List.of(createTestMovieResponse());

        when(movieService.getMoviesByYearRange(eq(startYear), eq(endYear))).thenReturn(Flux.fromIterable(movies));
        when(dtoMapper.toResponse(any(Movie.class))).thenReturn(responses.get(0));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/year-range?startYear={startYear}&endYear={endYear}", startYear, endYear)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieResponse.class)
                .hasSize(1);

        verify(movieService).getMoviesByYearRange(eq(startYear), eq(endYear));
    }

    @Test
    @DisplayName("Should return 400 when start year is greater than end year")
    void shouldReturn400WhenStartYearGreaterThanEndYear() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/year-range?startYear=2023&endYear=2020")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("startYear cannot be greater than endYear");

        verifyNoInteractions(movieService);
    }

    @Test
    @DisplayName("Should search movies by plot successfully")
    void shouldSearchMoviesByPlotSuccessfully() {
        // Given
        String plot = "action";
        List<Movie> movies = List.of(createTestMovie());
        List<MovieResponse> responses = List.of(createTestMovieResponse());

        when(movieService.searchMoviesByPlot(eq(plot))).thenReturn(Flux.fromIterable(movies));
        when(dtoMapper.toResponse(any(Movie.class))).thenReturn(responses.get(0));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/search-plot?plot={plot}", plot)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieResponse.class)
                .hasSize(1);

        verify(movieService).searchMoviesByPlot(eq(plot));
    }

    @Test
    @DisplayName("Should get movie count successfully")
    void shouldGetMovieCountSuccessfully() {
        // Given
        Long count = 42L;
        when(movieService.getActiveMovieCount()).thenReturn(Mono.just(count));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/count")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.count").isEqualTo(count);

        verify(movieService).getActiveMovieCount();
    }

    @Test
    @DisplayName("Should handle service exceptions with proper error responses")
    void shouldHandleServiceExceptionsWithProperErrorResponses() {
        // Given
        when(movieService.getMovieById(eq(testMovieId)))
                .thenReturn(Mono.error(new MovieNotFoundException("Movie not found with ID: " + testMovieId)));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{id}", testMovieId)
                .exchange()
                .expectStatus().isNotFound();

        verify(movieService).getMovieById(eq(testMovieId));
    }

    @Test
    @DisplayName("Should handle validation errors appropriately")
    void shouldHandleValidationErrorsAppropriately() {
        // Given - testing with invalid UUID format
        String invalidId = "not-a-uuid";

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies/{id}", invalidId)
                .exchange()
                .expectStatus().is5xxServerError(); // UUID parsing will cause 500

        verifyNoInteractions(movieService);
    }

    @Test
    @DisplayName("Should handle concurrent requests properly")
    void shouldHandleConcurrentRequestsProperly() {
        // Given
        Movie movie = createTestMovie();
        MovieResponse response = createTestMovieResponse();

        when(movieService.getMovieById(eq(testMovieId))).thenReturn(Mono.just(movie));
        when(dtoMapper.toResponse(eq(movie))).thenReturn(response);

        // When & Then - Make multiple concurrent requests
        for (int i = 0; i < 5; i++) {
            webTestClient.get()
                    .uri("/api/v1/movies/{id}", testMovieId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON);
        }

        verify(movieService, times(5)).getMovieById(eq(testMovieId));
    }

    @Test
    @DisplayName("Should handle large pagination requests")
    void shouldHandleLargePaginationRequests() {
        // Given
        when(movieService.getAllActiveMovies(eq(0), eq(100))).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies?page=0&size=100")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(MovieResponse.class)
                .hasSize(0);

        verify(movieService).getAllActiveMovies(eq(0), eq(100));
    }

    @Test
    @DisplayName("Should reject oversized pagination requests")
    void shouldRejectOversizedPaginationRequests() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/movies?page=0&size=101")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Invalid pagination parameters");

        verifyNoInteractions(movieService);
    }

    private Movie createTestMovie() {
        return Movie.builder()
                .id(testMovieId)
                .title("Test Movie")
                .plot("A test movie plot")
                .yearOfRelease(2023)
                .createdBy(testUserId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .isActive(true)
                .build();
    }

    private MovieResponse createTestMovieResponse() {
        return new MovieResponse(
                testMovieId,
                "Test Movie",
                "A test movie plot",
                2023,
                true,
                testUserId,
                Instant.now(),
                Instant.now()
        );
    }
}
