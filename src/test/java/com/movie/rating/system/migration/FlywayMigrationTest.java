package com.movie.rating.system.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class FlywayMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("movie_rating_system_test")
            .withUsername("test")
            .withPassword("test");

    private Connection connection;
    private Flyway flyway;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();

        // Clean the database before each test to ensure fresh migrations
        flyway.clean();
    }

    @Test
    void testFlywayMigrationExecutesSuccessfully() {
        // Execute migrations
        var result = flyway.migrate();
        int migrationsExecuted = result.migrationsExecuted;

        // Verify that all 2 migrations were executed
        assertEquals(2, migrationsExecuted, "Expected 1 migrations to be executed");

        // Verify migration info
        var migrationInfo = flyway.info();
        assertEquals(2, migrationInfo.all().length, "Expected 2 total migrations");
        assertEquals(2, migrationInfo.applied().length, "Expected 2 applied migrations");
    }
}
