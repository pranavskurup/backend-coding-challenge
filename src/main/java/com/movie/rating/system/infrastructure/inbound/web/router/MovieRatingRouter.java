package com.movie.rating.system.infrastructure.inbound.web.router;

import com.movie.rating.system.infrastructure.inbound.web.handler.MovieRatingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Router configuration for movie rating-related endpoints.
 */
@Configuration
public class MovieRatingRouter {

    @Bean
    public RouterFunction<ServerResponse> movieRatingRoutes(MovieRatingHandler ratingHandler) {
        return RouterFunctions
                .route(POST("/api/v1/ratings").and(accept(MediaType.APPLICATION_JSON)), 
                       ratingHandler::createRating)
                // Specific routes must come before generic {id} route
                .andRoute(GET("/api/v1/ratings/my"), 
                         ratingHandler::getMyRatings)
                .andRoute(GET("/api/v1/ratings/recent"), 
                         ratingHandler::getRecentRatings)
                // Generic {id} route must come after specific routes
                .andRoute(GET("/api/v1/ratings/{id}"), 
                         ratingHandler::getRatingById)
                .andRoute(PUT("/api/v1/ratings/{id}").and(accept(MediaType.APPLICATION_JSON)), 
                         ratingHandler::updateRating)
                .andRoute(DELETE("/api/v1/ratings/{id}"), 
                         ratingHandler::deleteRating)
                // Movie-specific rating routes
                .andRoute(GET("/api/v1/movies/{movieId}/ratings"), 
                         ratingHandler::getRatingsByMovie)
                .andRoute(GET("/api/v1/movies/{movieId}/ratings/stats"), 
                         ratingHandler::getMovieRatingStats)
                .andRoute(GET("/api/v1/movies/{movieId}/ratings/average"), 
                         ratingHandler::getMovieAverageRating)
                .andRoute(GET("/api/v1/movies/{movieId}/ratings/user"), 
                         ratingHandler::getUserMovieRating);
    }
}
