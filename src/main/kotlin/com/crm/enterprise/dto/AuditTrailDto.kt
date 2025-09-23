package com.crm.enterprise.dto

import java.time.LocalDateTime

data class AuditTrailResponse(
    val id: Long,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val fieldName: String?,
    val oldValue: String?,
    val newValue: String?,
    val description: String,
    val performedBy: Long,
    val performedByName: String? = null, // User's name for display
    val companyId: Long,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: LocalDateTime
)

data class AuditTrailRequest(
    val entityType: String,
    val entityId: Long,
    val action: String,
    val fieldName: String? = null,
    val oldValue: String? = null,
    val newValue: String? = null,
    val description: String,
    val ipAddress: String? = null,
    val userAgent: String? = null
)

data class AuditTrailFilter(
    val entityType: String? = null,
    val entityId: Long? = null,
    val action: String? = null,
    val performedBy: Long? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 50
)

data class AuditTrailSummary(
    val totalActivities: Long,
    val activitiesByType: Map<String, Long>,
    val activitiesByAction: Map<String, Long>,
    val recentActivities: List<AuditTrailResponse>
)
