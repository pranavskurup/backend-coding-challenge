package com.movie.rating.system.infrastructure.inbound.web.handler;

import com.movie.rating.system.domain.exception.EmailAlreadyExistsException;
import com.movie.rating.system.domain.exception.InvalidPasswordException;
import com.movie.rating.system.domain.exception.UserAccountInactiveException;
import com.movie.rating.system.domain.exception.UserNotFoundException;
import com.movie.rating.system.domain.exception.ValidationException;
import com.movie.rating.system.domain.port.inbound.ManageUserProfileUseCase;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.ChangePasswordRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateUserProfileRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.ErrorResponseDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.OperationSuccessResponseDto;
import com.movie.rating.system.infrastructure.inbound.web.mapper.UserProfileWebMapper;
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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler for user profile management operations.
 * Handles HTTP requests for profile operations using reactive patterns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileHandler {

    private final ManageUserProfileUseCase manageUserProfileUseCase;
    private final UserProfileWebMapper userProfileWebMapper;
    private final Validator validator;

    /**
     * Get user profile by ID.
     *
     * @param request the HTTP request
     * @return ServerResponse containing user profile or error
     */
    public Mono<ServerResponse> getUserProfile(ServerRequest request) {
        log.info("Received get user profile request");
        
        return extractUserIdFromPath(request)
                .flatMap(userId -> {
                    log.debug("Getting profile for user ID: {}", userId);
                    return manageUserProfileUseCase.getUserProfile(userId);
                })
                .map(userProfileWebMapper::toProfileResponseDto)
                .flatMap(profile ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(profile))
                )
                .onErrorResume(this::handleProfileError)
                .doFinally(signalType -> log.info("Get user profile request completed"));
    }

    /**
     * Update user profile.
     *
     * @param request the HTTP request
     * @return ServerResponse containing updated profile or error
     */
    public Mono<ServerResponse> updateUserProfile(ServerRequest request) {
        log.info("Received update user profile request");
        
        return extractUserIdFromPath(request)
                .flatMap(userId ->
                        request.bodyToMono(UpdateUserProfileRequestDto.class)
                                .flatMap(this::validateUpdateProfileRequest)
                                .flatMap(requestDto -> {
                                    if (!requestDto.hasAnyUpdate()) {
                                        return Mono.error(new ValidationException(
                                                "At least one field must be provided for update", 
                                                Map.of("request", "No update fields provided")
                                        ));
                                    }
                                    
                                    var command = userProfileWebMapper.toCommand(userId, requestDto);
                                    log.debug("Updating profile for user ID: {} with command: {}", userId, command);
                                    return manageUserProfileUseCase.updateUserProfile(command);
                                })
                )
                .map(userProfileWebMapper::toProfileResponseDto)
                .flatMap(profile ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(profile))
                )
                .onErrorResume(this::handleProfileError)
                .doFinally(signalType -> log.info("Update user profile request completed"));
    }

    /**
     * Change user password.
     *
     * @param request the HTTP request
     * @return ServerResponse indicating success or error
     */
    public Mono<ServerResponse> changePassword(ServerRequest request) {
        log.info("Received change password request");
        
        return extractUserIdFromPath(request)
                .flatMap(userId ->
                        request.bodyToMono(ChangePasswordRequestDto.class)
                                .flatMap(this::validateChangePasswordRequest)
                                .flatMap(requestDto -> {
                                    var command = userProfileWebMapper.toCommand(userId, requestDto);
                                    log.debug("Changing password for user ID: {}", userId);
                                    return manageUserProfileUseCase.changePassword(command);
                                })
                )
                .then(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(
                                OperationSuccessResponseDto.ofSuccess("Password changed successfully")
                        ))
                )
                .onErrorResume(this::handleProfileError)
                .doFinally(signalType -> log.info("Change password request completed"));
    }

    /**
     * Deactivate user account.
     *
     * @param request the HTTP request
     * @return ServerResponse indicating success or error
     */
    public Mono<ServerResponse> deactivateUser(ServerRequest request) {
        log.info("Received deactivate user request");
        
        return extractUserIdFromPath(request)
                .flatMap(userId -> {
                    log.debug("Deactivating user ID: {}", userId);
                    return manageUserProfileUseCase.deactivateUser(userId);
                })
                .then(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(
                                OperationSuccessResponseDto.ofSuccess("User deactivated successfully")
                        ))
                )
                .onErrorResume(this::handleProfileError)
                .doFinally(signalType -> log.info("Deactivate user request completed"));
    }

    /**
     * Reactivate user account.
     *
     * @param request the HTTP request
     * @return ServerResponse containing reactivated user profile or error
     */
    public Mono<ServerResponse> reactivateUser(ServerRequest request) {
        log.info("Received reactivate user request");
        
        return extractUserIdFromPath(request)
                .flatMap(userId -> {
                    log.debug("Reactivating user ID: {}", userId);
                    return manageUserProfileUseCase.reactivateUser(userId);
                })
                .map(userProfileWebMapper::toProfileResponseDto)
                .flatMap(profile ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(profile))
                )
                .onErrorResume(this::handleProfileError)
                .doFinally(signalType -> log.info("Reactivate user request completed"));
    }

    /**
     * Get all active users (admin operation).
     *
     * @param request the HTTP request
     * @return ServerResponse containing list of active users or error
     */
    public Mono<ServerResponse> getAllActiveUsers(ServerRequest request) {
        log.info("Received get all active users request");
        
        return manageUserProfileUseCase.getAllActiveUsers()
                .map(userProfileWebMapper::toProfileResponseDto)
                .collectList()
                .flatMap(profiles ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(profiles))
                )
                .onErrorResume(this::handleProfileError)
                .doFinally(signalType -> log.info("Get all active users request completed"));
    }

    /**
     * Search users by username pattern.
     *
     * @param request the HTTP request
     * @return ServerResponse containing matching users or error
     */
    public Mono<ServerResponse> searchUsersByUsername(ServerRequest request) {
        log.info("Received search users request");
        
        String pattern = request.queryParam("pattern")
                .orElse("");
        
        log.debug("Searching users with pattern: {}", pattern);
        
        return manageUserProfileUseCase.searchUsersByUsername(pattern)
                .map(userProfileWebMapper::toProfileResponseDto)
                .collectList()
                .flatMap(profiles ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(profiles))
                )
                .onErrorResume(this::handleProfileError)
                .doFinally(signalType -> log.info("Search users request completed"));
    }

    /**
     * Extract user ID from path variable.
     */
    private Mono<UUID> extractUserIdFromPath(ServerRequest request) {
        try {
            String userIdStr = request.pathVariable("userId");
            UUID userId = UUID.fromString(userIdStr);
            return Mono.just(userId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user ID format: {}", request.pathVariable("userId"));
            return Mono.error(new ValidationException(
                    "Invalid user ID format", 
                    Map.of("userId", "User ID must be a valid UUID")
            ));
        }
    }

    /**
     * Validate update profile request.
     */
    private Mono<UpdateUserProfileRequestDto> validateUpdateProfileRequest(UpdateUserProfileRequestDto requestDto) {
        Set<ConstraintViolation<UpdateUserProfileRequestDto>> violations = validator.validate(requestDto);
        
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
     * Validate change password request.
     */
    private Mono<ChangePasswordRequestDto> validateChangePasswordRequest(ChangePasswordRequestDto requestDto) {
        Set<ConstraintViolation<ChangePasswordRequestDto>> violations = validator.validate(requestDto);
        
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
     * Handle profile-related errors and return appropriate HTTP responses.
     */
    private Mono<ServerResponse> handleProfileError(Throwable error) {
        log.error("Profile operation error: {}", error.getMessage(), error);

        if (error instanceof UserNotFoundException) {
            return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("User not found", error.getMessage(), "", 404)
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

        // Add handling for InvalidPasswordException
        if (error instanceof InvalidPasswordException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("Invalid password", error.getMessage(), "", 400)
                    ));
        }

        // Add handling for UserAccountInactiveException
        if (error instanceof UserAccountInactiveException) {
            return ServerResponse.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("Account inactive", error.getMessage(), "", 403)
                    ));
        }

        // Add handling for EmailAlreadyExistsException
        if (error instanceof EmailAlreadyExistsException) {
            return ServerResponse.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("Email already exists", error.getMessage(), "", 409)
                    ));
        }

        if (error instanceof IllegalArgumentException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            ErrorResponseDto.of("Invalid request", error.getMessage(), "", 400)
                    ));
        }

        // Generic error response
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        ErrorResponseDto.of("Internal server error", "An unexpected error occurred during profile operation", "", 500)
                ));
    }
}
