package com.movie.rating.system.infrastructure.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC entity representing the users table in the database.
 * This entity is mapped to the "users" table and is used for persistence operations.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {

    /**
     * Unique identifier for the user
     */
    @Id
    @Column("id")
    private UUID id;

    /**
     * Unique username for the user
     */
    @Column("username")
    private String username;

    /**
     * Unique email address for the user
     */
    @Column("email")
    private String email;

    /**
     * Hashed password for the user
     */
    @Column("password_hash")
    private String passwordHash;

    /**
     * First name of the user
     */
    @Column("first_name")
    private String firstName;

    /**
     * Last name of the user
     */
    @Column("last_name")
    private String lastName;

    /**
     * Indicates if the user account is active
     */
    @Builder.Default
    @Column("is_active")
    private Boolean isActive = true;

    /**
     * Timestamp when the user was created
     */
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    /**
     * Timestamp when the user was last updated
     */
    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    /**
     * Timestamp when the user was deactivated (null if active)
     */
    @Column("deactivated_at")
    private Instant deactivatedAt;
}
