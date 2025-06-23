package com.movie.rating.system;

import org.springframework.boot.SpringApplication;

public class TestMovieRatingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(MovieRatingSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
