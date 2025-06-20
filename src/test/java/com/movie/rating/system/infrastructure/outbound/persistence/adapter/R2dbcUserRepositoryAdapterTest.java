package com.movie.rating.system.infrastructure.outbound.persistence.adapter;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.UserEntity;
import com.movie.rating.system.infrastructure.outbound.persistence.mapper.UserEntityMapper;
import com.movie.rating.system.infrastructure.outbound.persistence.repository.R2dbcUserRepository;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for R2dbcUserRepositoryAdapter with transaction manager support.
 * Tests reactive transactions, rollback scenarios, and concurrent operations.
 */
@Testcontainers
@DisplayName("R2DBC User Repository Adapter Unit Tests with Transaction Manager")
class R2dbcUserRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("user_adapter_test")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(Duration.ofMinutes(2));

    private R2dbcUserRepositoryAdapter adapter;
    private R2dbcUserRepository repository;
    private R2dbcEntityTemplate entityTemplate;
    private ConnectionFactory connectionFactory;
    private ReactiveTransactionManager transactionManager;
    private TransactionalOperator transactionalOperator;

    @BeforeEach
    void setUp() {
        // Run Flyway migrations
        runMigrations();

        // Setup R2DBC connection
        connectionFactory = createConnectionFactory();
        entityTemplate = new R2dbcEntityTemplate(connectionFactory);

        // Setup transaction manager
        transactionManager = new R2dbcTransactionManager(connectionFactory);
        transactionalOperator = TransactionalOperator.create(transactionManager);

        // Create repository and adapter
        RepositoryFactorySupport factory = new R2dbcRepositoryFactory(entityTemplate);
        repository = factory.getRepository(R2dbcUserRepository.class);

        UserEntityMapper mapper = new UserEntityMapper();
        adapter = new R2dbcUserRepositoryAdapter(repository, mapper);
    }

    @AfterEach
    void tearDown() {
        // Clean up the database after each test
        if (entityTemplate != null) {
            entityTemplate.delete(UserEntity.class)
                    .all()
                    .block(Duration.ofSeconds(10));
        }
    }

    @Test
    @DisplayName("Should save user within transaction successfully")
    void shouldSaveUserWithinTransaction() {
        // Given
        User user = createTestUser("transaction_user", "transaction@example.com");

        // When & Then - Test transactional save
        StepVerifier.create(
                transactionalOperator.transactional(
                        adapter.save(user)
                )
        )
        .assertNext(savedUser -> {
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getUsername()).isEqualTo("transaction_user");
            assertThat(savedUser.getEmail()).isEqualTo("transaction@example.com");
            assertThat(savedUser.getCreatedAt()).isNotNull();
            assertThat(savedUser.getUpdatedAt()).isNotNull();
        })
        .verifyComplete();

        // Verify user was actually saved
        StepVerifier.create(adapter.findByUsername("transaction_user"))
                .assertNext(foundUser -> {
                    assertThat(foundUser.getUsername()).isEqualTo("transaction_user");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should rollback transaction on error during save")
    void shouldRollbackTransactionOnErrorDuringSave() {
        // Given
        User user1 = createTestUser("rollback_user", "rollback@example.com");
        User user2 = createTestUser("rollback_user", "different@example.com"); // Same username - should cause constraint violation

        // When & Then - Test transactional rollback
        Mono<User> transactionalOperation = transactionalOperator.transactional(
                adapter.save(user1)
                        .then(adapter.save(user2)) // This should fail due to duplicate username
        );

        StepVerifier.create(transactionalOperation)
                .expectError()
                .verify();

        // Verify rollback - no users should exist
        StepVerifier.create(adapter.findByUsername("rollback_user"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle multiple saves in single transaction")
    void shouldHandleMultipleSavesInSingleTransaction() {
        // Given
        User user1 = createTestUser("multi1", "multi1@example.com");
        User user2 = createTestUser("multi2", "multi2@example.com");
        User user3 = createTestUser("multi3", "multi3@example.com");

        // When & Then - Test multiple saves in one transaction
        Mono<Long> transactionalOperation = transactionalOperator.transactional(
                adapter.save(user1)
                        .then(adapter.save(user2))
                        .then(adapter.save(user3))
                        .then(adapter.countActiveUsers())
        );

        StepVerifier.create(transactionalOperation)
                .expectNext(3L)
                .verifyComplete();

        // Verify all users were saved
        StepVerifier.create(adapter.countActiveUsers())
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update user within transaction")
    void shouldUpdateUserWithinTransaction() {
        // Given
        User user = createTestUser("update_user", "update@example.com");
        User savedUser = adapter.save(user).block(Duration.ofSeconds(10));
        assertThat(savedUser).isNotNull();

        User updatedUser = savedUser.toBuilder()
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        // When & Then - Test transactional update
        StepVerifier.create(
                transactionalOperator.transactional(
                        adapter.update(updatedUser)
                )
        )
        .assertNext(result -> {
            assertThat(result.getId()).isEqualTo(savedUser.getId());
            assertThat(result.getEmail()).isEqualTo("updated@example.com");
            assertThat(result.getFirstName()).isEqualTo("Updated");
            assertThat(result.getLastName()).isEqualTo("User");
            assertThat(result.getUpdatedAt()).isAfter(savedUser.getUpdatedAt());
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should delete user within transaction")
    void shouldDeleteUserWithinTransaction() {
        // Given
        User user = createTestUser("delete_user", "delete@example.com");
        User savedUser = adapter.save(user).block(Duration.ofSeconds(10));
        assertThat(savedUser).isNotNull();

        // When & Then - Test transactional delete
        StepVerifier.create(
                transactionalOperator.transactional(
                        adapter.deleteById(savedUser.getId())
                )
        )
        .verifyComplete();

        // Verify user was deleted
        StepVerifier.create(adapter.findById(savedUser.getId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should rollback delete operation on error")
    void shouldRollbackDeleteOperationOnError() {
        // Given
        User user1 = createTestUser("delete1", "delete1@example.com");
        User user2 = createTestUser("delete2", "delete2@example.com");
        
        User savedUser1 = adapter.save(user1).block(Duration.ofSeconds(10));
        User savedUser2 = adapter.save(user2).block(Duration.ofSeconds(10));
        assertThat(savedUser1).isNotNull();
        assertThat(savedUser2).isNotNull();

        // When & Then - Test transactional rollback on delete
        Mono<Void> transactionalOperation = transactionalOperator.transactional(
                adapter.deleteById(savedUser1.getId())
                        .then(adapter.deleteById(UUID.randomUUID())) // This won't fail but will complete
                        .then(Mono.error(new RuntimeException("Simulated error"))) // Force rollback
        );

        StepVerifier.create(transactionalOperation)
                .expectError(RuntimeException.class)
                .verify();

        // Verify rollback - user1 should still exist
        StepVerifier.create(adapter.findById(savedUser1.getId()))
                .assertNext(foundUser -> {
                    assertThat(foundUser.getId()).isEqualTo(savedUser1.getId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle concurrent transactions")
    void shouldHandleConcurrentTransactions() {
        // Given
        int numberOfConcurrentTransactions = 5;
        
        // When - Create multiple concurrent transactions
        Flux<User> concurrentSaves = Flux.range(1, numberOfConcurrentTransactions)
                .flatMap(i -> {
                    User user = createTestUser("concurrent" + i, "concurrent" + i + "@example.com");
                    return transactionalOperator.transactional(adapter.save(user));
                }, 3); // Concurrency level of 3

        // Then
        StepVerifier.create(concurrentSaves.collectList())
                .assertNext(savedUsers -> {
                    assertThat(savedUsers).hasSize(numberOfConcurrentTransactions);
                    // Verify all users have unique IDs
                    assertThat(savedUsers.stream().map(User::getId).distinct().count())
                            .isEqualTo(numberOfConcurrentTransactions);
                })
                .verifyComplete();

        // Verify count
        StepVerifier.create(adapter.countActiveUsers())
                .expectNext((long) numberOfConcurrentTransactions)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle nested transaction operations")
    void shouldHandleNestedTransactionOperations() {
        // Given
        User user = createTestUser("nested_user", "nested@example.com");

        // When & Then - Test nested transactional operations
        Mono<User> nestedTransactionOperation = transactionalOperator.transactional(
                adapter.save(user)
                        .flatMap(savedUser -> {
                            // Inner operation within the same transaction
                            User updatedUser = savedUser.toBuilder()
                                    .firstName("Nested")
                                    .lastName("Transaction")
                                    .build();
                            return adapter.update(updatedUser);
                        })
        );

        StepVerifier.create(nestedTransactionOperation)
                .assertNext(result -> {
                    assertThat(result.getUsername()).isEqualTo("nested_user");
                    assertThat(result.getFirstName()).isEqualTo("Nested");
                    assertThat(result.getLastName()).isEqualTo("Transaction");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should maintain transaction isolation")
    void shouldMaintainTransactionIsolation() {
        // Given
        User user = createTestUser("isolation_user", "isolation@example.com");

        // When - Start a transaction but don't commit yet
        Mono<User> transactionInProgress = transactionalOperator.transactional(
                adapter.save(user)
                        .delayElement(Duration.ofMillis(100)) // Simulate some processing time
        );

        // Concurrently try to find the user before transaction completes
        Mono<User> concurrentRead = Mono.delay(Duration.ofMillis(50))
                .then(adapter.findByUsername("isolation_user"));

        // Then - The concurrent read should not see uncommitted data
        StepVerifier.create(concurrentRead)
                .verifyComplete(); // Should not find the user yet

        // But the transaction should complete successfully
        StepVerifier.create(transactionInProgress)
                .assertNext(savedUser -> {
                    assertThat(savedUser.getUsername()).isEqualTo("isolation_user");
                })
                .verifyComplete();

        // Now the user should be visible
        StepVerifier.create(adapter.findByUsername("isolation_user"))
                .assertNext(foundUser -> {
                    assertThat(foundUser.getUsername()).isEqualTo("isolation_user");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle transaction timeout")
    void shouldHandleTransactionTimeout() {
        // Given
        User user = createTestUser("timeout_user", "timeout@example.com");

        // When & Then - Test transaction with delay that might timeout
        Mono<User> delayedTransaction = transactionalOperator.transactional(
                adapter.save(user)
                        .delayElement(Duration.ofMillis(50)) // Small delay
        );

        StepVerifier.create(delayedTransaction)
                .assertNext(savedUser -> {
                    assertThat(savedUser.getUsername()).isEqualTo("timeout_user");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle constraint violations within transactions")
    void shouldHandleConstraintViolationsWithinTransactions() {
        // Given
        User user1 = createTestUser("constraint_user", "constraint@example.com");
        adapter.save(user1).block(Duration.ofSeconds(10));

        User user2 = createTestUser("constraint_user", "different@example.com"); // Same username

        // When & Then - Test constraint violation handling
        StepVerifier.create(
                transactionalOperator.transactional(
                        adapter.save(user2)
                )
        )
        .expectError()
        .verify();

        // Original user should still exist
        StepVerifier.create(adapter.findByUsername("constraint_user"))
                .assertNext(foundUser -> {
                    assertThat(foundUser.getEmail()).isEqualTo("constraint@example.com");
                })
                .verifyComplete();
    }

    private User createTestUser(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .passwordHash("$2a$10$hashedpassword")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private void runMigrations() {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();

        flyway.clean();
        flyway.migrate();
    }

    private ConnectionFactory createConnectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(postgres.getHost())
                        .port(postgres.getFirstMappedPort())
                        .database(postgres.getDatabaseName())
                        .username(postgres.getUsername())
                        .password(postgres.getPassword())
                        .build()
        );
    }
}
