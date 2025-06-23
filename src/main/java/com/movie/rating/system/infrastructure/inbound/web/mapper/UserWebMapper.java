package com.movie.rating.system.infrastructure.inbound.web.mapper;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.port.inbound.RegisterUserUseCase.RegisterUserCommand;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.RegisterUserRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.UserResponseDto;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between web DTOs and domain entities/commands.
 * Handles the conversion of data between the web layer and domain layer.
 */
@Component
public class UserWebMapper {

    /**
     * Converts a RegisterUserRequestDto to a RegisterUserCommand.
     *
     * @param requestDto the web request DTO
     * @return the domain command
     */
    public RegisterUserCommand toCommand(RegisterUserRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }

        return new RegisterUserCommand(
                requestDto.username(),
                requestDto.email(),
                requestDto.password(),
                requestDto.firstName(),
                requestDto.lastName()
        );
    }

    /**
     * Converts a domain User entity to a UserResponseDto.
     *
     * @param user the domain user entity
     * @return the web response DTO
     */
    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
