package com.movie.rating.system.infrastructure.inbound.web.router;

import com.movie.rating.system.infrastructure.inbound.web.handler.MovieHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Router configuration for movie-related endpoints.
 */
@Configuration
public class MovieRouter {

    @Bean
    public RouterFunction<ServerResponse> movieRoutes(MovieHandler movieHandler) {
        return RouterFunctions
                .route(POST("/api/v1/movies").and(accept(MediaType.APPLICATION_JSON)), 
                       movieHandler::createMovie)
                // Specific routes must come before generic {id} route
                .andRoute(GET("/api/v1/movies/my"), 
                         movieHandler::getMyMovies)
                .andRoute(GET("/api/v1/movies/search").and(queryParam("title", t -> true)), 
                         movieHandler::searchMoviesByTitle)
                .andRoute(GET("/api/v1/movies/year/{year}"), 
                         movieHandler::searchMoviesByYear)
                .andRoute(GET("/api/v1/movies/year-range")
                         .and(queryParam("startYear", s -> true))
                         .and(queryParam("endYear", e -> true)), 
                         movieHandler::searchMoviesByYearRange)
                .andRoute(GET("/api/v1/movies/search-plot").and(queryParam("plot", p -> true)), 
                         movieHandler::searchMoviesByPlot)
                .andRoute(GET("/api/v1/movies/count"), 
                         movieHandler::getMovieCount)
                .andRoute(GET("/api/v1/movies"), 
                         movieHandler::getAllMovies)
                // Generic {id} route must come after all specific routes
                .andRoute(GET("/api/v1/movies/{id}"), 
                         movieHandler::getMovieById)
                .andRoute(PUT("/api/v1/movies/{id}").and(accept(MediaType.APPLICATION_JSON)), 
                         movieHandler::updateMovie)
                .andRoute(DELETE("/api/v1/movies/{id}"), 
                         movieHandler::deleteMovie);
    }
}
