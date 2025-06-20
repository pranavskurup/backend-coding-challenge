package com.movie.rating.system.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserBehaviorMethodTest {

    private User createTestUser() {
        return User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void shouldDeactivateActiveUser() {
        User user = createTestUser();
        assertTrue(user.isActive());

        User deactivatedUser = user.deactivate();

        assertFalse(deactivatedUser.isActive());
        assertNotNull(deactivatedUser.getDeactivatedAt());
        assertTrue(deactivatedUser.getUpdatedAt().isAfter(user.getUpdatedAt()));
        
        // Verify immutability
        assertTrue(user.isActive());
        assertNull(user.getDeactivatedAt());
    }

    @Test
    void shouldReturnSameInstanceWhenDeactivatingAlreadyInactiveUser() {
        User user = createTestUser().deactivate();
        assertFalse(user.isActive());

        User result = user.deactivate();

        assertSame(user, result);
    }

    @Test
    void shouldReactivateInactiveUser() {
        User user = createTestUser().deactivate();
        assertFalse(user.isActive());

        User reactivatedUser = user.reactivate();

        assertTrue(reactivatedUser.isActive());
        assertNull(reactivatedUser.getDeactivatedAt());
        assertTrue(reactivatedUser.getUpdatedAt().isAfter(user.getUpdatedAt()));
        
        // Verify immutability
        assertFalse(user.isActive());
        assertNotNull(user.getDeactivatedAt());
    }

    @Test
    void shouldReturnSameInstanceWhenReactivatingAlreadyActiveUser() {
        User user = createTestUser();
        assertTrue(user.isActive());

        User result = user.reactivate();

        assertSame(user, result);
    }

    @Test
    void shouldUpdateProfile() {
        User user = createTestUser();
        String newFirstName = "Jane";
        String newLastName = "Smith";

        User updatedUser = user.updateProfile(newFirstName, newLastName);

        assertEquals(newFirstName, updatedUser.getFirstName());
        assertEquals(newLastName, updatedUser.getLastName());
        assertTrue(updatedUser.getUpdatedAt().isAfter(user.getUpdatedAt()));
        
        // Verify immutability
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
    }

    @Test
    void shouldUpdateProfileWithNullValues() {
        User user = createTestUser();

        User updatedUser = user.updateProfile(null, null);

        assertNull(updatedUser.getFirstName());
        assertNull(updatedUser.getLastName());
        assertTrue(updatedUser.getUpdatedAt().isAfter(user.getUpdatedAt()));
    }

    @Test
    void shouldChangeEmail() {
        User user = createTestUser();
        String newEmail = "newemail@example.com";

        User updatedUser = user.changeEmail(newEmail);

        assertEquals(newEmail, updatedUser.getEmail());
        assertTrue(updatedUser.getUpdatedAt().isAfter(user.getUpdatedAt()));
        
        // Verify immutability
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenChangingEmailToNull() {
        User user = createTestUser();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            user.changeEmail(null);
        });

        assertEquals("New email cannot be null", exception.getMessage());
    }

    @Test
    void shouldChangePassword() {
        User user = createTestUser();
        String newPasswordHash = "newhashedpassword";

        User updatedUser = user.changePassword(newPasswordHash);

        assertEquals(newPasswordHash, updatedUser.getPasswordHash());
        assertTrue(updatedUser.getUpdatedAt().isAfter(user.getUpdatedAt()));
        
        // Verify immutability
        assertEquals("hashedpassword", user.getPasswordHash());
    }

    @Test
    void shouldThrowExceptionWhenChangingPasswordToNull() {
        User user = createTestUser();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            user.changePassword(null);
        });

        assertEquals("New password hash cannot be null", exception.getMessage());
    }

    @Test
    void shouldReturnTrueForCanPerformActionsWhenUserIsActive() {
        User user = createTestUser();
        assertTrue(user.isActive());

        assertTrue(user.canPerformActions());
    }

    @Test
    void shouldReturnFalseForCanPerformActionsWhenUserIsInactive() {
        User user = createTestUser().deactivate();
        assertFalse(user.isActive());

        assertFalse(user.canPerformActions());
    }

    @Test
    void shouldReturnFullNameWhenBothFirstAndLastNamePresent() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .build();

        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void shouldReturnFirstNameWhenOnlyFirstNamePresent() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("John")
                .build();

        assertEquals("John", user.getFullName());
    }

    @Test
    void shouldReturnLastNameWhenOnlyLastNamePresent() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .lastName("Doe")
                .build();

        assertEquals("Doe", user.getFullName());
    }

    @Test
    void shouldReturnUsernameWhenNoFirstOrLastName() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        assertEquals("testuser", user.getFullName());
    }

    @Test
    void shouldReturnUsernameWhenBothNamesAreNull() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName(null)
                .lastName(null)
                .build();

        assertEquals("testuser", user.getFullName());
    }
}
