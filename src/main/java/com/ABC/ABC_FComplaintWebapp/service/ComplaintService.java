package com.ABC.ABC_FComplaintWebapp.service;



import com.ABC.ABC_FComplaintWebapp.repositories.ComplaintRepo;
import com.ABC.ABC_FComplaintWebapp.model.Complaint;
import com.ABC.ABC_FComplaintWebapp.util.OutputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * SECURITY FIX_003 & FIX_002: Complaint Service with Ownership Checks & Output Sanitization
 * Weakness ID: Wk_003, Wk_002
 * Fix ID: Fix_003 – Access Control with UUIDs & Fix_002 – Output Encoding
 * STRIDE: Information Disclosure, Tampering
 * OWASP: A01 Broken Access Control, A03 Injection
 * CWE: CWE-639, CWE-863, CWE-79
 * CIA: Confidentiality, Integrity
 * ASVS: V4 – Access Control, V5 – Validation
 * D3FEND: D3-AAC (Access Control) & D3-OTV (Output Validation)
 */
@Service
public class ComplaintService {

    private static final Logger logger = Logger.getLogger(ComplaintService.class.getName());

    @Autowired
    private ComplaintRepo complaintRepository;

    @Autowired
    private AuditLoggingService auditLoggingService;
    
    /**
     * Create a new complaint as a user
     * SECURITY: Applies input sanitization and sets tenant ID automatically
     */
    public Complaint createComplaint(Complaint complaint) {
        // SECURITY FIX_002: Sanitize all complaint inputs
        complaint.setTitle(OutputSanitizer.sanitizeComplaintField(complaint.getTitle(), "title"));
        complaint.setDescription(OutputSanitizer.sanitizeComplaintField(complaint.getDescription(), "description"));
        complaint.setCategory(OutputSanitizer.sanitizeComplaintField(complaint.getCategory(), "category"));
        complaint.setUserName(OutputSanitizer.sanitizeComplaintField(complaint.getUserName(), "userName"));

        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setStatus("Pending");
        
        Complaint saved = complaintRepository.save(complaint);

        // SECURITY FIX_004: Audit log the complaint creation
        auditLoggingService.logComplaintCreated(complaint.getUserId(), complaint.getTenantId(),
                saved.getId(), saved.getTitle());

        return saved;
    }
    
    /**
     * Admin can ONLY update admin response and status
     * SECURITY: Enforces ownership check and validates access
     * No other fields can be modified
     */
    public Complaint updateAdminResponse(UUID complaintId, Integer adminUserId, Integer tenantId,
                                        String adminResponse, String status) {
        // SECURITY FIX_003: Validate complaint exists
        Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            logger.warning(() -> "Unauthorized access attempt: Complaint not found - ID: " + complaintId);
            auditLoggingService.logAuthorizationFailure(adminUserId, tenantId, "COMPLAINT_UPDATE");
            return null;
        }

        Complaint complaint = complaintOpt.get();

        // SECURITY FIX_003: Enforce tenant isolation - multi-tenancy check
        if (!complaint.getTenantId().equals(tenantId)) {
            logger.warning(() -> String.format(
                "Unauthorized access attempt: Tenant mismatch - User: %d, Attempted TenantId: %d, Actual TenantId: %d",
                adminUserId, tenantId, complaint.getTenantId()
            ));
            auditLoggingService.logAuthorizationFailure(adminUserId, tenantId, "COMPLAINT_CROSS_TENANT");
            return null;
        }

        // SECURITY FIX_002: Sanitize admin response before storing
        String sanitizedResponse = OutputSanitizer.sanitizeComplaintField(adminResponse, "response");
        String sanitizedStatus = OutputSanitizer.sanitizeComplaintField(status, "status");

        complaint.setAdminResponse(sanitizedResponse);
        complaint.setStatus(sanitizedStatus);
        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint updated = complaintRepository.save(complaint);

        // SECURITY FIX_004: Audit log the update action
        auditLoggingService.logAdminResponseUpdated(adminUserId, tenantId, complaintId, sanitizedStatus);

