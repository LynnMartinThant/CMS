# Database Migration Guide - Security Implementation

## Overview
This guide provides step-by-step instructions for migrating the existing database schema to support all security enhancements.

## Pre-Migration Checklist
- [ ] Backup existing database
- [ ] Backup existing application
- [ ] Test migration on staging environment first
- [ ] Plan maintenance window
- [ ] Notify users of planned downtime
- [ ] Prepare rollback plan

---

## Migration Steps

### Step 1: Backup Existing Data
```bash
# MySQL backup
mysqldump -u root -p complaint_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Or using Docker
docker exec mysql_container mysqldump -u root -ppassword complaint_db > backup.sql
```

### Step 2: Create New Tables

#### Create AuditLog Table
```sql
CREATE TABLE IF NOT EXISTS audit_logs (
    id BINARY(16) NOT NULL COMMENT 'UUID',
    user_id INT NOT NULL,
    tenant_id INT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    target_resource_id BINARY(16),
    description TEXT,
    status VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_created_at (created_at),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### Create User Table
```sql
CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) NOT NULL COMMENT 'UUID',
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    hashed_password VARCHAR(60) NOT NULL COMMENT 'BCrypt hash $2a$12$...',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login DATETIME,
    password_last_changed DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_email (email),
    UNIQUE INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Step 3: Migrate Existing Complaints Table

#### Step 3a: Add New Columns (if not using Hibernate auto-update)
```sql
-- Add tenant_id column if not exists
ALTER TABLE complaints 
ADD COLUMN IF NOT EXISTS tenant_id INT NOT NULL DEFAULT 1 AFTER id;

-- Add index on tenant_id
ALTER TABLE complaints 
ADD INDEX IF NOT EXISTS idx_tenant_id (tenant_id);

-- Add indexes for performance
ALTER TABLE complaints 
ADD INDEX IF NOT EXISTS idx_status (status);

ALTER TABLE complaints 
ADD INDEX IF NOT EXISTS idx_created_at (created_at);
```

#### Step 3b: Migrate ID from BIGINT to UUID (Optional - Only if needed)
**WARNING: This is a complex operation. Consider keeping existing IDs and adding UUID separately**

```sql
-- Create new table with UUID
CREATE TABLE complaints_new (
    id BINARY(16) NOT NULL COMMENT 'UUID',
    tenant_id INT NOT NULL,
    user_id INT,
    user_name VARCHAR(255),
    title VARCHAR(255),
    description LONGTEXT,
    category VARCHAR(255),
    status VARCHAR(255) DEFAULT 'Pending',
    admin_response LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migrate data (if proceeding with UUID migration)
INSERT INTO complaints_new 
SELECT 
    UUID_TO_BIN(UUID()) as id,
    COALESCE(tenant_id, 1),
    user_id,
    user_name,
    title,
    description,
    category,
    status,
    admin_response,
    created_at,
    updated_at
FROM complaints;

-- Rename tables
RENAME TABLE complaints TO complaints_old;
RENAME TABLE complaints_new TO complaints;
```

**Note:** The application now uses `GenerationType.UUID` for new records. Existing records will keep their Long IDs until migrated.

### Step 4: Create Application User Records
```sql
-- Insert default admin user (replace with actual credentials)
-- Password must be BCrypt hashed before insertion
INSERT INTO users (id, username, email, hashed_password, role, active, password_last_changed)
VALUES (
    UUID_TO_BIN(UUID()),
    'admin',
    'admin@example.com',
    '$2a$12$YOUR_BCRYPT_HASH_HERE', -- Generate using PasswordEncoderUtil
    'ADMIN',
    TRUE,
    NOW()
);

-- Insert default user account
INSERT INTO users (id, username, email, hashed_password, role, active, password_last_changed)
VALUES (
    UUID_TO_BIN(UUID()),
    'user1',
    'user1@example.com',
    '$2a$12$YOUR_BCRYPT_HASH_HERE',
    'USER',
    TRUE,
    NOW()
);
```

### Step 5: Update Application Configuration

#### Update application.properties:
```properties
# Before migration
spring.datasource.url=jdbc:mysql://localhost:3306/complaint_db
spring.datasource.username=root
spring.datasource.password=MyPassword123
spring.jpa.hibernate.ddl-auto=update

# After migration - Use environment variables
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/complaint_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:changeme}
spring.jpa.hibernate.ddl-auto=validate
```

### Step 6: Set Environment Variables

```bash
# Linux/Mac - Add to ~/.bash_profile or ~/.zshrc
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/complaint_db
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=secure_password_here

# Windows - Add to System Environment Variables
setx SPRING_DATASOURCE_URL "jdbc:mysql://localhost:3306/complaint_db"
setx SPRING_DATASOURCE_USERNAME "root"
setx SPRING_DATASOURCE_PASSWORD "secure_password_here"

# Docker - Add to .env file
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/complaint_db
SPRING_DATASOURCE_USERNAME=dbuser
SPRING_DATASOURCE_PASSWORD=dbpassword
```

