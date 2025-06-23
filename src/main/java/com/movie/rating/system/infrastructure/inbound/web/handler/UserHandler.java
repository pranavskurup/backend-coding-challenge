package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.domain.exception.EmailAlreadyExistsException;
import com.movie.rating.system.domain.exception.UsernameAlreadyExistsException;
import com.movie.rating.system.domain.port.inbound.RegisterUserUseCase;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.RegisterUserRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.AvailabilityResponseDto;
import com.movie.rating.system.infrastructure.inbound.web.mapper.UserWebMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handler for user-related HTTP requests using Spring WebFlux functional endpoints.
 * Handles user registration, availability checks, and related operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandler {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserWebMapper userWebMapper;
    private final Validator validator;

    /**
     * Handles user registration requests.
     *
     * @param request the HTTP request
     * @return ServerResponse containing the registered user or error
     */
    public Mono<ServerResponse> registerUser(ServerRequest request) {
        log.info("Received user registration request");

        return request.bodyToMono(RegisterUserRequestDto.class)
                .flatMap(this::validateRequest)
                .map(userWebMapper::toCommand)
                .flatMap(registerUserUseCase::registerUser)
                .map(userWebMapper::toResponseDto)
                .flatMap(userDto ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(userDto))
                )
                .onErrorResume(this::handleRegistrationError)
                .doOnSuccess(response -> log.info("User registration request completed"))
                .doOnError(error -> log.error("Error processing user registration", error));
    }

    /**
     * Checks if a username is available.
     *
     * @param request the HTTP request
     * @return ServerResponse containing availability status
     */
    public Mono<ServerResponse> checkUsernameAvailability(ServerRequest request) {
        String username = request.queryParam("username")
                .orElse("");

        log.debug("Checking username availability for: {}", username);

        return registerUserUseCase.isUsernameAvailable(username)
                .map(available -> new AvailabilityResponseDto(
                        available,
                        "username",
                        username,
                        available ? "Username is available" : "Username is already taken"
                ))
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(response))
                )
                .onErrorResume(error -> {
                    log.error("Error checking username availability", error);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(
                                    Map.of("error", "Failed to check username availability")
                            ));
                });
    }

    /**
     * Checks if an email is available.
     *
     * @param request the HTTP request
     * @return ServerResponse containing availability status
     */
    public Mono<ServerResponse> checkEmailAvailability(ServerRequest request) {
        String email = request.queryParam("email")
                .orElse("");

        log.debug("Checking email availability for: {}", email);

        return registerUserUseCase.isEmailAvailable(email)
                .map(available -> new AvailabilityResponseDto(
                        available,
                        "email",
                        email,
                        available ? "Email is available" : "Email is already taken"
                ))
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(response))
                )
                .onErrorResume(error -> {
                    log.error("Error checking email availability", error);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(
                                    Map.of("error", "Failed to check email availability")
                            ));
                });
    }

    /**
     * Validates the registration request DTO.
     *
     * @param requestDto the request DTO to validate
     * @return Mono containing the validated DTO or error
     */
    private Mono<RegisterUserRequestDto> validateRequest(RegisterUserRequestDto requestDto) {
        Set<ConstraintViolation<RegisterUserRequestDto>> violations = validator.validate(requestDto);

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
     * Handles registration errors and returns appropriate HTTP responses.
     *
     * @param error the error that occurred
     * @return ServerResponse with appropriate error status and message
     */
    private Mono<ServerResponse> handleRegistrationError(Throwable error) {
        log.error("Registration error: {}", error.getMessage(), error);

        if (error instanceof UsernameAlreadyExistsException) {
            return ServerResponse.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            Map.of(
                                    "error", "Username already exists",
                                    "message", error.getMessage(),
                                    "field", "username"
                            )
                    ));
        }

        if (error instanceof EmailAlreadyExistsException) {
            return ServerResponse.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            Map.of(
                                    "error", "Email already exists",
                                    "message", error.getMessage(),
                                    "field", "email"
                            )
                    ));
        }

        if (error instanceof ValidationException validationException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            Map.of(
                                    "error", "Validation failed",
                                    "message", validationException.getMessage(),
                                    "violations", validationException.getErrors()
                            )
                    ));
        }

        // Generic error response
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        Map.of(
                                "error", "Internal server error",
                                "message", "An unexpected error occurred during registration"
                        )
                ));
    }

    /**
     * Custom exception for validation errors.
     */
    public static class ValidationException extends RuntimeException {
        private final Map<String, String> errors;

        public ValidationException(String message, Map<String, String> errors) {
            super(message);
            this.errors = errors;
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }
}
