package com.ABC.ABC_FComplaintWebapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ABC.ABC_FComplaintWebapp.model.AuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * SECURITY FIX_004: Audit Log Repository
 * Weakness ID: Wk_004
 * Fix ID: Fix_004 – Structured Audit Logging Repository
 * D3FEND: D3-LM Logging and Monitoring
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    List<AuditLog> findByActionTypeOrderByCreatedAtDesc(String actionType);
    
    List<AuditLog> findByTenantIdOrderByCreatedAtDesc(Integer tenantId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.tenantId = :tenantId AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findAuditLogsByTenantAndDateRange(
        @Param("tenantId") Integer tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.actionType = :actionType ORDER BY al.createdAt DESC")
    List<AuditLog> findAuditLogsByUserAndActionType(
        @Param("userId") Integer userId,
        @Param("actionType") String actionType
    );
    
    @Query("SELECT al FROM AuditLog al WHERE al.status = :status AND al.tenantId = :tenantId ORDER BY al.createdAt DESC")
    List<AuditLog> findFailedAuditLogs(
        @Param("status") String status,
        @Param("tenantId") Integer tenantId
    );
}
