package com.movie.rating.system.infrastructure.inbound.web.mapper;

import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieRatingResponse;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between domain entities and web DTOs.
 */
@Component
public class WebDtoMapper {

    /**
     * Convert Movie domain entity to MovieResponse DTO.
     */
    public MovieResponse toMovieResponse(Movie movie) {
        if (movie == null) {
            return null;
        }

        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getPlot(),
                movie.getYearOfRelease(),
                movie.isActive(),
                movie.getCreatedBy(),
                movie.getCreatedAt(),
                movie.getUpdatedAt()
        );
    }

    /**
     * Convert MovieRating domain entity to MovieRatingResponse DTO.
     */
    public MovieRatingResponse toMovieRatingResponse(MovieRating movieRating) {
        if (movieRating == null) {
            return null;
        }

        return new MovieRatingResponse(
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
}
