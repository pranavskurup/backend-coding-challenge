package com.movie.rating.system.infrastructure.outbound.security;

import com.movie.rating.system.domain.port.outbound.PasswordHashingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * BCrypt implementation of the PasswordHashingService.
 * Provides secure password hashing and verification using BCrypt algorithm.
 */
@Slf4j
@Service
public class BCryptPasswordHashingService implements PasswordHashingService {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHashingService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12); // Strength 12 for good security
        log.info("Initialized BCryptPasswordHashingService with strength 12");
    }

    /**
     * Hashes a plain text password using BCrypt.
     *
     * @param plainTextPassword the password to hash
     * @return the hashed password
     * @throws IllegalArgumentException if password is null, empty, or exceeds 72 bytes
     */
    @Override
    public String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // BCrypt has a limitation of 72 bytes for password length
        byte[] passwordBytes = plainTextPassword.getBytes();
        if (passwordBytes.length > 72) {
            throw new IllegalArgumentException("Password cannot be more than 72 bytes");
        }

        log.debug("Hashing password");
        String hashedPassword = passwordEncoder.encode(plainTextPassword);
        log.debug("Password hashed successfully");
        
        return hashedPassword;
    }

    /**
     * Verifies if a plain text password matches a hashed password.
     *
     * @param plainTextPassword the plain text password
     * @param hashedPassword the hashed password
     * @return true if passwords match, false otherwise
     * @throws IllegalArgumentException if either parameter is null or empty
     */
    @Override
    public boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Plain text password cannot be null or empty");
        }
        
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }

        log.debug("Verifying password");
        boolean matches = passwordEncoder.matches(plainTextPassword, hashedPassword);
        log.debug("Password verification result: {}", matches);
        
        return matches;
    }
}
