package com.ABC.ABC_FComplaintWebapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ABC.ABC_FComplaintWebapp.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * SECURITY FIX_001: User Repository with Password Security
 * Weakness ID: Wk_001
 * Fix ID: Fix_001 – User Repository for BCrypt Password Management
 * D3FEND: D3-PH Password Hashing
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameAndActiveTrue(String username);
}
