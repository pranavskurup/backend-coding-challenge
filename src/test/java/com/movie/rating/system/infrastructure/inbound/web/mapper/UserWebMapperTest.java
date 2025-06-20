package com.movie.rating.system.infrastructure.inbound.web.mapper;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.port.inbound.RegisterUserUseCase.RegisterUserCommand;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.RegisterUserRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.UserResponseDto;
import com.movie.rating.system.infrastructure.inbound.web.mapper.UserWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserWebMapper Tests")
class UserWebMapperTest {

    private UserWebMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserWebMapper();
    }

    @Nested
    @DisplayName("toCommand() Tests")
    class ToCommandTests {

        @Test
        @DisplayName("Should convert RegisterUserRequestDto to RegisterUserCommand with all fields")
        void shouldConvertRequestDtoToCommandWithAllFields() {
            // Given
            RegisterUserRequestDto requestDto = new RegisterUserRequestDto(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "John",
                    "Doe"
            );

            // When
            RegisterUserCommand result = mapper.toCommand(requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testuser");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.password()).isEqualTo("password123");
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should convert RegisterUserRequestDto with special characters")
        void shouldConvertRequestDtoWithSpecialCharacters() {
            // Given
            RegisterUserRequestDto requestDto = new RegisterUserRequestDto(
                    "test-user_123",
                    "test.email+tag@example-domain.com",
                    "P@ssw0rd!Special",
                    "José",
                    "O'Connor"
            );

            // When
            RegisterUserCommand result = mapper.toCommand(requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("test-user_123");
            assertThat(result.email()).isEqualTo("test.email+tag@example-domain.com");
            assertThat(result.password()).isEqualTo("P@ssw0rd!Special");
            assertThat(result.firstName()).isEqualTo("José");
            assertThat(result.lastName()).isEqualTo("O'Connor");
        }

        @Test
        @DisplayName("Should return null when input is null")
        void shouldReturnNullWhenInputIsNull() {
            // When
            RegisterUserCommand result = mapper.toCommand(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle RegisterUserRequestDto with whitespace fields")
        void shouldHandleRequestDtoWithWhitespaceFields() {
            // Given
            RegisterUserRequestDto requestDto = new RegisterUserRequestDto(
                    "  testuser  ",
                    "  test@example.com  ",
                    "  password123  ",
                    "  John  ",
                    "  Doe  "
            );

            // When
            RegisterUserCommand result = mapper.toCommand(requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("  testuser  ");
            assertThat(result.email()).isEqualTo("  test@example.com  ");
            assertThat(result.password()).isEqualTo("  password123  ");
            assertThat(result.firstName()).isEqualTo("  John  ");
            assertThat(result.lastName()).isEqualTo("  Doe  ");
        }
    }

    @Nested
    @DisplayName("toResponseDto() Tests")
    class ToResponseDtoTests {

        @Test
        @DisplayName("Should convert User to UserResponseDto with all fields")
        void shouldConvertUserToResponseDtoWithAllFields() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now();

            User user = User.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash("hashedPassword")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // When
            UserResponseDto result = mapper.toResponseDto(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.username()).isEqualTo("testuser");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isEqualTo("Doe");
            assertThat(result.fullName()).isEqualTo("John Doe");
            assertThat(result.isActive()).isTrue();
            assertThat(result.createdAt()).isEqualTo(createdAt);
            assertThat(result.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("Should convert User to UserResponseDto with null names")
        void shouldConvertUserToResponseDtoWithNullNames() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now();

            User user = User.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash("hashedPassword")
                    .firstName(null)
                    .lastName(null)
                    .isActive(true)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // When
            UserResponseDto result = mapper.toResponseDto(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.username()).isEqualTo("testuser");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.firstName()).isNull();
            assertThat(result.lastName()).isNull();
            assertThat(result.fullName()).isEqualTo("testuser"); // Falls back to username
            assertThat(result.isActive()).isTrue();
            assertThat(result.createdAt()).isEqualTo(createdAt);
            assertThat(result.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("Should convert User to UserResponseDto with only first name")
        void shouldConvertUserToResponseDtoWithOnlyFirstName() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now();

            User user = User.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash("hashedPassword")
                    .firstName("John")
                    .lastName(null)
                    .isActive(true)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // When
            UserResponseDto result = mapper.toResponseDto(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isNull();
            assertThat(result.fullName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should convert User to UserResponseDto with only last name")
        void shouldConvertUserToResponseDtoWithOnlyLastName() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now();

            User user = User.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash("hashedPassword")
                    .firstName(null)
                    .lastName("Doe")
                    .isActive(true)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // When
            UserResponseDto result = mapper.toResponseDto(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.firstName()).isNull();
            assertThat(result.lastName()).isEqualTo("Doe");
            assertThat(result.fullName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should convert inactive User to UserResponseDto")
        void shouldConvertInactiveUserToResponseDto() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(7200);
            Instant updatedAt = Instant.now().minusSeconds(3600);
            Instant deactivatedAt = Instant.now().minusSeconds(1800);

            User user = User.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash("hashedPassword")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(false)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .deactivatedAt(deactivatedAt)
                    .build();

            // When
            UserResponseDto result = mapper.toResponseDto(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isActive()).isFalse();
            assertThat(result.createdAt()).isEqualTo(createdAt);
            assertThat(result.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("Should convert User with special characters to UserResponseDto")
        void shouldConvertUserWithSpecialCharactersToResponseDto() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now();

            User user = User.builder()
                    .id(userId)
                    .username("test-user_123")
                    .email("test.email+tag@example-domain.com")
                    .passwordHash("hashedPassword")
                    .firstName("José")
                    .lastName("O'Connor")
                    .isActive(true)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // When
            UserResponseDto result = mapper.toResponseDto(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("test-user_123");
            assertThat(result.email()).isEqualTo("test.email+tag@example-domain.com");
            assertThat(result.firstName()).isEqualTo("José");
            assertThat(result.lastName()).isEqualTo("O'Connor");
            assertThat(result.fullName()).isEqualTo("José O'Connor");
        }

        @Test
        @DisplayName("Should return null when input is null")
        void shouldReturnNullWhenInputIsNull() {
            // When
            UserResponseDto result = mapper.toResponseDto(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle User with empty string names")
        void shouldHandleUserWithEmptyStringNames() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now();

            User user = User.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash("hashedPassword")
                    .firstName("")
                    .lastName("")
                    .isActive(true)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // When
            UserResponseDto result = mapper.toResponseDto(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.firstName()).isEmpty();
            assertThat(result.lastName()).isEmpty();
            assertThat(result.fullName()).isEmpty(); // Empty strings concatenate to empty string
        }

        @Test
        @DisplayName("Should convert User with long names to UserResponseDto")
        void shouldConvertUserWithLongNamesToResponseDto() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now();
            String longFirstName = "A".repeat(100);
            String longLastName = "B".repeat(100);

            User user = User.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash("hashedPassword")
                    .firstName(longFirstName)
                    .lastName(longLastName)
                    .isActive(true)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // When
            UserResponseDto result = mapper.toResponseDto(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.firstName()).isEqualTo(longFirstName);
            assertThat(result.lastName()).isEqualTo(longLastName);
            assertThat(result.fullName()).isEqualTo(longFirstName + " " + longLastName);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should maintain data consistency through mapping cycle")
        void shouldMaintainDataConsistencyThroughMappingCycle() {
            // Given
            RegisterUserRequestDto requestDto = new RegisterUserRequestDto(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "John",
                    "Doe"
            );

            // When
            RegisterUserCommand command = mapper.toCommand(requestDto);

            // Then - Verify command mapping
            assertThat(command).isNotNull();
            assertThat(command.username()).isEqualTo(requestDto.username());
            assertThat(command.email()).isEqualTo(requestDto.email());
            assertThat(command.password()).isEqualTo(requestDto.password());
            assertThat(command.firstName()).isEqualTo(requestDto.firstName());
            assertThat(command.lastName()).isEqualTo(requestDto.lastName());

            // Given - Create a user entity with similar data (simulate after registration)
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            User user = User.builder()
                    .id(userId)
                    .username(command.username())
                    .email(command.email())
                    .passwordHash("hashedPassword") // Password would be hashed
                    .firstName(command.firstName())
                    .lastName(command.lastName())
                    .isActive(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // When
            UserResponseDto responseDto = mapper.toResponseDto(user);

            // Then - Verify response mapping
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.username()).isEqualTo(requestDto.username());
            assertThat(responseDto.email()).isEqualTo(requestDto.email());
            assertThat(responseDto.firstName()).isEqualTo(requestDto.firstName());
            assertThat(responseDto.lastName()).isEqualTo(requestDto.lastName());
            assertThat(responseDto.fullName()).isEqualTo("John Doe");
            assertThat(responseDto.isActive()).isTrue();
            assertThat(responseDto.id()).isEqualTo(userId);
            assertThat(responseDto.createdAt()).isEqualTo(now);
            assertThat(responseDto.updatedAt()).isEqualTo(now);
        }
    }
}
