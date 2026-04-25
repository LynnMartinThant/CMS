# Security Implementation Documentation

## Overview
This document details all security enhancements implemented in the ABC Complaint Webapp.

---

## Fix_001: Secure Password Hashing using BCrypt

**Weakness ID:** Wk_001  
**STRIDE:** Tampering, Information Disclosure  
**OWASP:** A02 Cryptographic Failures  
**CWE:** CWE-326, CWE-327  
**CIA:** Confidentiality, Integrity  
**ASVS:** V2.2 - Password Storage  
**D3FEND:** D3-PH Password Hashing

### Implementation Details
- **Algorithm:** BCrypt with adaptive work factor
- **Strength Factor:** 12 (provides ~100-150ms per operation)
- **Salt:** Automatically generated and embedded in hash
- **Hash Format:** `$2a$12$[22-char-salt][31-char-hash]`

### Files Modified/Created
- `User.java` - User model with hashedPassword field
- `PasswordEncoderUtil.java` - Utility class for password encoding/verification
- `SecurityConfig.java` - Spring Security password encoder bean
- `UserRepository.java` - User persistence layer

### Usage Example
```java
// Encoding password
String hashedPassword = PasswordEncoderUtil.encodePassword("myPassword123");

// Verifying password
boolean matches = PasswordEncoderUtil.matchPassword("myPassword123", storedHash);
```

### Security Benefits
- Plain text passwords never stored
- Salting prevents rainbow table attacks
- Adaptive cost adjusts for future computational improvements
- Constant-time comparison prevents timing attacks

---

## Fix_002: Output Encoding and HTML Escaping

**Weakness ID:** Wk_002  
**STRIDE:** Tampering, Information Disclosure  
**OWASP:** A03 Injection, A07 Cross-Site Scripting (XSS)  
**CWE:** CWE-79, CWE-80  
**CIA:** Integrity, Confidentiality  
**ASVS:** V5.3 - Output Encoding  
**D3FEND:** D3-OTV Output Transformation Validation

### Implementation Details
- **Technology:** Apache Commons Text library
- **Escaping Methods:** HTML, JavaScript, URL, CSS, XML
- **Application Point:** All complaint fields before rendering
- **Context-Aware:** Encoding applies to appropriate context

### Files Modified/Created
- `OutputSanitizer.java` - Comprehensive sanitization utility
- `ComplaintService.java` - Integrated sanitization in service layer

### Escaping Methods Available
```java
// HTML escaping for web views
String safe = OutputSanitizer.escapeHtml(userInput);

// JavaScript escaping for inline scripts
String safe = OutputSanitizer.escapeJavaScript(userInput);

// URL escaping for href/src attributes
String safe = OutputSanitizer.escapeUrl(userInput);

// CSS escaping for style attributes
String safe = OutputSanitizer.escapeCss(userInput);

// Field-specific sanitization
String safe = OutputSanitizer.sanitizeComplaintField(input, "title");
```

### Protected Fields
- Complaint title (255 chars max)
- Complaint description (5000 chars max)
- Complaint category (100 chars max)
- Admin response (5000 chars max)
- Status (50 chars max)

### Security Benefits
- Prevents XSS injection attacks
- Neutralizes HTML/JavaScript payloads
- Safe for database and web display
- Context-appropriate encoding

---

## Fix_003: UUID-Based Resource IDs and Ownership Checks

**Weakness ID:** Wk_003  
**STRIDE:** Information Disclosure, Tampering  
**OWASP:** A01 Broken Access Control  
**CWE:** CWE-639, CWE-863  
**CIA:** Confidentiality, Integrity  
**ASVS:** V4 - Access Control  
**D3FEND:** D3-AAC Attribute-Based Access Control

### Implementation Details
- **ID Type:** Changed from `Long` (sequential) to `UUID` (non-sequential)
- **Multi-Tenancy:** Added `tenantId` field to all models
- **Ownership Validation:** Service layer enforces user ownership checks
- **Tenant Isolation:** Cross-tenant access attempts are logged and rejected

### Files Modified/Created
- `Complaint.java` - UUID-based identification with tenantId
- `AuditLog.java` - UUID-based audit records with tenantId
- `User.java` - UUID-based user identification
- `ComplaintService.java` - Ownership validation logic
- `ComplaintRepo.java` - Tenant-aware query methods

