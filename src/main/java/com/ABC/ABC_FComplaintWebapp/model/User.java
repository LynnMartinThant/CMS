package com.ABC.ABC_FComplaintWebapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * SECURITY FIX_001: User Model with Secure Password Hashing
 * 
 * Weakness ID: Wk_001
 * Fix ID: Fix_001 – Secure Password Hashing using BCrypt
 * STRIDE: Spoofing (Primary), Information Disclosure (Secondary)
 * OWASP: A02 – Cryptographic Failures
 * CWE: CWE-522 – Insufficiently Protected Credentials
 * CIA: Confidentiality, Integrity
 * ASVS: V6.3, V6.7 – Authentication Security
 * D3FEND: D3-PH Password Hashing
 *
 * Implementation Details:
 * - Hash format: $2a$12$[22-char-salt][31-char-hash]
 * - Salting makes rainbow table attacks ineffective
 * - Adaptive work factor increases with computational improvements
 * 
 * Password Storage Contract:

 * ✓ Plain text: NEVER stored, NEVER logged, NEVER in memory longer than necessary
)
 * 
 * Proof of Implementation:
 * 1. SecurityConfig.java → BCryptPasswordEncoder(12) bean configuration
 * 2. UserService.java → registerUser() uses encoder.encode(plainPassword)
 * 3. UserService.java → authenticateUser() uses encoder.matches(raw, hash)
 * 

 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_username", columnList = "username", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Username is required")
    private String username;

    @Column(nullable = false, unique = true)
    @Email(message = "Email should be valid")
    private String email;

    /**
     * SECURITY: Password field stores BCrypt-hashed passwords only.
     */
    @Column(nullable = false, length = 70)
    @NotBlank(message = "Password is required") // hash
    private String hashedPassword;

    @Column(nullable = false)
    private String role = "USER"; // role based 

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;



    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false; 

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public User() {}

    public User(String username, String email, String hashedPassword) {
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
        this.passwordLastChanged = LocalDateTime.now();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getPasswordLastChanged() {
        return passwordLastChanged;
    }

    public void setPasswordLastChanged(LocalDateTime passwordLastChanged) {
        this.passwordLastChanged = passwordLastChanged;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
