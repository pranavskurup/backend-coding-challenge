package com.movie.rating.system.infrastructure.inbound.web.dto.mapper;

import com.movie.rating.system.domain.entity.Movie;
import com.movie.rating.system.domain.entity.MovieRating;
import com.movie.rating.system.domain.port.inbound.ManageMovieUseCase.CreateMovieCommand;
import com.movie.rating.system.domain.port.inbound.ManageMovieUseCase.UpdateMovieCommand;
import com.movie.rating.system.domain.port.inbound.ManageMovieRatingUseCase.CreateRatingCommand;
import com.movie.rating.system.domain.port.inbound.ManageMovieRatingUseCase.UpdateRatingCommand;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.CreateMovieRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.CreateMovieRatingRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateMovieRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateMovieRatingRequest;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieResponse;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.MovieRatingResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for converting between DTOs and domain entities/commands.
 */
@Component
public class MovieDtoMapper {

    // Movie mappings
    public CreateMovieCommand toCreateCommand(CreateMovieRequest request, UUID createdBy) {
        return new CreateMovieCommand(
                request.title(),
                request.plot(),
                request.yearOfRelease(),
                createdBy
        );
    }

    public UpdateMovieCommand toUpdateCommand(UpdateMovieRequest request, UUID movieId, UUID userId) {
        return new UpdateMovieCommand(
                movieId,
                request.title(),
                request.plot(),
                request.yearOfRelease(),
                userId
        );
    }

    public MovieResponse toResponse(Movie movie) {
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

    // Movie Rating mappings
    public CreateRatingCommand toCreateCommand(CreateMovieRatingRequest request, UUID userId) {
        return new CreateRatingCommand(
                request.movieId(),
                userId,
                request.rating(),
                request.review()
        );
    }

    public UpdateRatingCommand toUpdateCommand(UpdateMovieRatingRequest request, UUID ratingId, UUID userId) {
        return new UpdateRatingCommand(
                ratingId,
                userId,
                request.rating(),
                request.review()
        );
    }

    public MovieRatingResponse toResponse(MovieRating movieRating) {
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
