package com.movie.rating.system.domain.exception;

/**
 * Exception thrown when a movie with the same title and year already exists.
 */
public class DuplicateMovieException extends RuntimeException {

    private final String title;
    private final Integer year;

    public DuplicateMovieException(String title, Integer year) {
        super(String.format("Movie '%s' (%d) already exists", title, year));
        this.title = title;
        this.year = year;
    }

    public DuplicateMovieException(String message) {
        super(message);
        this.title = null;
        this.year = null;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }
}
