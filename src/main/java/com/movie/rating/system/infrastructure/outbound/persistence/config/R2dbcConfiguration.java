package com.movie.rating.system.infrastructure.outbound.persistence.config;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class for R2DBC setup including transaction management.
 * This configuration provides reactive transaction support for the movie rating system.
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = "com.movie.rating.system.infrastructure.outbound.persistence.repository")
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    private final ConnectionFactory connectionFactory;

    public R2dbcConfiguration(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        log.info("Initializing R2DBC configuration with transaction management");
    }

    @Override
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    /**
     * Configures the reactive transaction manager for R2DBC.
     * This enables @Transactional annotation support for reactive operations.
     *
     * @param connectionFactory the R2DBC connection factory
     * @return ReactiveTransactionManager instance
     */
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        log.debug("Creating R2DBC reactive transaction manager");
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * Provides R2dbcEntityTemplate for advanced database operations.
     * This can be used for custom queries and operations that go beyond
     * the standard repository methods.
     *
     * @param connectionFactory the R2DBC connection factory
     * @return R2dbcEntityTemplate instance
     */
    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        log.debug("Creating R2DBC entity template");
        return new R2dbcEntityTemplate(connectionFactory);
    }
}
