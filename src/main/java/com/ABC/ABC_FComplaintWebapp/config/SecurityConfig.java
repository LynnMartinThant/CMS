package com.ABC.ABC_FComplaintWebapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * This configuration provides:
 * - BCrypt password encoder with strength factor 12
 

 * This bean is injected into UserService:
 * - registerUser() → passwordEncoder.encode(plainPassword) → BCrypt hash stored
 * - authenticateUser() → passwordEncoder.matches(raw, hash) → constant-time verification
 */
@Configuration
public class SecurityConfig {

    /**
     * Higher strength = slower encoding = better against brute force
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength factor 12: balanced security and performance
        return new BCryptPasswordEncoder(13);
    }

    /**
     * Support HTTP method override (for REST compatibility)
     */
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}
