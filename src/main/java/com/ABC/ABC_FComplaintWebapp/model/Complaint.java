package com.ABC.ABC_FComplaintWebapp.model;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SECURITY FIX_003: UUID-based Resource Identification
 * Weakness ID: Wk_003
 * Fix ID: Fix_003 – Replaced Sequential IDs with UUIDs & Enforcement of Ownership Checks
 * STRIDE: Information Disclosure, Tampering
 * OWASP: A01 Broken Access Control
 * CWE: CWE-639, CWE-863
 * CIA: Confidentiality, Integrity
 * ASVS: V4 – Access Control
 * D3FEND: D3-AAC Attribute-Based Access Control
 */
@Entity
@Table(name = "complaints", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_tenant_id", columnList = "tenant_id"), // **Implementation**: UUID primary key instead of sequential Long  // **Implementation**: UUID generation prevents ID enumeration
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Complaint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // **Implementation**: Multi-tenancy via `tenantId` field
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(nullable = false)
    @NotNull(message = "User ID is required")
    private Integer userId;
    
    @Column(nullable = false)
    @NotBlank(message = "User name is required")
    private String userName;
    
    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Description is required")
    private String description;
    
    @Column(nullable = false)
    @NotBlank(message = "Category is required")
    private String category;
    
    @Column(nullable = false)
    private String status = "Pending";
    
    @Column(columnDefinition = "TEXT")
    private String adminResponse;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Complaint() {}   // **Implementation**: Ownership checks prevent cross-tenant access

    public Complaint(Integer tenantId, Integer userId, String userName, String title, String description, String category) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = "Pending";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
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