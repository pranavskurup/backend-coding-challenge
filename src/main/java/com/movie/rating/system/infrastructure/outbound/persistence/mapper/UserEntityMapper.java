package com.movie.rating.system.infrastructure.outbound.persistence.mapper;

import com.movie.rating.system.domain.entity.User;
import com.movie.rating.system.infrastructure.outbound.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between domain User and persistence UserEntity.
 * This mapper handles the conversion of data between the domain layer
 * and the persistence layer following hexagonal architecture principles.
 */
@Component
public class UserEntityMapper {

    /**
     * Converts a domain User entity to a persistence UserEntity.
     *
     * @param user the domain user entity
     * @return the persistence user entity
     */
    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deactivatedAt(user.getDeactivatedAt())
                .build();
    }

    /**
     * Converts a persistence UserEntity to a domain User entity.
     *
     * @param userEntity the persistence user entity
     * @return the domain user entity
     */
    public User toDomain(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        return User.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .passwordHash(userEntity.getPasswordHash())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .isActive(Boolean.TRUE.equals(userEntity.getIsActive()))
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .deactivatedAt(userEntity.getDeactivatedAt())
                .build();
    }
}
