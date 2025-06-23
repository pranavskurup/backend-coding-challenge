package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.exception.AuthenticationFailedException;
import com.movie.rating.system.domain.exception.UserAccountInactiveException;
import com.movie.rating.system.domain.port.inbound.UserAuthenticationUseCase;
import com.movie.rating.system.domain.port.outbound.JwtTokenService;
import com.movie.rating.system.domain.port.outbound.UserRepository;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.LoginRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.mapper.AuthenticationWebMapper;
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
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Tests for AuthenticationHandler
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationHandlerTest {

    @Mock
    private UserAuthenticationUseCase userAuthenticationUseCase;
    
    @Mock
    private JwtTokenService jwtTokenService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private Validator validator;

    private AuthenticationHandler authenticationHandler;
    private AuthenticationWebMapper authenticationWebMapper;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() throws Exception {
        authenticationWebMapper = new AuthenticationWebMapper();
        authenticationHandler = new AuthenticationHandler(
                userAuthenticationUseCase,
                jwtTokenService,
                userRepository,
                authenticationWebMapper,
                validator
        );

        // Set the duration fields using reflection since they're @Value injected
        java.lang.reflect.Field accessTokenDurationField = AuthenticationHandler.class.getDeclaredField("accessTokenDuration");
        accessTokenDurationField.setAccessible(true);
        accessTokenDurationField.set(authenticationHandler, Duration.ofHours(1));

        java.lang.reflect.Field refreshTokenDurationField = AuthenticationHandler.class.getDeclaredField("refreshTokenDuration");
        refreshTokenDurationField.setAccessible(true);
        refreshTokenDurationField.set(authenticationHandler, Duration.ofDays(7));

        RouterFunction<ServerResponse> routerFunction = route()
                .path("/api/v1/auth", builder -> builder
                        .POST("/login", authenticationHandler::login)
                        .POST("/refresh", authenticationHandler::refreshToken)
                        .POST("/logout", authenticationHandler::logout)
                )
                .build();

        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = User.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash("hashed-password")
                    .firstName("Test")
                    .lastName("User")
                    .isActive(true)
                    .createdAt(Instant.now())
                    .build();

            LoginRequestDto requestDto = new LoginRequestDto("testuser", "password123");

            when(validator.validate(any(LoginRequestDto.class))).thenReturn(java.util.Set.of());
            when(userAuthenticationUseCase.authenticate(any())).thenReturn(Mono.just(user));
            when(jwtTokenService.generateToken(any(UUID.class), any(String.class), any(String.class), any(Duration.class)))
                    .thenReturn(Mono.just("access-token"));
            when(jwtTokenService.generateToken(any(UUID.class), any(String.class), any(String.class), any(Duration.class), any()))
                    .thenReturn(Mono.just("refresh-token"));
            when(userAuthenticationUseCase.updateLastLogin(any(UUID.class))).thenReturn(Mono.empty());

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.access_token").isEqualTo("access-token")
                    .jsonPath("$.refresh_token").isEqualTo("refresh-token")
                    .jsonPath("$.token_type").isEqualTo("Bearer")
                    .jsonPath("$.user.id").isEqualTo(userId.toString())
                    .jsonPath("$.user.username").isEqualTo("testuser")
                    .jsonPath("$.user.email").isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturn401ForInvalidCredentials() {
            // Given
            LoginRequestDto requestDto = new LoginRequestDto("testuser", "wrongpassword");

            when(validator.validate(any(LoginRequestDto.class))).thenReturn(java.util.Set.of());
            when(userAuthenticationUseCase.authenticate(any()))
                    .thenReturn(Mono.error(new AuthenticationFailedException("Invalid username/email or password")));

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Authentication failed")
                    .jsonPath("$.status").isEqualTo(401);
        }

        @Test
        @DisplayName("Should return 403 for inactive account")
        void shouldReturn403ForInactiveAccount() {
            // Given
            LoginRequestDto requestDto = new LoginRequestDto("testuser", "password123");

            when(validator.validate(any(LoginRequestDto.class))).thenReturn(java.util.Set.of());
            when(userAuthenticationUseCase.authenticate(any()))
                    .thenReturn(Mono.error(new UserAccountInactiveException("User account is deactivated")));

            // When & Then
            webTestClient.post()
                    .uri("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Account inactive")
                    .jsonPath("$.status").isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void shouldLogoutSuccessfully() {
            // When & Then
            webTestClient.post()
                    .uri("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Logged out successfully");
        }
    }
}
