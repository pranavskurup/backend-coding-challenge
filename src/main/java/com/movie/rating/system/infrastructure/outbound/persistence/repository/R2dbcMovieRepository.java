package com.movie.rating.system.infrastructure.outbound.persistence.repository;

import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository interface for MovieEntity.
 * Provides database operations for movie persistence.
 */
public interface R2dbcMovieRepository extends R2dbcRepository<MovieEntity, UUID> {

    /**
     * Find all active movies.
     */
    @Query("SELECT * FROM movies WHERE is_active = true ORDER BY created_at DESC")
    Flux<MovieEntity> findAllActive();

    /**
     * Find movies created by a specific user.
     */
    @Query("SELECT * FROM movies WHERE created_by = :createdBy AND is_active = true ORDER BY created_at DESC")
    Flux<MovieEntity> findByCreatedByAndIsActiveTrue(@Param("createdBy") UUID createdBy);

    /**
     * Find movies by title pattern (case-insensitive).
     */
    @Query("SELECT * FROM movies WHERE LOWER(title) LIKE LOWER(CONCAT('%', :titlePattern, '%')) AND is_active = true ORDER BY title")
    Flux<MovieEntity> findByTitleContainingIgnoreCaseAndIsActiveTrue(@Param("titlePattern") String titlePattern);

    /**
     * Find movies by year of release.
     */
    @Query("SELECT * FROM movies WHERE year_of_release = :year AND is_active = true ORDER BY title")
    Flux<MovieEntity> findByYearOfReleaseAndIsActiveTrue(@Param("year") Integer year);

    /**
     * Find movies released within a year range.
     */
    @Query("SELECT * FROM movies WHERE year_of_release BETWEEN :startYear AND :endYear AND is_active = true ORDER BY year_of_release DESC, title")
    Flux<MovieEntity> findByYearOfReleaseBetweenAndIsActiveTrue(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    /**
     * Find movies by plot content (case-insensitive search).
     */
    @Query("SELECT * FROM movies WHERE LOWER(plot) LIKE LOWER(CONCAT('%', :plotKeyword, '%')) AND is_active = true ORDER BY title")
    Flux<MovieEntity> findByPlotContainingIgnoreCaseAndIsActiveTrue(@Param("plotKeyword") String plotKeyword);

    /**
     * Check if a movie with the same title and year already exists.
     */
    @Query("SELECT COUNT(*) > 0 FROM movies WHERE LOWER(title) = LOWER(:title) AND year_of_release = :yearOfRelease AND is_active = true")
    Mono<Boolean> existsByTitleAndYearOfReleaseAndIsActiveTrue(@Param("title") String title, @Param("yearOfRelease") Integer yearOfRelease);

    /**
     * Count total number of active movies.
     */
    @Query("SELECT COUNT(*) FROM movies WHERE is_active = true")
    Mono<Long> countByIsActiveTrue();

    /**
     * Count movies created by a specific user.
     */
    @Query("SELECT COUNT(*) FROM movies WHERE created_by = :createdBy AND is_active = true")
    Mono<Long> countByCreatedByAndIsActiveTrue(@Param("createdBy") UUID createdBy);

    /**
     * Find movies with pagination support.
     */
    @Query("SELECT * FROM movies WHERE is_active = true ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<MovieEntity> findAllActiveWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * Search movies by multiple criteria.
     */
    @Query("""
            SELECT * FROM movies 
            WHERE is_active = true
            AND (:titlePattern IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :titlePattern, '%')))
            AND (:yearOfRelease IS NULL OR year_of_release = :yearOfRelease)
            AND (:createdBy IS NULL OR created_by = :createdBy)
            ORDER BY created_at DESC
            """)
    Flux<MovieEntity> searchMovies(
            @Param("titlePattern") String titlePattern,
            @Param("yearOfRelease") Integer yearOfRelease,
            @Param("createdBy") UUID createdBy
    );

    /**
     * Find all movies by a specific user (including inactive ones).
     */
    @Query("SELECT * FROM movies WHERE created_by = :createdBy ORDER BY created_at DESC")
    Flux<MovieEntity> findAllByCreatedBy(@Param("createdBy") UUID createdBy);

    /**
     * Count all movies created by a specific user (including inactive ones).
     */
    @Query("SELECT COUNT(*) FROM movies WHERE created_by = :createdBy")
    Mono<Long> countAllByCreatedBy(@Param("createdBy") UUID createdBy);

    /**
     * Count inactive movies created by a specific user.
     */
    @Query("SELECT COUNT(*) FROM movies WHERE created_by = :createdBy AND is_active = false")
    Mono<Long> countByCreatedByAndIsActiveFalse(@Param("createdBy") UUID createdBy);

    /**
     * Find the oldest and newest movie years.
     */
    @Query("SELECT MIN(year_of_release) as min_year, MAX(year_of_release) as max_year FROM movies WHERE is_active = true")
    Mono<YearRange> findYearRange();

    /**
     * Record for year range query result.
     */
    record YearRange(Integer minYear, Integer maxYear) {}
}
