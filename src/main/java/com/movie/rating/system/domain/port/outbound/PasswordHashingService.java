package com.movie.rating.system.domain.port.outbound;

/**
 * Port for password hashing operations
 */
public interface PasswordHashingService {
    
    /**
     * Hash a plain text password
     * @param plainTextPassword the password to hash
     * @return the hashed password
     */
    String hashPassword(String plainTextPassword);
    
    /**
     * Verify if a plain text password matches a hashed password
     * @param plainTextPassword the plain text password
     * @param hashedPassword the hashed password
     * @return true if passwords match, false otherwise
     */
    boolean verifyPassword(String plainTextPassword, String hashedPassword);
}
