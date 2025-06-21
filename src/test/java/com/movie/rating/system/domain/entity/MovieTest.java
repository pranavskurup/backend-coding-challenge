package com.movie.rating.system.domain.entity;

import com.movie.rating.system.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Movie entity.
 * Tests entity validation, builder functionality, and business methods.
 */
@DisplayName("Movie Entity Tests")
class MovieTest {

    @Nested
    @DisplayName("Movie Builder Tests")
    class MovieBuilderTests {

        @Test
        @DisplayName("Should create movie with all required fields")
        void shouldCreateMovieWithAllRequiredFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID createdBy = UUID.randomUUID();
            String title = "The Matrix";
            String plot = "A computer programmer discovers reality as he knows it is a simulation.";
            Integer yearOfRelease = 1999;
            Instant now = Instant.now();

            // When
            Movie movie = Movie.builder()
                    .id(id)
                    .title(title)
                    .plot(plot)
                    .yearOfRelease(yearOfRelease)
                    .createdBy(createdBy)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // Then
            assertThat(movie.getId()).isEqualTo(id);
            assertThat(movie.getTitle()).isEqualTo(title);
            assertThat(movie.getPlot()).isEqualTo(plot);
            assertThat(movie.getYearOfRelease()).isEqualTo(yearOfRelease);
            assertThat(movie.getCreatedBy()).isEqualTo(createdBy);
            assertThat(movie.isActive()).isTrue();
            assertThat(movie.getCreatedAt()).isEqualTo(now);
            assertThat(movie.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should create movie with default values when timestamps not provided")
        void shouldCreateMovieWithDefaultTimestamps() {
            // Given
            Instant beforeCreation = Instant.now();

            // When
            Movie movie = Movie.builder()
                    .title("Test Movie")
                    .plot("Test plot")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build();

            // Then
            Instant afterCreation = Instant.now();
            
            assertThat(movie.getCreatedAt()).isBetween(beforeCreation, afterCreation);
            assertThat(movie.getUpdatedAt()).isEqualTo(movie.getCreatedAt());
            assertThat(movie.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw ValidationException when title is null")
        void shouldThrowValidationExceptionWhenTitleIsNull() {
            // When & Then
            assertThatThrownBy(() -> Movie.builder()
                    .plot("Test plot")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Movie title is required");
        }

        @Test
        @DisplayName("Should throw ValidationException when title is empty")
        void shouldThrowValidationExceptionWhenTitleIsEmpty() {
            // When & Then
            assertThatThrownBy(() -> Movie.builder()
                    .title("")
                    .plot("Test plot")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Movie title is required");
        }

        @Test
        @DisplayName("Should throw ValidationException when title is too long")
        void shouldThrowValidationExceptionWhenTitleIsTooLong() {
            // Given
            String longTitle = "A".repeat(256); // Max is 255

            // When & Then
            assertThatThrownBy(() -> Movie.builder()
                    .title(longTitle)
                    .plot("Test plot")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Movie title is too long");
        }

        @Test
        @DisplayName("Should create movie successfully when plot is null")
        void shouldCreateMovieSuccessfullyWhenPlotIsNull() {
            // When
            Movie movie = Movie.builder()
                    .title("Test Movie")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build();

            // Then
            assertThat(movie.getTitle()).isEqualTo("Test Movie");
            assertThat(movie.getPlot()).isNull();
        }

        @Test
        @DisplayName("Should throw ValidationException when plot is too long")
        void shouldThrowValidationExceptionWhenPlotIsTooLong() {
            // Given
            String longPlot = "A".repeat(10001); // Max is 10000

            // When & Then
            assertThatThrownBy(() -> Movie.builder()
                    .title("Test Movie")
                    .plot(longPlot)
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Plot is too long");
        }

        @Test
        @DisplayName("Should throw ValidationException when year of release is null")
        void shouldThrowValidationExceptionWhenYearOfReleaseIsNull() {
            // When & Then
            assertThatThrownBy(() -> Movie.builder()
                    .title("Test Movie")
                    .plot("Test plot")
                    .createdBy(UUID.randomUUID())
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Year of release is required");
        }

        @Test
        @DisplayName("Should throw ValidationException when year of release is too early")
        void shouldThrowValidationExceptionWhenYearOfReleaseIsTooEarly() {
            // When & Then
            assertThatThrownBy(() -> Movie.builder()
                    .title("Test Movie")
                    .plot("Test plot")
                    .yearOfRelease(1887) // Before 1888
                    .createdBy(UUID.randomUUID())
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid year of release");
        }

        @Test
        @DisplayName("Should throw ValidationException when year of release is in the future")
        void shouldThrowValidationExceptionWhenYearOfReleaseIsInFuture() {
            // Given
            int futureYear = Instant.now().atZone(java.time.ZoneOffset.UTC).getYear() + 10;

            // When & Then
            assertThatThrownBy(() -> Movie.builder()
                    .title("Test Movie")
                    .plot("Test plot")
                    .yearOfRelease(futureYear)
                    .createdBy(UUID.randomUUID())
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid year of release");
        }

        @Test
        @DisplayName("Should throw ValidationException when created by user ID is null")
        void shouldThrowValidationExceptionWhenCreatedByUserIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> Movie.builder()
                    .title("Test Movie")
                    .plot("Test plot")
                    .yearOfRelease(2023)
                    .build())
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Created by user ID is required");
        }
    }

    @Nested
    @DisplayName("Movie Business Methods Tests")
    class MovieBusinessMethodsTests {

        private Movie createTestMovie() {
            return Movie.builder()
                    .id(UUID.randomUUID())
                    .title("Test Movie")
                    .plot("Test plot")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build();
        }

        @Test
        @DisplayName("Should check if movie was created by user correctly")
        void shouldCheckIfMovieWasCreatedByUserCorrectly() {
            // Given
            UUID userId = UUID.randomUUID();
            Movie movie = Movie.builder()
                    .title("Test Movie")
                    .plot("Test plot")
                    .yearOfRelease(2023)
                    .createdBy(userId)
                    .build();

            // When & Then
            assertThat(movie.isCreatedBy(userId)).isTrue();
            assertThat(movie.isCreatedBy(UUID.randomUUID())).isFalse();
        }

        @Test
        @DisplayName("Should update movie details correctly")
        void shouldUpdateMovieDetailsCorrectly() {
            // Given
            Movie movie = createTestMovie();
            String newTitle = "Updated Movie";
            String newPlot = "Updated plot";
            Integer newYear = 2024;

            // When
            Movie updatedMovie = movie.updateWith(newTitle, newPlot, newYear);

            // Then
            assertThat(updatedMovie.getTitle()).isEqualTo(newTitle);
            assertThat(updatedMovie.getPlot()).isEqualTo(newPlot);
            assertThat(updatedMovie.getYearOfRelease()).isEqualTo(newYear);
            assertThat(updatedMovie.getUpdatedAt()).isAfter(movie.getUpdatedAt());
            assertThat(updatedMovie.getId()).isEqualTo(movie.getId());
            assertThat(updatedMovie.getCreatedAt()).isEqualTo(movie.getCreatedAt());
        }

        @Test
        @DisplayName("Should keep original values when update parameters are null")
        void shouldKeepOriginalValuesWhenUpdateParametersAreNull() {
            // Given
            Movie movie = createTestMovie();

            // When
            Movie updatedMovie = movie.updateWith(null, null, null);

            // Then
            assertThat(updatedMovie.getTitle()).isEqualTo(movie.getTitle());
            assertThat(updatedMovie.getPlot()).isEqualTo(movie.getPlot());
            assertThat(updatedMovie.getYearOfRelease()).isEqualTo(movie.getYearOfRelease());
            assertThat(updatedMovie.getUpdatedAt()).isAfter(movie.getUpdatedAt());
        }

        @Test
        @DisplayName("Should deactivate movie correctly")
        void shouldDeactivateMovieCorrectly() {
            // Given
            Movie movie = createTestMovie();

            // When
            Movie deactivatedMovie = movie.deactivate(UUID.randomUUID());

            // Then
            assertThat(deactivatedMovie.isActive()).isFalse();
            assertThat(deactivatedMovie.getUpdatedAt()).isAfter(movie.getUpdatedAt());
            assertThat(deactivatedMovie.getId()).isEqualTo(movie.getId());
        }

        @Test
        @DisplayName("Should reactivate movie correctly")
        void shouldReactivateMovieCorrectly() {
            // Given
            Movie movie = createTestMovie().deactivate(UUID.randomUUID());

            // When
            Movie reactivatedMovie = movie.reactivate();

            // Then
            assertThat(reactivatedMovie.isActive()).isTrue();
            assertThat(reactivatedMovie.getUpdatedAt()).isAfter(movie.getUpdatedAt());
            assertThat(reactivatedMovie.getId()).isEqualTo(movie.getId());
        }

        @Test
        @DisplayName("Should get display title correctly")
        void shouldGetDisplayTitleCorrectly() {
            // Given
            Movie movie = Movie.builder()
                    .title("The Matrix")
                    .plot("Test plot")
                    .yearOfRelease(1999)
                    .createdBy(UUID.randomUUID())
                    .build();

            // When & Then
            assertThat(movie.getDisplayTitle()).isEqualTo("The Matrix (1999)");
        }

        @Test
        @DisplayName("Should check if movie is effectively active correctly")
        void shouldCheckIfMovieIsEffectivelyActiveCorrectly() {
            // Given
            Movie activeMovie = createTestMovie();
            Movie deactivatedMovie = activeMovie.deactivate(UUID.randomUUID());

            // When & Then
            assertThat(activeMovie.isEffectivelyActive()).isTrue();
            assertThat(deactivatedMovie.isEffectivelyActive()).isFalse();
        }

        @Test
        @DisplayName("Should check if movie is deactivated by user correctly")
        void shouldCheckIfMovieIsDeactivatedByUserCorrectly() {
            // Given
            UUID deactivatorId = UUID.randomUUID();
            Movie movie = createTestMovie();
            Movie deactivatedMovie = movie.deactivate(deactivatorId);

            // When & Then
            assertThat(deactivatedMovie.isDeactivatedBy(deactivatorId)).isTrue();
            assertThat(deactivatedMovie.isDeactivatedBy(UUID.randomUUID())).isFalse();
            assertThat(movie.isDeactivatedBy(deactivatorId)).isFalse();
        }
    }

    @Nested
    @DisplayName("Movie Equals and HashCode Tests")
    class MovieEqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when IDs are the same")
        void shouldBeEqualWhenIdsAreTheSame() {
            // Given
            UUID id = UUID.randomUUID();
            Movie movie1 = Movie.builder()
                    .id(id)
                    .title("Movie 1")
                    .plot("Plot 1")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build();

            Movie movie2 = Movie.builder()
                    .id(id)
                    .title("Movie 2")
                    .plot("Plot 2")
                    .yearOfRelease(2024)
                    .createdBy(UUID.randomUUID())
                    .build();

            // When & Then
            assertThat(movie1).isEqualTo(movie2);
            assertThat(movie1.hashCode()).isEqualTo(movie2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            // Given
            Movie movie1 = Movie.builder()
                    .id(UUID.randomUUID())
                    .title("Same Movie")
                    .plot("Same plot")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build();

            Movie movie2 = Movie.builder()
                    .id(UUID.randomUUID())
                    .title("Same Movie")
                    .plot("Same plot")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build();

            // When & Then
            assertThat(movie1).isNotEqualTo(movie2);
            assertThat(movie1.hashCode()).isNotEqualTo(movie2.hashCode());
        }
    }

    @Nested
    @DisplayName("Movie ToString Tests")
    class MovieToStringTests {

        @Test
        @DisplayName("Should include all important fields in toString")
        void shouldIncludeAllImportantFieldsInToString() {
            // Given
            UUID id = UUID.randomUUID();
            Movie movie = Movie.builder()
                    .id(id)
                    .title("Test Movie")
                    .plot("Test plot")
                    .yearOfRelease(2023)
                    .createdBy(UUID.randomUUID())
                    .build();

            // When
            String toString = movie.toString();

            // Then
            assertThat(toString).contains("Test Movie");
            assertThat(toString).contains("2023");
            assertThat(toString).contains(id.toString());
        }
    }
}
