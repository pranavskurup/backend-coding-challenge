package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.exception.EmailAlreadyExistsException;
import com.movie.rating.system.domain.exception.InvalidPasswordException;
import com.movie.rating.system.domain.exception.UserNotFoundException;
import com.movie.rating.system.domain.exception.ValidationException;
import com.movie.rating.system.domain.port.inbound.ManageUserProfileUseCase;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.ChangePasswordRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateUserProfileRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.OperationSuccessResponseDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.UserProfileResponseDto;
import com.movie.rating.system.infrastructure.inbound.web.mapper.UserProfileWebMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Unit tests for UserProfileHandler.
 * Tests HTTP request handling, validation, and error responses for profile operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Profile Handler Tests")
class UserProfileHandlerTest {

    @Mock
    private ManageUserProfileUseCase manageUserProfileUseCase;

    private UserProfileHandler userProfileHandler;
    private WebTestClient webTestClient;
    private UserProfileWebMapper userProfileWebMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        userProfileWebMapper = new UserProfileWebMapper();
        
        // Properly initialize the validator
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();
        validator = validatorFactoryBean;
        
        userProfileHandler = new UserProfileHandler(manageUserProfileUseCase, userProfileWebMapper, validator);

        RouterFunction<ServerResponse> routes = RouterFunctions.route()
                .GET("/api/v1/users/{userId}/profile",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::getUserProfile)
                .PUT("/api/v1/users/{userId}/profile",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        userProfileHandler::updateUserProfile)
                .POST("/api/v1/users/{userId}/change-password",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        userProfileHandler::changePassword)
                .POST("/api/v1/users/{userId}/deactivate",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::deactivateUser)
                .POST("/api/v1/users/{userId}/reactivate",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::reactivateUser)
                .GET("/api/v1/users",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::getAllActiveUsers)
                .GET("/api/v1/users/search",
                        accept(MediaType.APPLICATION_JSON),
                        userProfileHandler::searchUsersByUsername)
                .build();

