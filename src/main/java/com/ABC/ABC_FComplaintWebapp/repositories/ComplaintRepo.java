package com.ABC.ABC_FComplaintWebapp.repositories;






import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ABC.ABC_FComplaintWebapp.model.Complaint;

import java.util.List;
import java.util.UUID;

/**
 * SECURITY FIX_003: Complaint Repository with Tenant-Aware Queries
 * Weakness ID: Wk_003
 * Fix ID: Fix_003 – UUID-based IDs and Multi-Tenancy Enforcement
 * D3FEND: D3-AAC Attribute-Based Access Control
 */
@Repository
public interface ComplaintRepo extends JpaRepository<Complaint, UUID> {
    
    // Original methods for backward compatibility
    List<Complaint> findByStatus(String status);
    List<Complaint> findByUserId(Integer userId);
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Complaint> findAllByOrderByCreatedAtDesc();

    // Tenant-aware queries for security
    List<Complaint> findByTenantIdOrderByCreatedAtDesc(Integer tenantId);
    
    List<Complaint> findByStatusAndTenantIdOrderByCreatedAtDesc(String status, Integer tenantId);
    
    List<Complaint> findByUserIdAndTenantIdOrderByCreatedAtDesc(Integer userId, Integer tenantId);
    
    @Query("SELECT c FROM Complaint c WHERE c.tenantId = :tenantId AND c.userId = :userId ORDER BY c.createdAt DESC")
    List<Complaint> findUserComplaints(@Param("tenantId") Integer tenantId, @Param("userId") Integer userId);
}
