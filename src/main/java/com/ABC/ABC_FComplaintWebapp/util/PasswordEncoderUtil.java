package com.ABC.ABC_FComplaintWebapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * SECURITY FIX_001: Password Encoding Service using BCrypt
 * Weakness ID: Wk_001
 * Fix ID: Fix_001 – Secure Password Hashing using BCrypt with Salting and Adaptive Work Factor
 * STRIDE: Tampering, Information Disclosure
 * OWASP: A02 Cryptographic Failures
 * CWE: CWE-326, CWE-327
 * CIA: Confidentiality, Integrity
 * ASVS: V2.2 – Password Storage, V6.2 – Cryptography
 * D3FEND: D3-PH Password Hashing
 *
 * Implementation Details:
 * - Uses BCrypt with strength factor of 12
 * - Automatically generates and embeds random salt
 * - Adaptive work factor increases with computational power
 * - Hash format: $2a$12$[22-character salt][31-character hash]
 * - BCrypt is intentionally slow (100+ ms per operation) to prevent brute forcing
 * - Compatible with Spring Security authentication mechanisms
 */
@Component
public class PasswordEncoderUtil {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * Encode plain text password using BCrypt
     * - Generates random salt
     * - Applies adaptive hash with strength factor 12
     * - Returns hex-encoded hash safe for database storage
     */
    public static String encodePassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return passwordEncoder.encode(plainPassword);
    }

    /**
     * Verify plain text password against stored BCrypt hash
     * - Extracts salt from stored hash
     * - Re-hashes plain password with same salt
     * - Performs constant-time comparison to prevent timing attacks
     */
    public static boolean matchPassword(String plainPassword, String encodedPassword) {
        if (plainPassword == null || plainPassword.isEmpty() || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, encodedPassword);
    }

    /**
     * Check if password needs re-encoding (for future algorithm upgrades)
     */
    public static boolean upgradePassword(String encodedPassword) {
        return passwordEncoder.upgradeEncoding(encodedPassword);
    }

    /**
     * Hash a password with custom strength (not recommended - use default strength 12)
     */
    public static String encodePasswordWithStrength(String plainPassword, int strength) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (strength < 4 || strength > 31) {
            throw new IllegalArgumentException("Strength must be between 4 and 31");
        }
        BCryptPasswordEncoder customEncoder = new BCryptPasswordEncoder(strength);
        return customEncoder.encode(plainPassword);
    }
}