        webTestClient = WebTestClient
                .bindToRouterFunction(routes)
                .webFilter(createTestAuthenticationFilter())
                .build();
    }
    
    /**
     * Create a test authentication filter that mocks user authentication
     */
    private org.springframework.web.server.WebFilter createTestAuthenticationFilter() {
        return (exchange, chain) -> {
            // Extract user ID from path variable if present
            String path = exchange.getRequest().getURI().getPath();
            if (path.contains("/users/")) {
                String[] pathParts = path.split("/users/");
                if (pathParts.length > 1) {
                    String userIdPart = pathParts[1].split("/")[0];
                    if (!userIdPart.equals("search")) { // Handle search endpoint
                        try {
                            UUID userId = UUID.fromString(userIdPart);
                            // Mock authentication for the requested user
                            exchange.getAttributes().put("userId", userId);
                            exchange.getAttributes().put("username", "testuser");
                            exchange.getAttributes().put("email", "test@example.com");
                        } catch (IllegalArgumentException e) {
                            // For invalid UUID in path, set a default test user
                            // The handler will validate the UUID format itself and return appropriate error
                            exchange.getAttributes().put("userId", UUID.randomUUID());
                            exchange.getAttributes().put("username", "testuser");
                            exchange.getAttributes().put("email", "test@example.com");
                        }
                    } else {
                        // For search endpoint, use a default test user
                        exchange.getAttributes().put("userId", UUID.randomUUID());
                        exchange.getAttributes().put("username", "testuser");
                        exchange.getAttributes().put("email", "test@example.com");
                    }
                }
            } else {
                // For non-user specific endpoints, set default authentication
                exchange.getAttributes().put("userId", UUID.randomUUID());
                exchange.getAttributes().put("username", "testuser");
                exchange.getAttributes().put("email", "test@example.com");
            }
            return chain.filter(exchange);
        };
    }

    @Nested
    @DisplayName("Get User Profile Tests")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should get user profile successfully")
        void shouldGetUserProfileSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId);
            when(manageUserProfileUseCase.getUserProfile(userId))
                    .thenReturn(Mono.just(user));

            // When & Then
            webTestClient.get()
                    .uri("/api/v1/users/{userId}/profile", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(UserProfileResponseDto.class)
                    .value(response -> {
                        assert response.id().equals(userId);
                        assert response.username().equals("testuser");
                        assert response.email().equals("test@example.com");
                        assert response.firstName().equals("John");
                        assert response.lastName().equals("Doe");
                        assert response.fullName().equals("John Doe");
                        assert response.isActive().equals(true);
                    });
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            when(manageUserProfileUseCase.getUserProfile(userId))
                    .thenReturn(Mono.error(new UserNotFoundException(userId.toString())));

            // When & Then
            webTestClient.get()
                    .uri("/api/v1/users/{userId}/profile", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("User not found")
                    .jsonPath("$.status").isEqualTo(404);
        }

        @Test
        @DisplayName("Should return 400 for invalid user ID format")
        void shouldReturn400ForInvalidUserIdFormat() {
            // When & Then
            webTestClient.get()
                    .uri("/api/v1/users/{userId}/profile", "invalid-uuid")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Validation failed")
                    .jsonPath("$.status").isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Update User Profile Tests")
    class UpdateUserProfileTests {

        @Test
        @DisplayName("Should update user profile successfully")
        void shouldUpdateUserProfileSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            UpdateUserProfileRequestDto requestDto = new UpdateUserProfileRequestDto(
                    "newemail@example.com",
                    "Jane",
                    "Smith"
            );

            User updatedUser = createTestUser(userId)
                    .toBuilder()
                    .email("newemail@example.com")
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            when(manageUserProfileUseCase.updateUserProfile(any()))
                    .thenReturn(Mono.just(updatedUser));

            // When & Then
            webTestClient.put()
                    .uri("/api/v1/users/{userId}/profile", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(UserProfileResponseDto.class)
                    .value(response -> {
                        assert response.email().equals("newemail@example.com");
                        assert response.firstName().equals("Jane");
                        assert response.lastName().equals("Smith");
                        assert response.fullName().equals("Jane Smith");
                    });
        }

        @Test
        @DisplayName("Should return 400 when no fields provided for update")
        void shouldReturn400WhenNoFieldsProvidedForUpdate() {
            // Given
            UUID userId = UUID.randomUUID();
            UpdateUserProfileRequestDto requestDto = new UpdateUserProfileRequestDto(null, null, null);

            // When & Then
            webTestClient.put()
                    .uri("/api/v1/users/{userId}/profile", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Validation failed")
                    .jsonPath("$.status").isEqualTo(400);
        }

        @Test
        @DisplayName("Should return 400 for invalid email")
        void shouldReturn400ForInvalidEmail() {
            // Given
            UUID userId = UUID.randomUUID();
            UpdateUserProfileRequestDto requestDto = new UpdateUserProfileRequestDto(
                    "invalid-email",
                    "Jane",
                    "Smith"
            );

            // When & Then
            webTestClient.put()
                    .uri("/api/v1/users/{userId}/profile", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Validation failed")
                    .jsonPath("$.status").isEqualTo(400);
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn409WhenEmailAlreadyExists() {
            // Given
            UUID userId = UUID.randomUUID();
            UpdateUserProfileRequestDto requestDto = new UpdateUserProfileRequestDto(
                    "existing@example.com",
                    "Jane",
                    "Smith"
            );

            when(manageUserProfileUseCase.updateUserProfile(any()))
                    .thenReturn(Mono.error(new EmailAlreadyExistsException("existing@example.com")));

            // When & Then
            webTestClient.put()
                    .uri("/api/v1/users/{userId}/profile", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isEqualTo(409)
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Email already exists")
                    .jsonPath("$.status").isEqualTo(409);
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto(
                    "currentPassword123",
                    "newPassword456"
            );

            when(manageUserProfileUseCase.changePassword(any()))
                    .thenReturn(Mono.empty());

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/users/{userId}/change-password", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(OperationSuccessResponseDto.class)
                    .value(response -> {
                        assert response.success().equals(true);
                        assert response.message().equals("Password changed successfully");
                    });
        }

        @Test
        @DisplayName("Should return 400 for invalid current password")
        void shouldReturn400ForInvalidCurrentPassword() {
            // Given
            UUID userId = UUID.randomUUID();
            ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto(
                    "wrongPassword",
                    "newPassword456"
            );

            when(manageUserProfileUseCase.changePassword(any()))
                    .thenReturn(Mono.error(new InvalidPasswordException()));

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/users/{userId}/change-password", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Invalid password")
                    .jsonPath("$.status").isEqualTo(400);
        }

        @Test
        @DisplayName("Should return 400 for validation errors")
        void shouldReturn400ForValidationErrors() {
            // Given
            UUID userId = UUID.randomUUID();
            ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto(
                    "", // Empty current password
                    "123" // Too short new password
            );

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/users/{userId}/change-password", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Validation failed")
                    .jsonPath("$.status").isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Deactivate User Tests")
    class DeactivateUserTests {

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            when(manageUserProfileUseCase.deactivateUser(userId))
                    .thenReturn(Mono.empty());

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/users/{userId}/deactivate", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(OperationSuccessResponseDto.class)
                    .value(response -> {
                        assert response.success().equals(true);
                        assert response.message().equals("User deactivated successfully");
                    });
        }

        @Test
        @DisplayName("Should return 404 when user not found for deactivation")
        void shouldReturn404WhenUserNotFoundForDeactivation() {
            // Given
            UUID userId = UUID.randomUUID();
            when(manageUserProfileUseCase.deactivateUser(userId))
                    .thenReturn(Mono.error(new UserNotFoundException(userId.toString())));

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/users/{userId}/deactivate", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("User not found")
                    .jsonPath("$.status").isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("Reactivate User Tests")
    class ReactivateUserTests {

        @Test
        @DisplayName("Should reactivate user successfully")
        void shouldReactivateUserSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            User reactivatedUser = createTestUser(userId);
            when(manageUserProfileUseCase.reactivateUser(userId))
                    .thenReturn(Mono.just(reactivatedUser));

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/users/{userId}/reactivate", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(UserProfileResponseDto.class)
                    .value(response -> {
                        assert response.id().equals(userId);
                        assert response.isActive().equals(true);
                    });
        }

        @Test
        @DisplayName("Should return 404 when user not found for reactivation")
        void shouldReturn404WhenUserNotFoundForReactivation() {
            // Given
            UUID userId = UUID.randomUUID();
            when(manageUserProfileUseCase.reactivateUser(userId))
                    .thenReturn(Mono.error(new UserNotFoundException(userId.toString())));

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/users/{userId}/reactivate", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("User not found")
                    .jsonPath("$.status").isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("Get All Active Users Tests")
    class GetAllActiveUsersTests {

        @Test
        @DisplayName("Should get all active users successfully")
        void shouldGetAllActiveUsersSuccessfully() {
            // Given
            List<User> activeUsers = Arrays.asList(
                    createTestUser(UUID.randomUUID(), "user1", "user1@example.com"),
                    createTestUser(UUID.randomUUID(), "user2", "user2@example.com"),
                    createTestUser(UUID.randomUUID(), "user3", "user3@example.com")
            );

            when(manageUserProfileUseCase.getAllActiveUsers())
                    .thenReturn(Flux.fromIterable(activeUsers));

            // When & Then
            webTestClient.get()
                    .uri("/api/v1/users")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBodyList(UserProfileResponseDto.class)
                    .value(profiles -> {
                        assert profiles.size() == 3;
                        assert profiles.get(0).username().equals("user1");
                        assert profiles.get(1).username().equals("user2");
                        assert profiles.get(2).username().equals("user3");
                    });
        }

        @Test
        @DisplayName("Should return empty list when no active users")
        void shouldReturnEmptyListWhenNoActiveUsers() {
            // Given
            when(manageUserProfileUseCase.getAllActiveUsers())
                    .thenReturn(Flux.empty());

            // When & Then
            webTestClient.get()
                    .uri("/api/v1/users")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBodyList(UserProfileResponseDto.class)
                    .value(profiles -> {
                        assert profiles.isEmpty();
                    });
        }
    }

    @Nested
    @DisplayName("Search Users Tests")
    class SearchUsersTests {

        @Test
        @DisplayName("Should search users by pattern successfully")
        void shouldSearchUsersByPatternSuccessfully() {
            // Given
            String pattern = "test";
            List<User> matchingUsers = Arrays.asList(
                    createTestUser(UUID.randomUUID(), "testuser1", "test1@example.com"),
                    createTestUser(UUID.randomUUID(), "testuser2", "test2@example.com")
            );

            when(manageUserProfileUseCase.searchUsersByUsername(pattern))
                    .thenReturn(Flux.fromIterable(matchingUsers));

            // When & Then
            webTestClient.get()
                    .uri("/api/v1/users/search?pattern={pattern}", pattern)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBodyList(UserProfileResponseDto.class)
                    .value(profiles -> {
                        assert profiles.size() == 2;
                        assert profiles.get(0).username().equals("testuser1");
                        assert profiles.get(1).username().equals("testuser2");
                    });
        }

        @Test
        @DisplayName("Should handle empty search pattern")
        void shouldHandleEmptySearchPattern() {
            // Given
            List<User> allActiveUsers = Arrays.asList(
                    createTestUser(UUID.randomUUID(), "user1", "user1@example.com"),
                    createTestUser(UUID.randomUUID(), "user2", "user2@example.com")
            );

            when(manageUserProfileUseCase.searchUsersByUsername(""))
                    .thenReturn(Flux.fromIterable(allActiveUsers));

            // When & Then
            webTestClient.get()
                    .uri("/api/v1/users/search")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBodyList(UserProfileResponseDto.class)
                    .value(profiles -> {
                        assert profiles.size() == 2;
                    });
        }

        @Test
        @DisplayName("Should return empty list when no users match pattern")
        void shouldReturnEmptyListWhenNoUsersMatchPattern() {
            // Given
            when(manageUserProfileUseCase.searchUsersByUsername(anyString()))
                    .thenReturn(Flux.empty());

            // When & Then
            webTestClient.get()
                    .uri("/api/v1/users/search?pattern=nonexistent")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBodyList(UserProfileResponseDto.class)
                    .value(profiles -> {
                        assert profiles.isEmpty();
                    });
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle validation exception with multiple errors")
        void shouldHandleValidationExceptionWithMultipleErrors() {
            // Given
            UUID userId = UUID.randomUUID();
            Map<String, String> errors = Map.of(
                    "email", "Email is invalid",
                    "firstName", "First name is required"
            );
            
            when(manageUserProfileUseCase.updateUserProfile(any()))
                    .thenReturn(Mono.error(new ValidationException("Validation failed", errors)));

            UpdateUserProfileRequestDto requestDto = new UpdateUserProfileRequestDto(
                    "valid@example.com",
                    "Jane",
                    "Smith"
            );

            // When & Then
            webTestClient.put()
                    .uri("/api/v1/users/{userId}/profile", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Validation failed")
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.details.validationErrors").exists();
        }

        @Test
        @DisplayName("Should handle internal server error")
        void shouldHandleInternalServerError() {
            // Given
            UUID userId = UUID.randomUUID();
            when(manageUserProfileUseCase.getUserProfile(userId))
                    .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

            // When & Then
            webTestClient.get()
                    .uri("/api/v1/users/{userId}/profile", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Internal server error")
                    .jsonPath("$.status").isEqualTo(500);
        }
    }

    private User createTestUser(UUID userId) {
        return createTestUser(userId, "testuser", "test@example.com");
    }

    private User createTestUser(UUID userId, String username, String email) {
        Instant now = Instant.now();
        return User.builder()
                .id(userId)
                .username(username)
                .email(email)
                .passwordHash("$2a$12$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
