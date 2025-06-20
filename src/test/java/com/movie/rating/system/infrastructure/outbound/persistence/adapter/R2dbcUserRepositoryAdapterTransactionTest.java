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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Advanced transaction tests for R2dbcUserRepositoryAdapter.
 * Tests complex transaction scenarios, rollback behavior, and edge cases.
 */
@Testcontainers
@DisplayName("R2DBC User Repository Adapter Advanced Transaction Tests")
class R2dbcUserRepositoryAdapterTransactionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("user_transaction_test")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(Duration.ofMinutes(2));

    private R2dbcUserRepositoryAdapter adapter;
    private R2dbcUserRepository repository;
    private R2dbcEntityTemplate entityTemplate;
    private TransactionalOperator transactionalOperator;

    @BeforeEach
    void setUp() {
        runMigrations();

        ConnectionFactory connectionFactory = createConnectionFactory();
        entityTemplate = new R2dbcEntityTemplate(connectionFactory);

        ReactiveTransactionManager transactionManager = new R2dbcTransactionManager(connectionFactory);
        transactionalOperator = TransactionalOperator.create(transactionManager);

        RepositoryFactorySupport factory = new R2dbcRepositoryFactory(entityTemplate);
        repository = factory.getRepository(R2dbcUserRepository.class);

        UserEntityMapper mapper = new UserEntityMapper();
        adapter = new R2dbcUserRepositoryAdapter(repository, mapper);
    }

    @AfterEach
    void tearDown() {
        if (entityTemplate != null) {
            entityTemplate.delete(UserEntity.class)
                    .all()
                    .block(Duration.ofSeconds(10));
        }
    }

    @Test
    @DisplayName("Should rollback complex multi-operation transaction on failure")
    void shouldRollbackComplexTransactionOnFailure() {
        // Given
        User user1 = createTestUser("complex1", "complex1@example.com");
        User user2 = createTestUser("complex2", "complex2@example.com");
        User user3 = createTestUser("complex3", "complex3@example.com");

        // When - Complex transaction that will fail at the end
        Mono<String> complexTransaction = transactionalOperator.transactional(
                adapter.save(user1)
                        .then(adapter.save(user2))
                        .then(adapter.save(user3))
                        .then(adapter.countActiveUsers())
                        .doOnNext(count -> assertThat(count).isEqualTo(3L))
                        .then(Mono.error(new RuntimeException("Intentional failure for rollback test")))
                        .then(Mono.just("Should not reach here"))
        );

        // Then - Transaction should fail and rollback
        StepVerifier.create(complexTransaction)
                .expectError(RuntimeException.class)
                .verify();

        // Verify all operations were rolled back
        StepVerifier.create(adapter.countActiveUsers())
                .expectNext(0L)
                .verifyComplete();

        StepVerifier.create(adapter.findByUsername("complex1"))
                .verifyComplete();

        StepVerifier.create(adapter.findByUsername("complex2"))
                .verifyComplete();

        StepVerifier.create(adapter.findByUsername("complex3"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle batch operations within transaction")
    void shouldHandleBatchOperationsWithinTransaction() {
        // Given
        List<User> users = List.of(
                createTestUser("batch1", "batch1@example.com"),
                createTestUser("batch2", "batch2@example.com"),
                createTestUser("batch3", "batch3@example.com"),
                createTestUser("batch4", "batch4@example.com"),
                createTestUser("batch5", "batch5@example.com")
        );

        // When - Batch save within transaction
        Mono<List<User>> batchTransaction = transactionalOperator.transactional(
                Flux.fromIterable(users)
                        .flatMap(adapter::save)
                        .collectList()
        );

        // Then
        StepVerifier.create(batchTransaction)
                .assertNext(savedUsers -> {
                    assertThat(savedUsers).hasSize(5);
                    assertThat(savedUsers.stream().allMatch(user -> user.getId() != null)).isTrue();
                })
                .verifyComplete();

        // Verify all users are saved
        StepVerifier.create(adapter.countActiveUsers())
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should rollback batch operations on single failure")
    void shouldRollbackBatchOperationsOnSingleFailure() {
        // Given
        List<User> users = List.of(
                createTestUser("batchfail1", "batchfail1@example.com"),
                createTestUser("batchfail2", "batchfail2@example.com"),
                createTestUser("batchfail1", "batchfail3@example.com"), // Duplicate username - will fail
                createTestUser("batchfail4", "batchfail4@example.com")
        );

        // When - Batch save that will fail due to constraint violation
        Mono<List<User>> batchTransaction = transactionalOperator.transactional(
                Flux.fromIterable(users)
                        .flatMap(adapter::save)
                        .collectList()
        );

        // Then - Should fail and rollback all
        StepVerifier.create(batchTransaction)
                .expectError()
                .verify();

        // Verify no users were saved
        StepVerifier.create(adapter.countActiveUsers())
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle mixed operations in transaction")
    void shouldHandleMixedOperationsInTransaction() {
        // Given - Setup initial user
        User initialUser = createTestUser("mixed_initial", "mixed@example.com");
        User savedInitialUser = adapter.save(initialUser).block(Duration.ofSeconds(10));
        assertThat(savedInitialUser).isNotNull();

        // Prepare new user and update data
        User newUser = createTestUser("mixed_new", "mixednew@example.com");
        User updatedUser = savedInitialUser.toBuilder()
                .firstName("Updated")
                .lastName("Mixed")
                .build();

        // When - Mixed operations in transaction
        Mono<String> mixedTransaction = transactionalOperator.transactional(
                adapter.save(newUser)
                        .then(adapter.update(updatedUser))
                        .then(adapter.countActiveUsers())
                        .map(count -> "Total users: " + count)
        );

        // Then
        StepVerifier.create(mixedTransaction)
                .expectNext("Total users: 2")
                .verifyComplete();

        // Verify operations
        StepVerifier.create(adapter.findByUsername("mixed_new"))
                .assertNext(user -> assertThat(user.getUsername()).isEqualTo("mixed_new"))
                .verifyComplete();

        StepVerifier.create(adapter.findById(savedInitialUser.getId()))
                .assertNext(user -> {
                    assertThat(user.getFirstName()).isEqualTo("Updated");
                    assertThat(user.getLastName()).isEqualTo("Mixed");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle transaction with conditional logic")
    void shouldHandleTransactionWithConditionalLogic() {
        // Given
        User user = createTestUser("conditional", "conditional@example.com");

        // When - Transaction with conditional operations
        Mono<User> conditionalTransaction = transactionalOperator.transactional(
                adapter.save(user)
                        .flatMap(savedUser -> {
                            // Conditional logic based on saved user
                            if (savedUser.getUsername().contains("conditional")) {
                                return adapter.update(savedUser.toBuilder()
                                        .firstName("Conditional")
                                        .lastName("User")
                                        .build());
                            } else {
                                return Mono.just(savedUser);
                            }
                        })
        );

        // Then
        StepVerifier.create(conditionalTransaction)
                .assertNext(result -> {
                    assertThat(result.getUsername()).isEqualTo("conditional");
                    assertThat(result.getFirstName()).isEqualTo("Conditional");
                    assertThat(result.getLastName()).isEqualTo("User");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle transaction with error recovery")
    void shouldHandleTransactionWithErrorRecovery() {
        // Given
        User user1 = createTestUser("recovery1", "recovery1@example.com");
        User user2 = createTestUser("recovery2", "recovery2@example.com");

        // When - Transaction with error handling and recovery
        Mono<String> recoveryTransaction = transactionalOperator.transactional(
                adapter.save(user1)
                        .then(adapter.existsByUsername("nonexistent"))
                        .flatMap(exists -> {
                            if (!exists) {
                                return adapter.save(user2).then(Mono.just("Recovery successful"));
                            } else {
                                return Mono.error(new RuntimeException("Unexpected condition"));
                            }
                        })
        );

        // Then
        StepVerifier.create(recoveryTransaction)
                .expectNext("Recovery successful")
                .verifyComplete();

        // Verify both users were saved
        StepVerifier.create(adapter.countActiveUsers())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should maintain data consistency across concurrent transactions")
    void shouldMaintainDataConsistencyAcrossConcurrentTransactions() {
        // Given
        int numberOfConcurrentUsers = 10;

        // When - Create concurrent transactions that each save a user and check total count
        Flux<Long> concurrentTransactions = Flux.range(1, numberOfConcurrentUsers)
                .flatMap(i -> {
                    User user = createTestUser("concurrent" + i, "concurrent" + i + "@example.com");
                    return transactionalOperator.transactional(
                            adapter.save(user)
                                    .then(adapter.countActiveUsers())
                    );
                }, 3); // Concurrency level

        // Then
        StepVerifier.create(concurrentTransactions.collectList())
                .assertNext(counts -> {
                    assertThat(counts).hasSize(numberOfConcurrentUsers);
                    // Each transaction should see at least its own user
                    assertThat(counts).allMatch(count -> count >= 1L);
                })
                .verifyComplete();

        // Final count should be exactly the number of users created
        StepVerifier.create(adapter.countActiveUsers())
                .expectNext((long) numberOfConcurrentUsers)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle large transaction with many operations")
    void shouldHandleLargeTransactionWithManyOperations() {
        // Given
        int numberOfOperations = 50;

        // When - Large transaction with many save operations
        Mono<Long> largeTransaction = transactionalOperator.transactional(
                Flux.range(1, numberOfOperations)
                        .flatMap(i -> {
                            User user = createTestUser("large" + i, "large" + i + "@example.com");
                            return adapter.save(user);
                        })
                        .then(adapter.countActiveUsers())
        );

        // Then
        StepVerifier.create(largeTransaction)
                .expectNext((long) numberOfOperations)
                .verifyComplete();

        // Verify final state
        StepVerifier.create(adapter.countActiveUsers())
                .expectNext((long) numberOfOperations)
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
