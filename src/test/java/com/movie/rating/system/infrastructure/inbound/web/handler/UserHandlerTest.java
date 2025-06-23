package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.exception.UsernameAlreadyExistsException;
import com.movie.rating.system.domain.port.inbound.RegisterUserUseCase;
import com.movie.rating.system.domain.port.inbound.RegisterUserUseCase.RegisterUserCommand;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.RegisterUserRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.AvailabilityResponseDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.UserResponseDto;
import com.movie.rating.system.infrastructure.inbound.web.handler.UserHandler;
import com.movie.rating.system.infrastructure.inbound.web.mapper.UserWebMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Unit tests for UserHandler.
 * Tests HTTP request handling, validation, and error responses.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Handler Tests")
class UserHandlerTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    private UserHandler userHandler;
    private WebTestClient webTestClient;
    private UserWebMapper userWebMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        userWebMapper = new UserWebMapper();

        // Properly initialize the validator
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();
        validator = validatorFactoryBean;

        userHandler = new UserHandler(registerUserUseCase, userWebMapper, validator);

        RouterFunction<ServerResponse> routes = RouterFunctions.route()
                .POST("/api/v1/auth/register",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        userHandler::registerUser)
                .GET("/api/v1/users/check/username",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::checkUsernameAvailability)
                .GET("/api/v1/users/check/email",
                        accept(MediaType.APPLICATION_JSON),
                        userHandler::checkEmailAvailability)
                .build();

        webTestClient = WebTestClient
                .bindToRouterFunction(routes)
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto(
                "testuser",
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        User savedUser = createTestUser();
        when(registerUserUseCase.registerUser(any(RegisterUserCommand.class)))
                .thenReturn(Mono.just(savedUser));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseDto.class)
                .value(response -> {
                    assert response.username().equals("testuser");
                    assert response.email().equals("test@example.com");
                    assert response.firstName().equals("John");
                    assert response.lastName().equals("Doe");
                });
    }

    @Test
    @DisplayName("Should return conflict when username already exists")
    void shouldReturnConflictWhenUsernameAlreadyExists() {
        // Given
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto(
                "existinguser",
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        when(registerUserUseCase.registerUser(any(RegisterUserCommand.class)))
                .thenReturn(Mono.error(new UsernameAlreadyExistsException("existinguser")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Username already exists")
                .jsonPath("$.field").isEqualTo("username");
    }

    @Test
    @DisplayName("Should return bad request for invalid input")
    void shouldReturnBadRequestForInvalidInput() {
        // Given
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto(
                "", // Invalid username
                "invalid-email", // Invalid email
                "123", // Too short password
                "John",
                "Doe"
        );

        // When & Then
        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Validation failed");
    }

    @Test
    @DisplayName("Should check username availability")
    void shouldCheckUsernameAvailability() {
        // Given
        when(registerUserUseCase.isUsernameAvailable("testuser"))
                .thenReturn(Mono.just(true));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/users/check/username?username=testuser")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AvailabilityResponseDto.class)
                .value(response -> {
                    assert response.available().equals(true);
                    assert response.field().equals("username");
                    assert response.value().equals("testuser");
                });
    }

    @Test
    @DisplayName("Should check email availability")
    void shouldCheckEmailAvailability() {
        // Given
        when(registerUserUseCase.isEmailAvailable("test@example.com"))
                .thenReturn(Mono.just(false));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/users/check/email?email=test@example.com")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AvailabilityResponseDto.class)
                .value(response -> {
                    assert response.available().equals(false);
                    assert response.field().equals("email");
                    assert response.value().equals("test@example.com");
                });
    }

    @Test
    @DisplayName("Should handle empty username availability check")
    void shouldHandleEmptyUsernameAvailabilityCheck() {
        // Given
        when(registerUserUseCase.isUsernameAvailable(anyString()))
                .thenReturn(Mono.just(false));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/users/check/username")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AvailabilityResponseDto.class)
                .value(response -> {
                    assert response.available().equals(false);
                    assert response.field().equals("username");
                    assert response.value().equals("");
                });
    }

    private User createTestUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$12$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