### Step 7: Build and Deploy

```bash
# Clean build
mvn clean package

# Start with production profile
java -Dspring.profiles.active=prod \
     -Dspring.datasource.url=jdbc:mysql://localhost:3306/complaint_db \
     -Dspring.datasource.username=root \
     -Dspring.datasource.password=changeme \
     -jar target/ABC_FComplaintWebapp-0.0.1-SNAPSHOT.jar
```

### Step 8: Verify Migration

```bash
# Check new tables exist
SELECT COUNT(*) FROM audit_logs;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM complaints;

# Verify data integrity
SELECT COUNT(*) FROM complaints WHERE tenant_id IS NULL; -- Should be 0
SELECT COUNT(*) FROM complaints WHERE user_id IS NULL;   -- Should be 0

# Check UUID format
SELECT HEX(id) FROM users LIMIT 1;

# Verify audit logging
SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 10;
```

---

## Data Migration Queries

### Migrate Existing User Data to New Users Table
If you have user data in another table:
```sql
INSERT INTO users (id, username, email, hashed_password, role, created_at)
SELECT 
    UUID_TO_BIN(UUID()),
    user_username,  -- Adjust column name
    user_email,     -- Adjust column name
    '$2a$12$PLACEHOLDER', -- Must hash these separately
    'USER',
    NOW()
FROM legacy_users;
```

### Link Complaints to Tenants
```sql
-- If you have tenant information elsewhere
UPDATE complaints c
JOIN complaint_tenant_map ct ON c.id = ct.complaint_id
SET c.tenant_id = ct.tenant_id;
```

---

## Rollback Procedure

If migration fails, rollback to previous state:

```bash
# Stop application
systemctl stop complaint-app  # or docker stop

# Restore database from backup
mysql -u root -p complaint_db < backup_20260418_120000.sql

# Restart with previous version
java -jar ABC_FComplaintWebapp-previous-version.jar

# Restore previous application.properties
cp application.properties.backup application.properties
```

---

## Performance Considerations

### Index Strategy
```sql
-- Verify indexes are created
SHOW INDEX FROM complaints;
SHOW INDEX FROM audit_logs;
SHOW INDEX FROM users;

-- Monitor query performance
EXPLAIN SELECT * FROM complaints WHERE tenant_id = 1 AND user_id = 123;
```

### UUID vs BIGINT Performance
- UUID (binary(16)): 128 bits, slower comparisons, more storage
- BIGINT (8 bytes): Faster comparisons, smaller indexes
- Trade-off: Security vs Performance (UUIDs prevent enumeration attacks)

### Optimization Tips
1. Keep indexes on frequently queried columns: tenant_id, user_id, status
2. Archive old audit logs periodically (older than 1 year)
3. Use queries with LIMIT for large result sets
4. Consider sharding by tenant_id for large deployments

---

## Post-Migration Validation

### Application Health Checks
```bash
# Health endpoint
curl http://localhost:8080/actuator/health

# Check logs for errors
tail -f /var/log/complaint-app/application.log

# Verify database connectivity
curl http://localhost:8080/api/complaints/admin/all
```

### Security Verification
```bash
# Test BCrypt password verification
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin_password"}'

# Verify audit logging
curl http://localhost:8080/api/audit/logs

# Test output encoding
curl -X POST http://localhost:8080/complaints/create \
     -d 'title=<script>alert(1)</script>'
```

### Data Integrity Checks
```sql
-- Check for NULL values where not allowed
SELECT COUNT(*) FROM complaints WHERE tenant_id IS NULL;
SELECT COUNT(*) FROM users WHERE hashed_password IS NULL;
SELECT COUNT(*) FROM audit_logs WHERE user_id IS NULL;

-- Verify counts
SELECT COUNT(*) as complaint_count FROM complaints;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as audit_count FROM audit_logs;
```

---

## Schema Export

After migration, export the final schema:
```bash
# Export just schema (no data)
mysqldump -u root -p --no-data complaint_db > schema.sql

# Export with data
mysqldump -u root -p complaint_db > full_backup.sql
```

---

## Timeline Estimate

- Pre-migration setup: 30 minutes
- Migration execution: 15-30 minutes (depends on data size)
- Verification: 15 minutes
- Total: 1-2 hours (recommend during maintenance window)

---

## Support

For migration issues:
1. Check application logs: `/var/log/complaint-app/application.log`
2. Review database error log: `/var/log/mysql/error.log`
3. Consult SECURITY_FIXES_SUMMARY.md for implementation details
4. Contact DevSecOps team with backup and logs

---

**Modified Date:** April 18, 2026  
**Version:** 1.0
