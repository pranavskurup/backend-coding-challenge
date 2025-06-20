package com.movie.rating.system.infrastructure.outbound.persistence.mapper;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserEntityMapper.
 * Tests the conversion between domain User and persistence UserEntity objects.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserEntityMapper Unit Tests")
class UserEntityMapperTest {

    private UserEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserEntityMapper();
    }

    @Test
    @DisplayName("Should convert domain User to UserEntity successfully")
    void shouldConvertDomainUserToEntity() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant deactivatedAt = now.plusSeconds(3600);

        User domainUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(deactivatedAt)
                .build();

        // When
        UserEntity entity = mapper.toEntity(domainUser);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(userId);
        assertThat(entity.getUsername()).isEqualTo("testuser");
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(entity.getFirstName()).isEqualTo("John");
        assertThat(entity.getLastName()).isEqualTo("Doe");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeactivatedAt()).isEqualTo(deactivatedAt);
    }

    @Test
    @DisplayName("Should convert UserEntity to domain User successfully")
    void shouldConvertEntityToDomainUser() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant deactivatedAt = now.plusSeconds(3600);

        UserEntity entity = UserEntity.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(deactivatedAt)
                .build();

        // When
        User domainUser = mapper.toDomain(entity);

        // Then
        assertThat(domainUser).isNotNull();
        assertThat(domainUser.getId()).isEqualTo(userId);
        assertThat(domainUser.getUsername()).isEqualTo("testuser");
        assertThat(domainUser.getEmail()).isEqualTo("test@example.com");
        assertThat(domainUser.getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(domainUser.getFirstName()).isEqualTo("John");
        assertThat(domainUser.getLastName()).isEqualTo("Doe");
        assertThat(domainUser.isActive()).isTrue();
        assertThat(domainUser.getCreatedAt()).isEqualTo(now);
        assertThat(domainUser.getUpdatedAt()).isEqualTo(now);
        assertThat(domainUser.getDeactivatedAt()).isEqualTo(deactivatedAt);
    }

    @Test
    @DisplayName("Should return null when converting null domain User to entity")
    void shouldReturnNullWhenConvertingNullDomainUserToEntity() {
        // When
        UserEntity entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when converting null entity to domain User")
    void shouldReturnNullWhenConvertingNullEntityToDomainUser() {
        // When
        User domainUser = mapper.toDomain(null);

        // Then
        assertThat(domainUser).isNull();
    }

    @Test
    @DisplayName("Should handle domain User with null optional fields")
    void shouldHandleDomainUserWithNullOptionalFields() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        User domainUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName(null) // Optional field
                .lastName(null)  // Optional field
                .isActive(false)
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(null) // Optional field
                .build();

        // When
        UserEntity entity = mapper.toEntity(domainUser);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(userId);
        assertThat(entity.getUsername()).isEqualTo("testuser");
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(entity.getFirstName()).isNull();
        assertThat(entity.getLastName()).isNull();
        assertThat(entity.getIsActive()).isFalse();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeactivatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle UserEntity with null optional fields")
    void shouldHandleEntityWithNullOptionalFields() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        UserEntity entity = UserEntity.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName(null) // Optional field
                .lastName(null)  // Optional field
                .isActive(false)
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(null) // Optional field
                .build();

        // When
        User domainUser = mapper.toDomain(entity);

        // Then
        assertThat(domainUser).isNotNull();
        assertThat(domainUser.getId()).isEqualTo(userId);
        assertThat(domainUser.getUsername()).isEqualTo("testuser");
        assertThat(domainUser.getEmail()).isEqualTo("test@example.com");
        assertThat(domainUser.getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(domainUser.getFirstName()).isNull();
        assertThat(domainUser.getLastName()).isNull();
        assertThat(domainUser.isActive()).isFalse();
        assertThat(domainUser.getCreatedAt()).isEqualTo(now);
        assertThat(domainUser.getUpdatedAt()).isEqualTo(now);
        assertThat(domainUser.getDeactivatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle UserEntity with null isActive field as false")
    void shouldHandleEntityWithNullIsActiveFieldAsFalse() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        UserEntity entity = UserEntity.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(null) // Explicitly null
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(null)
                .build();

        // When
        User domainUser = mapper.toDomain(entity);

        // Then
        assertThat(domainUser).isNotNull();
        assertThat(domainUser.isActive()).isFalse(); // Should be false when null
    }

    @Test
    @DisplayName("Should handle UserEntity with Boolean.FALSE isActive field")
    void shouldHandleEntityWithBooleanFalseIsActiveField() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        UserEntity entity = UserEntity.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(Boolean.FALSE)
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(null)
                .build();

        // When
        User domainUser = mapper.toDomain(entity);

        // Then
        assertThat(domainUser).isNotNull();
        assertThat(domainUser.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle UserEntity with Boolean.TRUE isActive field")
    void shouldHandleEntityWithBooleanTrueIsActiveField() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        UserEntity entity = UserEntity.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(Boolean.TRUE)
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(null)
                .build();

        // When
        User domainUser = mapper.toDomain(entity);

        // Then
        assertThat(domainUser).isNotNull();
        assertThat(domainUser.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should maintain data integrity in round-trip conversion")
    void shouldMaintainDataIntegrityInRoundTripConversion() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant deactivatedAt = now.plusSeconds(3600);

        User originalUser = User.builder()
                .id(userId)
                .username("roundtripuser")
                .email("roundtrip@example.com")
                .passwordHash("$2a$10$hashedpasswordfortest")
                .firstName("Round")
                .lastName("Trip")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(deactivatedAt)
                .build();

        // When - Convert to entity and back to domain
        UserEntity entity = mapper.toEntity(originalUser);
        User roundTripUser = mapper.toDomain(entity);

        // Then
        assertThat(roundTripUser).isNotNull();
        assertThat(roundTripUser.getId()).isEqualTo(originalUser.getId());
        assertThat(roundTripUser.getUsername()).isEqualTo(originalUser.getUsername());
        assertThat(roundTripUser.getEmail()).isEqualTo(originalUser.getEmail());
        assertThat(roundTripUser.getPasswordHash()).isEqualTo(originalUser.getPasswordHash());
        assertThat(roundTripUser.getFirstName()).isEqualTo(originalUser.getFirstName());
        assertThat(roundTripUser.getLastName()).isEqualTo(originalUser.getLastName());
        assertThat(roundTripUser.isActive()).isEqualTo(originalUser.isActive());
        assertThat(roundTripUser.getCreatedAt()).isEqualTo(originalUser.getCreatedAt());
        assertThat(roundTripUser.getUpdatedAt()).isEqualTo(originalUser.getUpdatedAt());
        assertThat(roundTripUser.getDeactivatedAt()).isEqualTo(originalUser.getDeactivatedAt());
    }

    @Test
    @DisplayName("Should handle empty strings in conversion")
    void shouldHandleEmptyStringsInConversion() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        User domainUser = User.builder()
                .id(userId)
                .username("emptytest")
                .email("empty@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("") // Empty string
                .lastName("") // Empty string
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(null)
                .build();

        // When
        UserEntity entity = mapper.toEntity(domainUser);
        User convertedBack = mapper.toDomain(entity);

        // Then
        assertThat(entity.getFirstName()).isEmpty();
        assertThat(entity.getLastName()).isEmpty();
        assertThat(convertedBack.getFirstName()).isEmpty();
        assertThat(convertedBack.getLastName()).isEmpty();
    }

    @Test
    @DisplayName("Should handle special characters in user data")
    void shouldHandleSpecialCharactersInUserData() {
        // Given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        User domainUser = User.builder()
                .id(userId)
                .username("special_user-123")
                .email("special+test@example-domain.com")
                .passwordHash("$2a$10$special.chars/in\\hash")
                .firstName("José")
                .lastName("O'Brien-Smith")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .deactivatedAt(null)
                .build();

        // When
        UserEntity entity = mapper.toEntity(domainUser);
        User convertedBack = mapper.toDomain(entity);

        // Then
        assertThat(entity.getUsername()).isEqualTo("special_user-123");
        assertThat(entity.getEmail()).isEqualTo("special+test@example-domain.com");
        assertThat(entity.getPasswordHash()).isEqualTo("$2a$10$special.chars/in\\hash");
        assertThat(entity.getFirstName()).isEqualTo("José");
        assertThat(entity.getLastName()).isEqualTo("O'Brien-Smith");
        
        assertThat(convertedBack.getUsername()).isEqualTo("special_user-123");
        assertThat(convertedBack.getEmail()).isEqualTo("special+test@example-domain.com");
        assertThat(convertedBack.getPasswordHash()).isEqualTo("$2a$10$special.chars/in\\hash");
        assertThat(convertedBack.getFirstName()).isEqualTo("José");
        assertThat(convertedBack.getLastName()).isEqualTo("O'Brien-Smith");
    }
}
