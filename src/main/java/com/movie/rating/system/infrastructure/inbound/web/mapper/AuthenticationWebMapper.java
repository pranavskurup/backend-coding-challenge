package com.movie.rating.system.infrastructure.inbound.web.mapper;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.domain.port.inbound.UserAuthenticationUseCase.AuthenticationCommand;
import com.movie.rating.system.infrastructure.inbound.web.dto.request.LoginRequestDto;
import com.movie.rating.system.infrastructure.inbound.web.dto.response.AuthenticationResponseDto;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Mapper for authentication-related DTOs and domain objects
 */
@Component
public class AuthenticationWebMapper {

    /**
     * Map login request DTO to authentication command
     */
    public AuthenticationCommand toCommand(LoginRequestDto requestDto) {
        return new AuthenticationCommand(
                requestDto.usernameOrEmail(),
                requestDto.password()
        );
    }

    /**
     * Map user entity to authentication response DTO
     */
    public AuthenticationResponseDto toAuthenticationResponse(User user, String accessToken, 
                                                            String refreshToken, Duration tokenDuration) {
        AuthenticationResponseDto.UserInfo userInfo = new AuthenticationResponseDto.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                user.getCreatedAt()
        );

        return AuthenticationResponseDto.of(
                accessToken,
                refreshToken,
                tokenDuration.toSeconds(),
                userInfo
        );
    }

    /**
     * Map refresh token response
     */
    public AuthenticationResponseDto toRefreshResponse(User user, String newAccessToken, 
                                                     String refreshToken, Duration tokenDuration) {
        return toAuthenticationResponse(user, newAccessToken, refreshToken, tokenDuration);
    }
}
