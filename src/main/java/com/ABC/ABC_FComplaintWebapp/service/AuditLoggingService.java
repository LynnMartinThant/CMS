package com.ABC.ABC_FComplaintWebapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ABC.ABC_FComplaintWebapp.model.AuditLog;
import com.ABC.ABC_FComplaintWebapp.repositories.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

/**
 * SECURITY FIX_004: Audit Logging Service
 * Weakness ID: Wk_004
 * Fix ID: Fix_004 – Structured Audit Logging for All CRUD and Privileged Actions
 * STRIDE: Repudiation, Tampering
 * OWASP: A09 Logging and Monitoring, A06 Authorization
 * CWE: CWE-778
 * CIA: Integrity, Accountability
 * ASVS: V7 – Logging & Monitoring
 * D3FEND: D3-LM Logging and Monitoring
 *
 * Implementation Details:
 * - Records all CRUD operations (Create, Read, Update, Delete)
 * - Captures privileged/admin actions
 * - Logs: user ID, tenant ID, action type, target resource, timestamp, outcome
 * - Includes IP address for network-level tracking
 * - Immutable audit log (no modifications after creation)
 * - Indexed for efficient queries and reporting
 * - Can be extended for integration with SIEM systems
 */
@Service
public class AuditLoggingService {

    private static final Logger logger = Logger.getLogger(AuditLoggingService.class.getName());

    @Autowired
    private AuditLogRepository auditLogRepository; 
     * Log a generic action with all details
     */
    public AuditLog logAction(Integer userId, Integer tenantId, String actionType,
                             String resourceType, UUID targetResourceId,
                             String description, String status) {
        try {
            String ipAddress = getClientIpAddress();

            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setTenantId(tenantId);
            auditLog.setActionType(actionType);
            auditLog.setResourceType(resourceType);
            auditLog.setTargetResourceId(targetResourceId);
            auditLog.setDescription(description);
            auditLog.setStatus(status);
            auditLog.setIpAddress(ipAddress);

            return auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.warning(() -> "Failed to log action: " + e.getMessage());
            return null;
        }
    }

    /**
     * Log complaint creation (CREATE action)
     */
    public void logComplaintCreated(Integer userId, Integer tenantId, UUID complaintId, String description) { 
        logAction(userId, tenantId, "CREATE", "COMPLAINT", complaintId,
                 "Complaint created: " + description, "SUCCESS");
    }

    /**
     * Log complaint view/read (READ action)
     */
    public void logComplaintRead(Integer userId, Integer tenantId, UUID complaintId) { // Complaint read access SR_004

        logAction(userId, tenantId, "READ", "COMPLAINT", complaintId,
                 "Complaint accessed", "SUCCESS");
    }

    /**
     * Log complaint update (UPDATE action)
     */
    public void logComplaintUpdated(Integer userId, Integer tenantId, UUID complaintId, String updates) {
        logAction(userId, tenantId, "UPDATE", "COMPLAINT", complaintId,
                 "Complaint updated: " + updates, "SUCCESS");
    }

    /**
     * Log complaint deletion (DELETE action)
     */
    public void logComplaintDeleted(Integer userId, Integer tenantId, UUID complaintId) {
        logAction(userId, tenantId, "DELETE", "COMPLAINT", complaintId,
                 "Complaint deleted", "SUCCESS");
    }

    /**
     * Log admin response update
     */
    public void logAdminResponseUpdated(Integer userId, Integer tenantId, UUID complaintId, String newStatus) {
        logAction(userId, tenantId, "UPDATE", "COMPLAINT", complaintId,
                 "Admin response updated, status: " + newStatus, "SUCCESS");
    }

    /**
     * Log authentication attempt
     */
    public void logAuthenticationAttempt(String username, Boolean successful) {
        logAction(0, 0, "AUTHENTICATE", "USER", null,
                 "Login attempt for user: " + username, successful ? "SUCCESS" : "FAILURE");
    }

  


  

    /**
     * Get audit logs for a specific user
     */
    public List<AuditLog> getUserAuditLogs(Integer userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get all audit logs for a tenant
     */
    public List<AuditLog> getTenantAuditLogs(Integer tenantId) {
        return auditLogRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    /**
     * Get audit logs filtered by action type
     */
    public List<AuditLog> getAuditLogsByActionType(String actionType) {
        return auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType);
    }

    /**
     * Get audit logs for a date range
     */
    public List<AuditLog> getAuditLogsByDateRange(Integer tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findAuditLogsByTenantAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * Get failed operations for security monitoring
     */
    public List<AuditLog> getFailedOperations(Integer tenantId) {
        return auditLogRepository.findFailedAuditLogs("FAILURE", tenantId);
    }

   
}
