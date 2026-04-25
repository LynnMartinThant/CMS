package com.ABC.ABC_FComplaintWebapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SECURITY FIX_004: Audit Logging Model
 * Weakness ID: Wk_004
 * Fix ID: Fix_004 – Structured Audit Logging
 * STRIDE: Repudiation, Tampering
 * OWASP: A09 Logging and Monitoring, A06 Authorization
 * CWE: CWE-778
 * CIA: Integrity, Accountability
 * ASVS: V7 – Logging & Monitoring
 * D3FEND: D3-LM Logging and Monitoring
 */

/*
**Audit Logging Points:**
```java
// Complaint creation
auditLoggingService.logComplaintCreated(userId, tenantId, complaintId, "Complaint: Network Issue");

// Complaint read access
auditLoggingService.logComplaintRead(userId, tenantId, complaintId);

// Complaint modified
auditLoggingService.logComplaintUpdated(userId, tenantId, complaintId, "Status changed to In Process");

// Admin response added
auditLoggingService.logAdminResponseUpdated(adminId, tenantId, complaintId, "Closed");

// Authorization failure
auditLoggingService.logAuthorizationFailure(userId, tenantId, "COMPLAINT_READ_CROSS_TENANT");

// Login attempt
auditLoggingService.logAuthenticationAttempt("admin", true); // Success
auditLoggingService.logAuthenticationAttempt("admin", false); // Failed

// Data export
auditLoggingService.logDataExport(adminId, tenantId, "CSV", 150);
```

**Database Schema:**
```sql
CREATE TABLE audit_logs (
    id BINARY(16) NOT NULL,
    user_id INT NOT NULL,
    tenant_id INT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    target_resource_id BINARY(16),
    description TEXT,
    status VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45),
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_created_at (created_at),
    INDEX idx_tenant_id (tenant_id)
);
```

*/
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_action_type", columnList = "action_type"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "action_type", nullable = false)
    private String actionType; // CREATE, READ, UPDATE, DELETE, AUTHENTICATE, AUTHORIZE

    @Column(name = "resource_type", nullable = false)
    private String resourceType; // COMPLAINT, USER, SYSTEM

    @Column(name = "target_resource_id")
    private UUID targetResourceId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    private String status; // SUCCESS, FAILURE

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public AuditLog() {}

    public AuditLog(Integer userId, Integer tenantId, String actionType, String resourceType,
                    UUID targetResourceId, String description, String status, String ipAddress) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.targetResourceId = targetResourceId;
        this.description = description;
        this.status = status;
        this.ipAddress = ipAddress;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public UUID getTargetResourceId() {
        return targetResourceId;
    }

    public void setTargetResourceId(UUID targetResourceId) {
        this.targetResourceId = targetResourceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
