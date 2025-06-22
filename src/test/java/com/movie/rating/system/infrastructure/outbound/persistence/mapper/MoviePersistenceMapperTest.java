package com.movie.rating.system.infrastructure.outbound.persistence.mapper;

import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MoviePersistenceMapper Tests")
class MoviePersistenceMapperTest {

    private MoviePersistenceMapper mapper;

    private UUID movieId;
    private UUID createdBy;
    private UUID deactivatedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deactivatedAt;

    @BeforeEach
    void setUp() {
        mapper = new MoviePersistenceMapper();
        
        movieId = UUID.randomUUID();
        createdBy = UUID.randomUUID();
        deactivatedBy = UUID.randomUUID();
        createdAt = Instant.now().minusSeconds(3600);
        updatedAt = Instant.now().minusSeconds(1800);
        deactivatedAt = Instant.now();
    }

    @Test
    @DisplayName("Should convert domain Movie to MovieEntity")
    void shouldConvertDomainMovieToEntity() {
        // Given
        Movie movie = Movie.builder()
                .id(movieId)
                .title("The Shawshank Redemption")
                .plot("Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.")
                .yearOfRelease(1994)
                .isActive(true)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deactivatedAt(null)
                .deactivatedBy(null)
                .build();

        // When
        MovieEntity entity = mapper.toEntity(movie);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.id()).isEqualTo(movieId);
        assertThat(entity.title()).isEqualTo("The Shawshank Redemption");
        assertThat(entity.plot()).isEqualTo("Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.");
        assertThat(entity.yearOfRelease()).isEqualTo(1994);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.createdBy()).isEqualTo(createdBy);
        assertThat(entity.createdAt()).isEqualTo(createdAt);
        assertThat(entity.updatedAt()).isEqualTo(updatedAt);
        assertThat(entity.deactivatedAt()).isNull();
        assertThat(entity.deactivatedBy()).isNull();
    }

    @Test
    @DisplayName("Should convert domain Movie to MovieEntity with deactivation data")
    void shouldConvertDomainMovieToEntityWithDeactivationData() {
        // Given
        Movie movie = Movie.builder()
                .id(movieId)
                .title("The Godfather")
                .plot("The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.")
                .yearOfRelease(1972)
                .isActive(false)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deactivatedAt(deactivatedAt)
                .deactivatedBy(deactivatedBy)
                .build();

        // When
        MovieEntity entity = mapper.toEntity(movie);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.id()).isEqualTo(movieId);
        assertThat(entity.title()).isEqualTo("The Godfather");
        assertThat(entity.plot()).isEqualTo("The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.");
        assertThat(entity.yearOfRelease()).isEqualTo(1972);
        assertThat(entity.isActive()).isFalse();
        assertThat(entity.createdBy()).isEqualTo(createdBy);
        assertThat(entity.createdAt()).isEqualTo(createdAt);
        assertThat(entity.updatedAt()).isEqualTo(updatedAt);
        assertThat(entity.deactivatedAt()).isEqualTo(deactivatedAt);
        assertThat(entity.deactivatedBy()).isEqualTo(deactivatedBy);
    }

    @Test
    @DisplayName("Should convert MovieEntity to domain Movie")
    void shouldConvertEntityToDomainMovie() {
        // Given
        MovieEntity entity = new MovieEntity(
                movieId,
                "Pulp Fiction",
                "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.",
                1994,
                true,
                createdBy,
                createdAt,
                updatedAt,
                null,
                null
        );

        // When
        Movie movie = mapper.toDomain(entity);

        // Then
        assertThat(movie).isNotNull();
        assertThat(movie.getId()).isEqualTo(movieId);
        assertThat(movie.getTitle()).isEqualTo("Pulp Fiction");
        assertThat(movie.getPlot()).isEqualTo("The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.");
        assertThat(movie.getYearOfRelease()).isEqualTo(1994);
        assertThat(movie.isActive()).isTrue();
        assertThat(movie.getCreatedBy()).isEqualTo(createdBy);
        assertThat(movie.getCreatedAt()).isEqualTo(createdAt);
        assertThat(movie.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(movie.getDeactivatedAt()).isNull();
        assertThat(movie.getDeactivatedBy()).isNull();
    }

    @Test
    @DisplayName("Should convert MovieEntity to domain Movie with deactivation data")
    void shouldConvertEntityToDomainMovieWithDeactivationData() {
        // Given
        MovieEntity entity = new MovieEntity(
                movieId,
                "Fight Club",
                "An insomniac office worker and a devil-may-care soapmaker form an underground fight club that evolves into something much more.",
                1999,
                false,
                createdBy,
                createdAt,
                updatedAt,
                deactivatedAt,
                deactivatedBy
        );

        // When
        Movie movie = mapper.toDomain(entity);

        // Then
        assertThat(movie).isNotNull();
        assertThat(movie.getId()).isEqualTo(movieId);
        assertThat(movie.getTitle()).isEqualTo("Fight Club");
        assertThat(movie.getPlot()).isEqualTo("An insomniac office worker and a devil-may-care soapmaker form an underground fight club that evolves into something much more.");
        assertThat(movie.getYearOfRelease()).isEqualTo(1999);
        assertThat(movie.isActive()).isFalse();
        assertThat(movie.getCreatedBy()).isEqualTo(createdBy);
        assertThat(movie.getCreatedAt()).isEqualTo(createdAt);
        assertThat(movie.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(movie.getDeactivatedAt()).isEqualTo(deactivatedAt);
        assertThat(movie.getDeactivatedBy()).isEqualTo(deactivatedBy);
    }

    @Test
    @DisplayName("Should handle null isActive in entity conversion by defaulting to true")
    void shouldHandleNullIsActiveInEntityConversion() {
        // Given
        MovieEntity entity = new MovieEntity(
                movieId,
                "The Dark Knight",
                "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests.",
                2008,
                null, // null isActive
                createdBy,
                createdAt,
                updatedAt,
                null,
                null
        );

        // When
        Movie movie = mapper.toDomain(entity);

        // Then
        assertThat(movie).isNotNull();
        assertThat(movie.isActive()).isTrue(); // Should default to true when null
        assertThat(movie.getId()).isEqualTo(movieId);
        assertThat(movie.getTitle()).isEqualTo("The Dark Knight");
        assertThat(movie.getPlot()).isEqualTo("When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests.");
        assertThat(movie.getYearOfRelease()).isEqualTo(2008);
    }

    @Test
    @DisplayName("Should handle null movie in toEntity conversion")
    void shouldHandleNullMovieInToEntityConversion() {
        // When
        MovieEntity entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should handle null entity in toDomain conversion")
    void shouldHandleNullEntityInToDomainConversion() {
        // When
        Movie movie = mapper.toDomain(null);

        // Then
        assertThat(movie).isNull();
    }

    @Test
    @DisplayName("Should preserve all fields in round-trip conversion")
    void shouldPreserveAllFieldsInRoundTripConversion() {
        // Given
        Movie originalMovie = Movie.builder()
                .id(movieId)
                .title("Inception")
                .plot("A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.")
                .yearOfRelease(2010)
                .isActive(true)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deactivatedAt(null)
                .deactivatedBy(null)
                .build();

        // When - Convert to entity and back to domain
        MovieEntity entity = mapper.toEntity(originalMovie);
        Movie convertedMovie = mapper.toDomain(entity);

        // Then
        assertThat(convertedMovie).isNotNull();
        assertThat(convertedMovie.getId()).isEqualTo(originalMovie.getId());
        assertThat(convertedMovie.getTitle()).isEqualTo(originalMovie.getTitle());
        assertThat(convertedMovie.getPlot()).isEqualTo(originalMovie.getPlot());
        assertThat(convertedMovie.getYearOfRelease()).isEqualTo(originalMovie.getYearOfRelease());
        assertThat(convertedMovie.isActive()).isEqualTo(originalMovie.isActive());
        assertThat(convertedMovie.getCreatedBy()).isEqualTo(originalMovie.getCreatedBy());
        assertThat(convertedMovie.getCreatedAt()).isEqualTo(originalMovie.getCreatedAt());
        assertThat(convertedMovie.getUpdatedAt()).isEqualTo(originalMovie.getUpdatedAt());
        assertThat(convertedMovie.getDeactivatedAt()).isEqualTo(originalMovie.getDeactivatedAt());
        assertThat(convertedMovie.getDeactivatedBy()).isEqualTo(originalMovie.getDeactivatedBy());
    }

    @Test
    @DisplayName("Should preserve all fields in round-trip conversion with deactivation data")
    void shouldPreserveAllFieldsInRoundTripConversionWithDeactivationData() {
        // Given
        Movie originalMovie = Movie.builder()
                .id(movieId)
                .title("The Matrix")
                .plot("A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.")
                .yearOfRelease(1999)
                .isActive(false)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deactivatedAt(deactivatedAt)
                .deactivatedBy(deactivatedBy)
                .build();

        // When - Convert to entity and back to domain
        MovieEntity entity = mapper.toEntity(originalMovie);
        Movie convertedMovie = mapper.toDomain(entity);

        // Then
        assertThat(convertedMovie).isNotNull();
        assertThat(convertedMovie.getId()).isEqualTo(originalMovie.getId());
        assertThat(convertedMovie.getTitle()).isEqualTo(originalMovie.getTitle());
        assertThat(convertedMovie.getPlot()).isEqualTo(originalMovie.getPlot());
        assertThat(convertedMovie.getYearOfRelease()).isEqualTo(originalMovie.getYearOfRelease());
        assertThat(convertedMovie.isActive()).isEqualTo(originalMovie.isActive());
        assertThat(convertedMovie.getCreatedBy()).isEqualTo(originalMovie.getCreatedBy());
        assertThat(convertedMovie.getCreatedAt()).isEqualTo(originalMovie.getCreatedAt());
        assertThat(convertedMovie.getUpdatedAt()).isEqualTo(originalMovie.getUpdatedAt());
        assertThat(convertedMovie.getDeactivatedAt()).isEqualTo(originalMovie.getDeactivatedAt());
        assertThat(convertedMovie.getDeactivatedBy()).isEqualTo(originalMovie.getDeactivatedBy());
    }

    @Test
    @DisplayName("Should handle minimal movie data")
    void shouldHandleMinimalMovieData() {
        // Given - Movie with only required fields
        Movie movie = Movie.builder()
                .id(movieId)
                .title("Minimal Movie")
                .plot("A simple plot")
                .yearOfRelease(2023)
                .createdBy(createdBy)
                .build(); // Other fields will be set by builder defaults

        // When
        MovieEntity entity = mapper.toEntity(movie);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.id()).isEqualTo(movieId);
        assertThat(entity.title()).isEqualTo("Minimal Movie");
        assertThat(entity.plot()).isEqualTo("A simple plot");
        assertThat(entity.yearOfRelease()).isEqualTo(2023);
        assertThat(entity.isActive()).isTrue(); // Should be true by default
        assertThat(entity.createdBy()).isEqualTo(createdBy);
        assertThat(entity.createdAt()).isNotNull();
        assertThat(entity.updatedAt()).isNotNull();
        assertThat(entity.deactivatedAt()).isNull();
        assertThat(entity.deactivatedBy()).isNull();
    }

    @Test
    @DisplayName("Should handle entity with minimal data")
    void shouldHandleEntityWithMinimalData() {
        // Given - Entity with only essential fields
        MovieEntity entity = new MovieEntity(
                movieId,
                "Minimal Entity Movie",
                "A simple entity plot",
                2023,
                null, // null isActive
                createdBy,
                createdAt,
                null, // null updatedAt
                null,
                null
        );

        // When
        Movie movie = mapper.toDomain(entity);

        // Then
        assertThat(movie).isNotNull();
        assertThat(movie.getId()).isEqualTo(movieId);
        assertThat(movie.getTitle()).isEqualTo("Minimal Entity Movie");
        assertThat(movie.getPlot()).isEqualTo("A simple entity plot");
        assertThat(movie.getYearOfRelease()).isEqualTo(2023);
        assertThat(movie.isActive()).isTrue(); // Should default to true
        assertThat(movie.getCreatedBy()).isEqualTo(createdBy);
        assertThat(movie.getCreatedAt()).isEqualTo(createdAt);
        assertThat(movie.getUpdatedAt()).isEqualTo(createdAt); // Movie builder sets updatedAt to createdAt if null
        assertThat(movie.getDeactivatedAt()).isNull();
        assertThat(movie.getDeactivatedBy()).isNull();
    }
}
