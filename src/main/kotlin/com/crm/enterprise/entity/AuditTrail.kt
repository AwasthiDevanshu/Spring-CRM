package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("audit_trails")
data class AuditTrail(
    @Id
    val id: Long? = null,
    val entityType: String, // 'LEAD', 'DEAL', 'CONTACT', 'ACTIVITY', 'NOTE', etc.
    val entityId: Long,
    val action: String, // 'CREATE', 'UPDATE', 'DELETE', 'VIEW', 'ASSIGN', 'STATUS_CHANGE', etc.
    val fieldName: String? = null, // Specific field that was changed
    val oldValue: String? = null, // Previous value
    val newValue: String? = null, // New value
    val description: String, // Human-readable description
    val performedBy: Long, // User ID who performed the action
    val companyId: Long,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