### Access Control Methods
```java
// Get complaint with ownership validation
Complaint complaint = complaintService.getComplaintById(
    complaintId, userId, tenantId, isAdmin
);

// Ownership check in update
complaintService.updateAdminResponse(
    complaintId, adminUserId, tenantId, response, status
);

// Tenant isolation enforced
// Returns null if:
// - User is not owner and not admin
// - Tenant ID doesn't match
// - Resource doesn't exist
```

### Query Methods with Tenant Filtering
```java
// Tenant-specific queries
findByTenantIdOrderByCreatedAtDesc(tenantId)
findByStatusAndTenantIdOrderByCreatedAtDesc(status, tenantId)
findByUserIdAndTenantIdOrderByCreatedAtDesc(userId, tenantId)
```

### Security Benefits
- UUID prevents sequential ID enumeration attacks
- Ownership checks prevent unauthorized resource access
- Tenant isolation prevents cross-customer data leakage
- Multi-tenancy support for future scaling

---

## Fix_004: Structured Audit Logging

**Weakness ID:** Wk_004  
**STRIDE:** Repudiation, Tampering  
**OWASP:** A09 Logging and Monitoring, A06 Authorization  
**CWE:** CWE-778  
**CIA:** Integrity, Accountability  
**ASVS:** V7 - Logging & Monitoring  
**D3FEND:** D3-LM Logging and Monitoring

### Implementation Details
- **Immutable Logs:** Audit records never modified after creation
- **Captured Fields:** User ID, Tenant ID, Action Type, Resource Type, Target Resource, IP Address, Timestamp, Outcome
- **Database Indexing:** Optimized for efficient query and reporting
- **Audit Actions:** CREATE, READ, UPDATE, DELETE, AUTHENTICATE, AUTHORIZE, EXPORT

### Files Modified/Created
- `AuditLog.java` - Immutable audit log model
- `AuditLogRepository.java` - Audit query interface
- `AuditLoggingService.java` - Comprehensive audit logging service
- Database table: `audit_logs` with indexes on user_id, action_type, created_at, tenant_id

### Audit Logging Points
```java
// Complaint creation
auditLoggingService.logComplaintCreated(userId, tenantId, complaintId, description);

// Complaint read
auditLoggingService.logComplaintRead(userId, tenantId, complaintId);

// Complaint update
auditLoggingService.logComplaintUpdated(userId, tenantId, complaintId, updates);

// Admin response update
auditLoggingService.logAdminResponseUpdated(userId, tenantId, complaintId, newStatus);

// Authorization failures
auditLoggingService.logAuthorizationFailure(userId, tenantId, resource);

// Authentication attempts
auditLoggingService.logAuthenticationAttempt(username, isSuccessful);
```

### Audit Query Examples
```java
// Get user's audit trail
List<AuditLog> userAudits = auditLoggingService.getUserAuditLogs(userId);

// Get all audit logs for tenant
List<AuditLog> tenantAudits = auditLoggingService.getTenantAuditLogs(tenantId);

// Get failed operations
List<AuditLog> failures = auditLoggingService.getFailedOperations(tenantId);

// Get logs by date range
List<AuditLog> rangeLogs = auditLoggingService.getAuditLogsByDateRange(
    tenantId, startDate, endDate
);
```

### Security Benefits
- Complete audit trail for compliance
- Non-repudiation - users cannot deny actions
- Intrusion detection - unusual access patterns discoverable
- Forensic analysis - incident investigation capability
- SIEM integration ready

---

## Fix_005: Externalized Secrets and Secure Configuration

**Weakness ID:** Wk_005  
**STRIDE:** Information Disclosure  
**OWASP:** A05 Security Misconfiguration  
**CWE:** CWE-798 (Use of Hardcoded Credentials)  
**CIA:** Confidentiality  
**ASVS:** V3.4 - Session Cookie Management, V6.4 - Secure Configuration  
**D3FEND:** D3-SCF Secure Configuration File

### Implementation Details
- **Configuration Sources:** Environment variables (preferred)
- **Default Values:** Safe defaults for development only
- **Secrets Management:** Database credentials, encryption keys, SSL certificates
- **Environment Profiles:** `dev`, `test`, `prod` configurations

### Files Modified/Created
- `application.properties` - Development configuration with environment variable support
- `application-prod.properties` - Production configuration template

### Configuration via Environment Variables
```bash
# Database Configuration
export SPRING_DATASOURCE_URL=jdbc:mysql://db-prod:3306/complaint_db
export SPRING_DATASOURCE_USERNAME=db_user
export SPRING_DATASOURCE_PASSWORD=secure_password_here

# Server Configuration
export SERVER_PORT=8443

# SSL/TLS Configuration
export KEYSTORE_PATH=/opt/keystore/app.p12
export KEYSTORE_PASSWORD=keystore_password
export KEY_ALIAS=tomcat
```

