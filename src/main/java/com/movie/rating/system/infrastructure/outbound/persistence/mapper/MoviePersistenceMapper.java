package com.movie.rating.system.infrastructure.outbound.persistence.mapper;

import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Movie domain entity and MovieEntity database entity.
 */
@Component
public class MoviePersistenceMapper {

    /**
     * Convert domain Movie to database MovieEntity.
     *
     * @param movie the domain movie
     * @return the database entity
     */
    public MovieEntity toEntity(Movie movie) {
        if (movie == null) {
            return null;
        }

        return new MovieEntity(
                movie.getId(),
                movie.getTitle(),
                movie.getPlot(),
                movie.getYearOfRelease(),
                movie.isActive(),
                movie.getCreatedBy(),
                movie.getCreatedAt(),
                movie.getUpdatedAt(),
                movie.getDeactivatedAt(),
                movie.getDeactivatedBy()
        );
    }

    /**
     * Convert database MovieEntity to domain Movie.
     *
     * @param entity the database entity
     * @return the domain movie
     */
    public Movie toDomain(MovieEntity entity) {
        if (entity == null) {
            return null;
        }

        return Movie.builder()
                .id(entity.id())
                .title(entity.title())
                .plot(entity.plot())
                .yearOfRelease(entity.yearOfRelease())
                .isActive(entity.isActive() != null ? entity.isActive() : true)
                .createdBy(entity.createdBy())
                .createdAt(entity.createdAt())
                .updatedAt(entity.updatedAt())
                .deactivatedAt(entity.deactivatedAt())
                .deactivatedBy(entity.deactivatedBy())
                .build();
    }
}
