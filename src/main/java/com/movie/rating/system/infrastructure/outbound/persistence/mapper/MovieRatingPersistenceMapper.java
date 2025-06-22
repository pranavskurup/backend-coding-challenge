package com.movie.rating.system.infrastructure.outbound.persistence.mapper;

import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.MovieRatingEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between MovieRating domain entity and MovieRatingEntity database entity.
 */
@Component
public class MovieRatingPersistenceMapper {

    /**
     * Convert domain MovieRating to database MovieRatingEntity.
     *
     * @param movieRating the domain movie rating
     * @return the database entity
     */
    public MovieRatingEntity toEntity(MovieRating movieRating) {
        if (movieRating == null) {
            return null;
        }

        return new MovieRatingEntity(
                movieRating.getId(),
                movieRating.getMovieId(),
                movieRating.getUserId(),
                movieRating.getRating(),
                movieRating.getReview(),
                movieRating.isActive(),
                movieRating.getCreatedAt(),
                movieRating.getUpdatedAt()
        );
    }

    /**
     * Convert database MovieRatingEntity to domain MovieRating.
     *
     * @param entity the database entity
     * @return the domain movie rating
     */
    public MovieRating toDomain(MovieRatingEntity entity) {
        if (entity == null) {
            return null;
        }

        return MovieRating.builder()
                .id(entity.id())
                .movieId(entity.movieId())
                .userId(entity.userId())
                .rating(entity.rating())
                .review(entity.review())
                .isActive(entity.isActive() != null ? entity.isActive() : true)
                .createdAt(entity.createdAt())
                .updatedAt(entity.updatedAt())
                .build();
    }
}
