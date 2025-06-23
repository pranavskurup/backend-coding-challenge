package com.movie.rating.system.infrastructure.inbound.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for operation success responses.
 */
public record OperationSuccessResponseDto(
        
        @JsonProperty("success")
        Boolean success,
        
        @JsonProperty("message")
        String message
) {
    /**
     * Create a success response with default message
     */
    public static OperationSuccessResponseDto ofSuccess() {
        return new OperationSuccessResponseDto(true, "Operation completed successfully");
    }
    
    /**
     * Create a success response with custom message
     */
    public static OperationSuccessResponseDto ofSuccess(String message) {
        return new OperationSuccessResponseDto(true, message);
    }
}
