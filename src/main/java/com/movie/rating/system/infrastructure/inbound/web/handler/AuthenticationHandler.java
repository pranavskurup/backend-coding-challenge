package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.domain.exception.AuthenticationFailedException;
import com.movie.rating.system.domain.exception.InvalidTokenException;
import com.movie.rating.system.domain.exception.UserAccountInactiveException;
import com.movie.rating.system.domain.exception.UserNotFoundException;
import com.movie.rating.system.domain.exception.ValidationException;
import com.movie.rating.system.domain.port.inbound.UserAuthenticationUseCase;
import com.movie.rating.system.domain.port.outbound.JwtTokenService;
import com.movie.rating.system.domain.port.outbound.UserRepository;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.LoginRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.RefreshTokenRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.ErrorResponseDto;
import com.movie.rating.system.infrastructure.inbound.web.mapper.AuthenticationWebMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handler for authentication operations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationHandler {

    private final UserAuthenticationUseCase userAuthenticationUseCase;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final AuthenticationWebMapper authenticationWebMapper;
    private final Validator validator;

    @Value("${app.jwt.access-token-duration:PT1H}")
    private Duration accessTokenDuration;

    @Value("${app.jwt.refresh-token-duration:P7D}")
    private Duration refreshTokenDuration;

    /**
     * Handle user login
     */
    public Mono<ServerResponse> login(ServerRequest request) {
        log.info("Received login request");
        
        return request.bodyToMono(LoginRequestDto.class)
                .flatMap(this::validateLoginRequest)
                .flatMap(requestDto -> {
                    var command = authenticationWebMapper.toCommand(requestDto);
                    log.debug("Authenticating user: {}", command.usernameOrEmail());
                    return userAuthenticationUseCase.authenticate(command);
                })
                .flatMap(user -> {
                    // Generate tokens
                    Mono<String> accessTokenMono = jwtTokenService.generateToken(
                            user.getId(), user.getUsername(), user.getEmail(), accessTokenDuration
                    );
                    Mono<String> refreshTokenMono = jwtTokenService.generateToken(
                            user.getId(), user.getUsername(), user.getEmail(), refreshTokenDuration,
                            Map.of("token_type", "refresh")
                    );

                    return Mono.zip(accessTokenMono, refreshTokenMono)
                            .flatMap(tokens -> {
                                String accessToken = tokens.getT1();
                                String refreshToken = tokens.getT2();
                                
                                // Update last login
                                return userAuthenticationUseCase.updateLastLogin(user.getId())
                                        .then(Mono.just(authenticationWebMapper.toAuthenticationResponse(
                                                user, accessToken, refreshToken, accessTokenDuration)));
                            });
                })
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(response))
                )
                .onErrorResume(this::handleAuthenticationError)
                .doFinally(signalType -> log.info("Login request completed"));
    }

    /**
     * Handle token refresh
     */
    public Mono<ServerResponse> refreshToken(ServerRequest request) {
        log.info("Received token refresh request");
        
        return request.bodyToMono(RefreshTokenRequestDto.class)
                .flatMap(this::validateRefreshTokenRequest)
                .flatMap(requestDto -> {
                    return jwtTokenService.validateTokenWithBlacklist(requestDto.refreshToken())
                            .flatMap(tokenClaims -> {
                                // Verify it's a refresh token
                                Object tokenType = tokenClaims.customClaims().get("token_type");
                                if (!"refresh".equals(tokenType)) {
                                    return Mono.error(new InvalidTokenException("Invalid refresh token"));
                                }
                                
                                // Find user and generate new access token
                                return userRepository.findById(tokenClaims.userId())
                                        .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")))
                                        .flatMap(user -> {
                                            if (!user.isActive()) {
                                                return Mono.error(new UserAccountInactiveException("User account is deactivated"));
                                            }
                                            
                                            return jwtTokenService.generateToken(
                                                    user.getId(), user.getUsername(), user.getEmail(), accessTokenDuration
                                            ).map(newAccessToken -> authenticationWebMapper.toRefreshResponse(
                                                    user, newAccessToken, requestDto.refreshToken(), accessTokenDuration));
                                        });
                            });
                })
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(response))
                )
                .onErrorResume(this::handleAuthenticationError)
                .doFinally(signalType -> log.info("Token refresh request completed"));
    }

    /**
     * Handle logout (invalidate token)
     */
    public Mono<ServerResponse> logout(ServerRequest request) {
        log.info("Received logout request");
        
        return extractAuthorizationToken(request)
                .flatMap(token -> jwtTokenService.blacklistToken(token, "User logout"))
                .then(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(Map.of(
                                "message", "Logged out successfully",
                                "timestamp", java.time.Instant.now()
                        )))
                )
                .onErrorResume(error -> {
                    log.warn("Logout error (non-critical): {}", error.getMessage());
                    // Even if blacklisting fails, we return success for better UX
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(Map.of(
                                    "message", "Logged out successfully",
                                    "timestamp", java.time.Instant.now()
                            )));
                })
                .doFinally(signalType -> log.info("Logout request completed"));
    }

    /**
     * Extract authorization token from request header
     */
    private Mono<String> extractAuthorizationToken(ServerRequest request) {
        return Mono.fromCallable(() -> {
            String authHeader = request.headers().firstHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InvalidTokenException("Missing or invalid Authorization header");
            }
            return authHeader.substring(7); // Remove "Bearer " prefix
        });
    }

    /**
     * Validate login request
     */
    private Mono<LoginRequestDto> validateLoginRequest(LoginRequestDto requestDto) {
        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(requestDto);
        
        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                    .collect(Collectors.groupingBy(
                            violation -> violation.getPropertyPath().toString(),
                            Collectors.mapping(
                                    ConstraintViolation::getMessage,
                                    Collectors.joining("; ")
                            )
                    ));
            
            return Mono.error(new ValidationException("Validation failed", errors));
        }
        
        return Mono.just(requestDto);
    }

    /**
     * Validate refresh token request
     */
    private Mono<RefreshTokenRequestDto> validateRefreshTokenRequest(RefreshTokenRequestDto requestDto) {
        Set<ConstraintViolation<RefreshTokenRequestDto>> violations = validator.validate(requestDto);
        
        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                    .collect(Collectors.groupingBy(
                            violation -> violation.getPropertyPath().toString(),
                            Collectors.mapping(
                                    ConstraintViolation::getMessage,
                                    Collectors.joining("; ")
                            )
                    ));
            
            return Mono.error(new ValidationException("Validation failed", errors));
        }
        
        return Mono.just(requestDto);
    }

    /**
     * Handle authentication-related errors
     */
    private Mono<ServerResponse> handleAuthenticationError(Throwable error) {
        log.error("Authentication error: {}", error.getMessage(), error);

        if (error instanceof AuthenticationFailedException) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("Authentication failed", error.getMessage(), "", 401)
                    ));
        }

        if (error instanceof UserNotFoundException) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("Authentication failed", "Invalid username/email or password", "", 401)
                    ));
        }

        if (error instanceof UserAccountInactiveException) {
            return ServerResponse.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("Account inactive", error.getMessage(), "", 403)
                    ));
        }

        if (error instanceof InvalidTokenException) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("Invalid token", error.getMessage(), "", 401)
                    ));
        }

        if (error instanceof ValidationException validationEx) {
            Map<String, Object> details = Map.of("validationErrors", validationEx.getErrors());
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("Validation failed", validationEx.getMessage(), "", 400, details)
                    ));
        }

        // Generic error response
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        ErrorResponseDto.of("Internal server error", "An unexpected error occurred during authentication", "", 500)
                ));
    }
}
