package com.movie.rating.system.domain.entity;

import com.movie.rating.system.domain.exception.InvalidEmailException;
import com.movie.rating.system.domain.exception.InvalidUsernameException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"passwordHash"}) // Exclude sensitive data from toString
public class User {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    @Builder.Default
    private final boolean isActive = true;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant deactivatedAt;

    /**
     * Custom builder to handle validation and defaults
     */
    public static UserBuilder builder() {
        return new UserBuilder() {
            private static final Pattern EMAIL_PATTERN = Pattern.compile(
                    "^[a-zA-Z0-9]([a-zA-Z0-9._+%-]*[a-zA-Z0-9])?@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
            );
            private static final int USERNAME_MIN_LENGTH = 3;
            private static final int USERNAME_MAX_LENGTH = 100;

            public User build() {
                if (super.createdAt == null) {
                    super.createdAt = Instant.now();
                }
                if (super.updatedAt == null) {
                    super.updatedAt = super.createdAt;
                }
                if (super.id == null) {
                    super.id = UUID.randomUUID();
                }
                validateEmail(super.email);
                validateUsername(super.username);
                // Validate required fields
                Objects.requireNonNull(super.id, "User ID cannot be null");
                Objects.requireNonNull(super.username, "Username cannot be null");
                Objects.requireNonNull(super.email, "Email cannot be null");
                Objects.requireNonNull(super.passwordHash, "Password hash cannot be null");
                Objects.requireNonNull(super.createdAt, "Created at cannot be null");
                Objects.requireNonNull(super.updatedAt, "Updated at cannot be null");
                return super.build();
            }

            private void validateUsername(String value) {
                if (value == null || value.trim().isEmpty()) {
                    throw new InvalidUsernameException("Username cannot be null or empty");
                }

                String trimmedValue = value.trim();
                if (trimmedValue.length() < USERNAME_MIN_LENGTH || trimmedValue.length() > USERNAME_MAX_LENGTH) {
                    throw new InvalidUsernameException(
                            String.format("Username must be between %d and %d characters", USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH));
                }

                // Allow alphanumeric characters, underscores, and hyphens
                if (!trimmedValue.matches("^[a-zA-Z0-9_-]+$")) {
                    throw new InvalidUsernameException("Username can only contain letters, numbers, underscores, and hyphens");
                }
            }

            private void validateEmail(String value) {
                if (value == null || value.trim().isEmpty()) {
                    throw new InvalidEmailException("Email cannot be null or empty");
                }
                String trimmedValue = value.trim().toLowerCase();

                // Check for consecutive dots
                if (trimmedValue.contains("..")) {
                    throw new InvalidEmailException("Invalid email format: " + value);
                }

                // Additional check: ensure domain has at least one dot (for TLD)
                int atIndex = trimmedValue.indexOf('@');
                if (atIndex != -1 && atIndex < trimmedValue.length() - 1) {
                    String domain = trimmedValue.substring(atIndex + 1);
                    if (!domain.contains(".")) {
                        throw new InvalidEmailException("Invalid email format: " + value);
                    }
                }

                if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
                    throw new InvalidEmailException("Invalid email format: " + value);
                }
            }
        };
    }

    // Domain behavior methods

    /**
     * Deactivates the user account
     */
    public User deactivate() {
        if (!isActive) {
            return this; // Already deactivated
        }

        return this.toBuilder()
                .isActive(false)
                .deactivatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Reactivates the user account
     */
    public User reactivate() {
        if (isActive) {
            return this; // Already active
        }

        return this.toBuilder()
                .isActive(true)
                .deactivatedAt(null)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Updates user profile information
     */
    public User updateProfile(String firstName, String lastName) {
        return this.toBuilder()
                .firstName(firstName)
                .lastName(lastName)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Changes user email
     */
    public User changeEmail(String newEmail) {
        Objects.requireNonNull(newEmail, "New email cannot be null");

        return this.toBuilder()
                .email(newEmail)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Changes user password
     */
    public User changePassword(String newPasswordHash) {
        Objects.requireNonNull(newPasswordHash, "New password hash cannot be null");

        return this.toBuilder()
                .passwordHash(newPasswordHash)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Checks if the user can perform actions
     */
    public boolean canPerformActions() {
        return isActive;
    }

    /**
     * Gets the full name of the user
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }

        StringBuilder fullName = new StringBuilder();
        if (firstName != null) {
            fullName.append(firstName);
        }
        if (lastName != null) {
            if (!fullName.isEmpty()) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }

        return fullName.toString();
    }
}