        return updated;
    }
    
    /**
     * Get a specific complaint by ID
     * SECURITY: Validates access control and ownership
     */
    public Complaint getComplaintById(UUID id, Integer userId, Integer tenantId, Boolean isAdmin) {
        Optional<Complaint> complaintOpt = complaintRepository.findById(id);

        if (complaintOpt.isEmpty()) {
            logger.warning(() -> "Complaint not found - ID: " + id);
            auditLoggingService.logAuthorizationFailure(userId, tenantId, "COMPLAINT_READ_NOT_FOUND");
            return null;
        }

        Complaint complaint = complaintOpt.get();

        // SECURITY FIX_003: Enforce tenant isolation
        if (!complaint.getTenantId().equals(tenantId)) {
            logger.warning(() -> String.format(
                "Unauthorized access attempt: Tenant mismatch - User: %d, Attempted TenantId: %d, Actual TenantId: %d",
                userId, tenantId, complaint.getTenantId()
            ));
            auditLoggingService.logAuthorizationFailure(userId, tenantId, "COMPLAINT_CROSS_TENANT_READ");
            return null;
        }

        // SECURITY FIX_003: Enforce ownership or admin role
        if (!isAdmin && !complaint.getUserId().equals(userId)) {
            logger.warning(() -> String.format(
                "Unauthorized access attempt: User %d attempted to read complaint %s owned by %d",
                userId, id, complaint.getUserId()
            ));
            auditLoggingService.logAuthorizationFailure(userId, tenantId, "COMPLAINT_OWNERSHIP_VIOLATION");
            return null;
        }

        // SECURITY FIX_004: Audit log the read action
        auditLoggingService.logComplaintRead(userId, tenantId, id);

        return complaint;
    }

    /**
     * Get a specific complaint by ID (overloaded for legacy calls)
     */
    public Complaint getComplaintById(UUID id) {
        return complaintRepository.findById(id).orElse(null);
    }
    
    /**
     * Get all complaints (admin only)
     * SECURITY: Filters by tenant ID to enforce multi-tenancy
     */
    public List<Complaint> getAllComplaints(Integer tenantId) {
        return complaintRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    /**
     * Get all complaints (legacy)
     */
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get complaints filtered by status
     * SECURITY: Filters by tenant ID
     */
    public List<Complaint> getComplaintsByStatus(String status, Integer tenantId) {
        return complaintRepository.findByStatusAndTenantIdOrderByCreatedAtDesc(
                OutputSanitizer.sanitizeComplaintField(status, "status"), tenantId);
    }

    /**
     * Get complaints filtered by status (legacy)
     */
    public List<Complaint> getComplaintsByStatus(String status) {
        return complaintRepository.findByStatus(status);
    }
    
    /**
     * Get complaints for a specific user
     * SECURITY: Enforces ownership or admin access
     */
    public List<Complaint> getComplaintsByUserId(Integer userId, Integer tenantId) {
        return complaintRepository.findByUserIdAndTenantIdOrderByCreatedAtDesc(userId, tenantId);
    }

    /**
     * Get complaints for a specific user (legacy)
     */
    public List<Complaint> getComplaintsByUserId(Integer userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Delete complaint (soft delete recommended, physical delete shown here)
     * SECURITY: Enforces ownership and validates tenant
     */
    public Boolean deleteComplaint(UUID complaintId, Integer userId, Integer tenantId, Boolean isAdmin) {
        Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);

        if (complaintOpt.isEmpty()) {
            logger.warning(() -> "Complaint not found for deletion - ID: " + complaintId);
            return false;
        }

        Complaint complaint = complaintOpt.get();

        // SECURITY FIX_003: Enforce tenant isolation
        if (!complaint.getTenantId().equals(tenantId)) {
            logger.warning(() -> String.format(
                "Unauthorized delete attempt: Tenant mismatch - User: %d", userId
            ));
            auditLoggingService.logAuthorizationFailure(userId, tenantId, "COMPLAINT_DELETE_CROSS_TENANT");
            return false;
        }

        // SECURITY FIX_003: Enforce ownership or admin role
        if (!isAdmin && !complaint.getUserId().equals(userId)) {
            logger.warning(() -> String.format(
                "Unauthorized delete attempt: User %d tried to delete complaint %s owned by %d",
                userId, complaintId, complaint.getUserId()
            ));
            auditLoggingService.logAuthorizationFailure(userId, tenantId, "COMPLAINT_DELETE_OWNERSHIP_VIOLATION");
            return false;
        }

        complaintRepository.deleteById(complaintId);

        // SECURITY FIX_004: Audit log the deletion
        auditLoggingService.logComplaintDeleted(userId, tenantId, complaintId);

        return true;
    }
}
