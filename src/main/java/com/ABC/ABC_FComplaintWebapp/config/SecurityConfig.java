package com.ABC.ABC_FComplaintWebapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * SECURITY FIX_001: Spring Security Configuration for Password Encoding
 * 
 * Weakness ID: Wk_001
 * Fix ID: Fix_001 – BCrypt Password Encoder Bean Configuration
 * STRIDE: Spoofing (Primary), Information Disclosure (Secondary)
 * OWASP: A02 – Cryptographic Failures
 * CWE: CWE-522 – Insufficiently Protected Credentials
 * CIA: Confidentiality, Integrity
 * ASVS: V6.3, V6.7 – Authentication Security
 * D3FEND: D3-PH Password Hashing
 *
 * This configuration provides:
 * - BCrypt password encoder with strength factor 12
 * - Automatic salt generation (embedded in hash)
 * - Adaptive computational cost (prevents brute force)
 * - Integration point for UserService password encoding/verification
 * 
 * PROOF OF FIX_001:
 * This bean is injected into UserService:
 * - registerUser() → passwordEncoder.encode(plainPassword) → BCrypt hash stored
 * - authenticateUser() → passwordEncoder.matches(raw, hash) → constant-time verification
 */
@Configuration
public class SecurityConfig {

    /**
     * Spring Security Password Encoder Bean
     * Uses BCrypt with strength factor 12 (adjusts computational cost)
     * Strength factor range: 4-31 (default 10, recommended 12+)
     *
     * Encoding time estimates:
     * - Strength 10: ~10-15ms
 
     *
     * Higher strength = slower encoding = better against brute force
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength factor 12: balanced security and performance
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Support HTTP method override (for REST compatibility)
     */
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}
