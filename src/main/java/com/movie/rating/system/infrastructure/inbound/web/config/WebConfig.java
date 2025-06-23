package com.movie.rating.system.infrastructure.inbound.web.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * General web configuration for the movie rating system.
 * Provides beans for validation and other web-related functionality.
 */
@Configuration
public class WebConfig {

    /**
     * Provides a validator bean for request validation.
     *
     * @return Validator instance for validating DTOs
     */
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}
