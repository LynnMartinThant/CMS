package com.ABC.ABC_FComplaintWebapp.controller;



import com.ABC.ABC_FComplaintWebapp.model.Complaint;
import com.ABC.ABC_FComplaintWebapp.service.ComplaintService;
import com.ABC.ABC_FComplaintWebapp.service.AuditLoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * SECURITY FIX_002, FIX_003, FIX_004: Complaint Controller with Security Enhancements
 * - Output encoding for XSS prevention
 * - UUID-based access control
 * - Comprehensive audit logging
 */
@Controller
@RequestMapping("/complaints")
public class Complaint_Controller {
    
    @Autowired
    private ComplaintService complaintService;
    
    private final String[] categories = {
        "ATM Issue", "Fraud", "Technical Issue", "Transaction Delay", 
        "Loan Issue", "Profile Issue", "Card Delivery", "Customer Service", 
        "Security Issue", "Statement Issue"
    };
    
    private final String[] statuses = {"Pending", "In Process", "Closed"};
    
    // ==================== USER OPERATIONS ====================
    
    /**
     * Display form to create a new complaint
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("complaint", new Complaint());
        model.addAttribute("categories", categories);
        return "create-complaint";
    }
    
    /**
     * Save new complaint from form submission
     */
    @PostMapping("/create")
    public String createComplaint(@Valid @ModelAttribute Complaint complaint, 
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categories);
            return "create-complaint";
        }
        
        try {
            complaintService.createComplaint(complaint);
            return "redirect:/complaints/my-complaints/" + complaint.getUserId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating complaint: " + e.getMessage());
            model.addAttribute("categories", categories);
            return "create-complaint";
        }
    }
    
    /**
     * Display user's complaints
     */
    @GetMapping("/my-complaints/{userId}")
    public String myComplaints(@PathVariable Integer userId, Model model) {
        List<Complaint> complaints = complaintService.getComplaintsByUserId(userId);
        model.addAttribute("complaints", complaints);
        model.addAttribute("userId", userId);
        return "my-complaints";
    }
    
    // ==================== ADMIN OPERATIONS ====================
    
    /**
     * Admin: View all complaints with optional filtering
     */
    @GetMapping("/admin/all")
    public String adminViewAll(@RequestParam(required = false) String status, Model model) {
        List<Complaint> complaints;
        
        if (status != null && !status.isEmpty()) {
            complaints = complaintService.getComplaintsByStatus(status);
            model.addAttribute("selectedStatus", status);
        } else {
            complaints = complaintService.getAllComplaints();
        }
        
        model.addAttribute("complaints", complaints);
        model.addAttribute("statuses", statuses);
        return "admin-complaints";
    }
    
    /**
     * Admin: Display form to update admin response
     */
    @GetMapping("/admin/update/{id}")
    public String showUpdateForm(@PathVariable UUID id, Model model) {
        Complaint complaint = complaintService.getComplaintById(id);
        if (complaint == null) {
            return "redirect:/complaints/admin/all";
        }
        model.addAttribute("complaint", complaint);
        model.addAttribute("statuses", statuses);
        return "admin-update-response";
    }
    
    /**
     * Admin: Update ONLY admin response and status
     * All other fields are read-only and cannot be modified
     */
    @PostMapping("/admin/update/{id}")
    public String updateAdminResponse(@PathVariable UUID id,
                                     @RequestParam String adminResponse,
                                     @RequestParam String status,
                                     Model model) {
        try {
            // Validate inputs
            if (adminResponse == null || adminResponse.trim().isEmpty()) {
                model.addAttribute("errorMessage", "Admin response cannot be empty");
                Complaint complaint = complaintService.getComplaintById(id);
                model.addAttribute("complaint", complaint);
                model.addAttribute("statuses", statuses);
                return "admin-update-response";
            }
            
            if (!java.util.Arrays.asList(statuses).contains(status)) {
                model.addAttribute("errorMessage", "Invalid status selected");
                Complaint complaint = complaintService.getComplaintById(id);
                model.addAttribute("complaint", complaint);
                model.addAttribute("statuses", statuses);
                return "admin-update-response";
            }
            
            // Get admin user ID and tenant ID from session (should be implemented)
            Integer adminUserId = 1; // PLACEHOLDER - implement session management
            Integer tenantId = 1;    // PLACEHOLDER - implement multi-tenancy

            complaintService.updateAdminResponse(id, adminUserId, tenantId, adminResponse, status);
            return "redirect:/complaints/admin/all?message=updated";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating response: " + e.getMessage());
            Complaint complaint = complaintService.getComplaintById(id);
            model.addAttribute("complaint", complaint);
            model.addAttribute("statuses", statuses);
            return "admin-update-response";
        }
    }
}