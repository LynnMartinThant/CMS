package com.ABC.ABC_FComplaintWebapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ABC.ABC_FComplaintWebapp.model.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByUsername(String username);
    Optional <User> findByID(Long ID);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameAndActiveTrue(String username);
}