### Spring Boot Profile Activation
```bash
# Development (default)
java -jar app.jar

# Production
java -jar app.jar --spring.profiles.active=prod

# Custom property override
java -jar app.jar --spring.datasource.username=dbuser
```

### Configuration Hierarchy
1. Environment variables (highest priority)
2. System properties
3. application.properties defaults
4. application-{profile}.properties

### Security Features in Production Config
```properties
# HTTPS/TLS Enforcement
server.ssl.enabled=true
server.ssl.key-store=${KEYSTORE_PATH}

# Secure Session Cookies
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict

# Logging
logging.level.root=WARN
logging.file.name=/var/log/abc_complaint_app/application.log

# Cache Control
spring.thymeleaf.cache=true
```

### Security Benefits
- No hardcoded credentials in source code
- Credentials never committed to version control
- Different credentials for different environments
- Audit trail for credential access
- Compatible with container orchestration (Docker, Kubernetes)
- SIEM and secrets vault integration ready

---

## Implementation Checklist

### Before Deployment
- [ ] Set all required environment variables
- [ ] Configure HTTPS/TLS certificates
- [ ] Set up database with secure credentials
- [ ] Enable database encryption at rest
- [ ] Configure firewall rules
- [ ] Enable rate limiting
- [ ] Set up logging aggregation
- [ ] Configure backup strategy
- [ ] Complete security testing

### Ongoing Maintenance
- [ ] Monitor audit logs for suspicious activity
- [ ] Review password strength requirements
- [ ] Rotate database credentials quarterly
- [ ] Update dependencies for security patches
- [ ] Review failed authentication attempts
- [ ] Audit cross-tenant access attempts
- [ ] Validate output encoding effectiveness
- [ ] Monitor performance impact of BCrypt

---

## Testing Security Fixes

### Test Fix_001 (Password Hashing)
```java
@Test
public void testPasswordHashing() {
    String plainPassword = "TestPassword123!";
    String hash1 = PasswordEncoderUtil.encodePassword(plainPassword);
    String hash2 = PasswordEncoderUtil.encodePassword(plainPassword);
    
    // Hashes should be different (different salts)
    assertNotEquals(hash1, hash2);
    
    // Both should verify
    assertTrue(PasswordEncoderUtil.matchPassword(plainPassword, hash1));
    assertTrue(PasswordEncoderUtil.matchPassword(plainPassword, hash2));
    
    // Wrong password should not verify
    assertFalse(PasswordEncoderUtil.matchPassword("WrongPassword", hash1));
}
```

### Test Fix_002 (Output Escaping)
```java
@Test
public void testXSSPrevention() {
    String xssPayload = "<script>alert('XSS')</script>";
    String sanitized = OutputSanitizer.sanitizeComplaintField(xssPayload, "title");
    
    assertTrue(sanitized.contains("&lt;script&gt;"));
    assertFalse(sanitized.contains("<script>"));
}
```

### Test Fix_003 (Ownership Checks)
```java
@Test
public void testOwnershipValidation() {
    Complaint result = complaintService.getComplaintById(
        complaintId, wrongUserId, tenantId, false
    );
    assertNull(result); // Should fail ownership check
}
```

### Test Fix_004 (Audit Logging)
```java
@Test
public void testAuditLogging() {
    auditLoggingService.logComplaintCreated(userId, tenantId, complaintId, "Test");
    
    List<AuditLog> logs = auditLoggingService.getUserAuditLogs(userId);
    assertTrue(logs.stream()
        .anyMatch(log -> log.getActionType().equals("CREATE")));
}
```

### Test Fix_005 (Configuration)
```java
@Test
public void testEnvironmentConfiguration() {
    String url = environment.getProperty("spring.datasource.url");
    assertNotNull(url);
    assertTrue(url.contains("jdbc:mysql://"));
}
```

---

## References

- OWASP Security Top 10 2021
- OWASP ASVS (Application Security Verification Standard)
- CWE/SANS Top 25
- STRIDE Threat Modeling
- D3FEND Framework
- Spring Security Documentation
- NIST Cybersecurity Framework

---

## Support and Questions

For questions regarding these security implementations, please refer to:
1. Inline code comments with security headers
2. This documentation file
3. Security team for compliance questions
4. DevSecOps team for deployment guidance

Last Updated: 2026-04-18  
Version: 1.0
