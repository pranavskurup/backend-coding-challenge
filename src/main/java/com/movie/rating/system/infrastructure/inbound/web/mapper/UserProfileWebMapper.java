package com.movie.rating.system.infrastructure.inbound.web.mapper;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.port.inbound.ManageUserProfileUseCase.ChangePasswordCommand;
import com.movie.rating.system.domain.port.inbound.ManageUserProfileUseCase.UpdateUserProfileCommand;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.ChangePasswordRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.UpdateUserProfileRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.UserProfileResponseDto;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for converting between DTOs and domain objects for user profile operations.
 */
@Component
public class UserProfileWebMapper {

    /**
     * Converts UpdateUserProfileRequestDto to UpdateUserProfileCommand.
     *
     * @param userId the user ID
     * @param requestDto the request DTO
     * @return UpdateUserProfileCommand
     */
    public UpdateUserProfileCommand toCommand(UUID userId, UpdateUserProfileRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        
        return new UpdateUserProfileCommand(
                userId,
                requestDto.email(),
                requestDto.firstName(),
                requestDto.lastName()
        );
    }

    /**
     * Converts ChangePasswordRequestDto to ChangePasswordCommand.
     *
     * @param userId the user ID
     * @param requestDto the request DTO
     * @return ChangePasswordCommand
     */
    public ChangePasswordCommand toCommand(UUID userId, ChangePasswordRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        
        return new ChangePasswordCommand(
                userId,
                requestDto.currentPassword(),
                requestDto.newPassword()
        );
    }

    /**
     * Converts User entity to UserProfileResponseDto.
     *
     * @param user the user entity
     * @return UserProfileResponseDto
     */
    public UserProfileResponseDto toProfileResponseDto(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserProfileResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeactivatedAt()
        );
    }
}
