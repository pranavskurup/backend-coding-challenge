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
        
        // Verify that all 5 migrations were executed
        assertEquals(5, migrationsExecuted, "Expected 5 migrations to be executed");
        
        // Verify migration info
        var migrationInfo = flyway.info();
        assertEquals(5, migrationInfo.all().length, "Expected 5 total migrations");
        assertEquals(5, migrationInfo.applied().length, "Expected 5 applied migrations");
    }

    @Test
    void testUsersTableCreated() throws SQLException {
        flyway.migrate();
        
        assertTrue(tableExists("users"), "Users table should exist");
        
        // Verify table structure
        List<String> columns = getTableColumns("users");
        assertTrue(columns.contains("id"), "Users table should have id column");
        assertTrue(columns.contains("username"), "Users table should have username column");
        assertTrue(columns.contains("email"), "Users table should have email column");
        assertTrue(columns.contains("password_hash"), "Users table should have password_hash column");
        assertTrue(columns.contains("first_name"), "Users table should have first_name column");
        assertTrue(columns.contains("last_name"), "Users table should have last_name column");
        assertTrue(columns.contains("is_active"), "Users table should have is_active column");
        assertTrue(columns.contains("created_at"), "Users table should have created_at column");
        assertTrue(columns.contains("updated_at"), "Users table should have updated_at column");
        assertTrue(columns.contains("deactivated_at"), "Users table should have deactivated_at column");
        
        // Verify indexes
        assertTrue(indexExists("idx_users_username"), "Username index should exist");
        assertTrue(indexExists("idx_users_email"), "Email index should exist");
        assertTrue(indexExists("idx_users_is_active"), "Is_active index should exist");
        assertTrue(indexExists("idx_users_created_at"), "Created_at index should exist");
        
        // Verify unique constraints
        assertTrue(hasUniqueConstraint("users", "username"), "Username should have unique constraint");
        assertTrue(hasUniqueConstraint("users", "email"), "Email should have unique constraint");
    }

    @Test
    void testMoviesTableCreated() throws SQLException {
        flyway.migrate();
        
        assertTrue(tableExists("movies"), "Movies table should exist");
        
        // Verify table structure
        List<String> columns = getTableColumns("movies");
        assertTrue(columns.contains("id"), "Movies table should have id column");
        assertTrue(columns.contains("title"), "Movies table should have title column");
        assertTrue(columns.contains("description"), "Movies table should have description column");
        assertTrue(columns.contains("genre"), "Movies table should have genre column");
        assertTrue(columns.contains("release_year"), "Movies table should have release_year column");
        assertTrue(columns.contains("director"), "Movies table should have director column");
        assertTrue(columns.contains("duration_minutes"), "Movies table should have duration_minutes column");
        assertTrue(columns.contains("owner_id"), "Movies table should have owner_id column");
        assertTrue(columns.contains("is_active"), "Movies table should have is_active column");
        assertTrue(columns.contains("rating_enabled"), "Movies table should have rating_enabled column");
        assertTrue(columns.contains("created_at"), "Movies table should have created_at column");
        assertTrue(columns.contains("updated_at"), "Movies table should have updated_at column");
        assertTrue(columns.contains("deactivated_at"), "Movies table should have deactivated_at column");
        
        // Verify foreign key constraint
        assertTrue(hasForeignKeyConstraint("movies", "owner_id", "users", "id"), 
                "Movies table should have foreign key constraint to users table");
        
        // Verify indexes
        assertTrue(indexExists("idx_movies_title"), "Title index should exist");
        assertTrue(indexExists("idx_movies_genre"), "Genre index should exist");
        assertTrue(indexExists("idx_movies_release_year"), "Release year index should exist");
        assertTrue(indexExists("idx_movies_owner_id"), "Owner ID index should exist");
        assertTrue(indexExists("idx_movies_is_active"), "Is_active index should exist");
        assertTrue(indexExists("idx_movies_rating_enabled"), "Rating_enabled index should exist");
        assertTrue(indexExists("idx_movies_created_at"), "Created_at index should exist");
    }

    @Test
    void testRatingsTableCreated() throws SQLException {
        flyway.migrate();
        
        assertTrue(tableExists("ratings"), "Ratings table should exist");
        
        // Verify table structure
        List<String> columns = getTableColumns("ratings");
        assertTrue(columns.contains("id"), "Ratings table should have id column");
        assertTrue(columns.contains("user_id"), "Ratings table should have user_id column");
        assertTrue(columns.contains("movie_id"), "Ratings table should have movie_id column");
        assertTrue(columns.contains("rating"), "Ratings table should have rating column");
        assertTrue(columns.contains("review"), "Ratings table should have review column");
        assertTrue(columns.contains("created_at"), "Ratings table should have created_at column");
        assertTrue(columns.contains("updated_at"), "Ratings table should have updated_at column");
        
        // Verify foreign key constraints
        assertTrue(hasForeignKeyConstraint("ratings", "user_id", "users", "id"), 
                "Ratings table should have foreign key constraint to users table");
        assertTrue(hasForeignKeyConstraint("ratings", "movie_id", "movies", "id"), 
                "Ratings table should have foreign key constraint to movies table");
        
        // Verify unique constraint (user_id, movie_id)
        assertTrue(hasCompositeUniqueConstraint("ratings", List.of("user_id", "movie_id")), 
                "Ratings table should have unique constraint on user_id and movie_id");
        
        // Verify check constraint for rating value
        assertTrue(hasCheckConstraint("ratings", "rating"), 
                "Ratings table should have check constraint on rating column");
        
        // Verify indexes
        assertTrue(indexExists("idx_ratings_user_id"), "User ID index should exist");
        assertTrue(indexExists("idx_ratings_movie_id"), "Movie ID index should exist");
        assertTrue(indexExists("idx_ratings_rating"), "Rating index should exist");
        assertTrue(indexExists("idx_ratings_created_at"), "Created_at index should exist");
    }

    @Test
    void testJwtTokensTableCreated() throws SQLException {
        flyway.migrate();
        
        assertTrue(tableExists("jwt_tokens"), "JWT tokens table should exist");
        
        // Verify table structure
        List<String> columns = getTableColumns("jwt_tokens");
        assertTrue(columns.contains("id"), "JWT tokens table should have id column");
        assertTrue(columns.contains("user_id"), "JWT tokens table should have user_id column");
        assertTrue(columns.contains("token_hash"), "JWT tokens table should have token_hash column");
        assertTrue(columns.contains("token_type"), "JWT tokens table should have token_type column");
        assertTrue(columns.contains("expires_at"), "JWT tokens table should have expires_at column");
        assertTrue(columns.contains("is_revoked"), "JWT tokens table should have is_revoked column");
        assertTrue(columns.contains("created_at"), "JWT tokens table should have created_at column");
        assertTrue(columns.contains("revoked_at"), "JWT tokens table should have revoked_at column");
        
        // Verify foreign key constraint
        assertTrue(hasForeignKeyConstraint("jwt_tokens", "user_id", "users", "id"), 
                "JWT tokens table should have foreign key constraint to users table");
        
        // Verify unique constraint on token_hash
        assertTrue(hasUniqueConstraint("jwt_tokens", "token_hash"), 
                "Token hash should have unique constraint");
        
        // Verify indexes
        assertTrue(indexExists("idx_jwt_tokens_user_id"), "User ID index should exist");
        assertTrue(indexExists("idx_jwt_tokens_token_hash"), "Token hash index should exist");
        assertTrue(indexExists("idx_jwt_tokens_token_type"), "Token type index should exist");
        assertTrue(indexExists("idx_jwt_tokens_expires_at"), "Expires at index should exist");
        assertTrue(indexExists("idx_jwt_tokens_is_revoked"), "Is revoked index should exist");
        assertTrue(indexExists("idx_jwt_tokens_created_at"), "Created_at index should exist");
    }

    @Test
    void testUserSessionsTableCreated() throws SQLException {
        flyway.migrate();
        
        assertTrue(tableExists("user_sessions"), "User sessions table should exist");
        
        // Verify table structure
        List<String> columns = getTableColumns("user_sessions");
        assertTrue(columns.contains("id"), "User sessions table should have id column");
        assertTrue(columns.contains("user_id"), "User sessions table should have user_id column");
        assertTrue(columns.contains("session_token"), "User sessions table should have session_token column");
        assertTrue(columns.contains("ip_address"), "User sessions table should have ip_address column");
        assertTrue(columns.contains("user_agent"), "User sessions table should have user_agent column");
        assertTrue(columns.contains("is_active"), "User sessions table should have is_active column");
        assertTrue(columns.contains("created_at"), "User sessions table should have created_at column");
        assertTrue(columns.contains("last_accessed_at"), "User sessions table should have last_accessed_at column");
        assertTrue(columns.contains("expires_at"), "User sessions table should have expires_at column");
        
        // Verify foreign key constraint
        assertTrue(hasForeignKeyConstraint("user_sessions", "user_id", "users", "id"), 
                "User sessions table should have foreign key constraint to users table");
        
        // Verify unique constraint on session_token
        assertTrue(hasUniqueConstraint("user_sessions", "session_token"), 
                "Session token should have unique constraint");
        
        // Verify indexes
        assertTrue(indexExists("idx_user_sessions_user_id"), "User ID index should exist");
        assertTrue(indexExists("idx_user_sessions_session_token"), "Session token index should exist");
        assertTrue(indexExists("idx_user_sessions_is_active"), "Is active index should exist");
        assertTrue(indexExists("idx_user_sessions_expires_at"), "Expires at index should exist");
        assertTrue(indexExists("idx_user_sessions_created_at"), "Created_at index should exist");
        assertTrue(indexExists("idx_user_sessions_last_accessed_at"), "Last accessed at index should exist");
    }

    @Test
    void testAllTablesHaveUuidPrimaryKeys() throws SQLException {
        flyway.migrate();
        
        String[] tables = {"users", "movies", "ratings", "jwt_tokens", "user_sessions"};
        
        for (String table : tables) {
            assertTrue(hasUuidPrimaryKey(table), 
                    String.format("Table %s should have UUID primary key", table));
        }
    }

    // Helper methods for database verification
    private boolean tableExists(String tableName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)")) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        }
    }

    private List<String> getTableColumns(String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT column_name FROM information_schema.columns WHERE table_name = ?")) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                columns.add(rs.getString("column_name"));
            }
        }
        return columns;
    }

    private boolean indexExists(String indexName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT EXISTS (SELECT FROM pg_indexes WHERE indexname = ?)")) {
            stmt.setString(1, indexName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        }
    }

    private boolean hasUniqueConstraint(String tableName, String columnName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM information_schema.table_constraints tc " +
                "JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name " +
                "WHERE tc.table_name = ? AND tc.constraint_type = 'UNIQUE' AND ccu.column_name = ?)")) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        }
    }

    private boolean hasCompositeUniqueConstraint(String tableName, List<String> columnNames) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) as constraint_count FROM (" +
                "    SELECT tc.constraint_name, array_agg(ccu.column_name ORDER BY ccu.column_name) as columns " +
                "    FROM information_schema.table_constraints tc " +
                "    JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name " +
                "    WHERE tc.table_name = ? AND tc.constraint_type = 'UNIQUE' " +
                "    GROUP BY tc.constraint_name " +
                "    HAVING COUNT(*) = ? " +
                ") as constraints " +
                "WHERE array_to_string(constraints.columns, ',') = ?")) {
            stmt.setString(1, tableName);
            stmt.setInt(2, columnNames.size());
            stmt.setString(3, String.join(",", columnNames.stream().sorted().toArray(String[]::new)));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt("constraint_count") > 0;
        }
    }

    private boolean hasForeignKeyConstraint(String tableName, String columnName, String referencedTable, String referencedColumn) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT EXISTS (" +
                "    SELECT 1 FROM information_schema.table_constraints tc " +
                "    JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                "    JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name " +
                "    WHERE tc.constraint_type = 'FOREIGN KEY' " +
                "    AND tc.table_name = ? " +
                "    AND kcu.column_name = ? " +
                "    AND ccu.table_name = ? " +
                "    AND ccu.column_name = ?" +
                ")")) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            stmt.setString(3, referencedTable);
            stmt.setString(4, referencedColumn);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        }
    }

    private boolean hasCheckConstraint(String tableName, String columnName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT EXISTS (" +
                "    SELECT 1 FROM information_schema.check_constraints cc " +
                "    JOIN information_schema.constraint_column_usage ccu ON cc.constraint_name = ccu.constraint_name " +
                "    WHERE ccu.table_name = ? AND ccu.column_name = ?" +
                ")")) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        }
    }

    private boolean hasUuidPrimaryKey(String tableName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT EXISTS (" +
                "    SELECT 1 FROM information_schema.columns c " +
                "    JOIN information_schema.table_constraints tc ON c.table_name = tc.table_name " +
                "    JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                "    WHERE c.table_name = ? " +
                "    AND c.column_name = kcu.column_name " +
                "    AND tc.constraint_type = 'PRIMARY KEY' " +
                "    AND c.data_type = 'uuid'" +
                ")")) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        }
    }
}
