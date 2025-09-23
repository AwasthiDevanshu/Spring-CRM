package com.crm.enterprise.dto

import com.crm.enterprise.entity.ActivityStatus
import com.crm.enterprise.entity.ActivityType
import com.crm.enterprise.entity.ActivityPriority
import java.time.LocalDateTime

data class ActivityRequest(
    val type: ActivityType,
    val subject: String,
    val description: String? = null,
    val status: ActivityStatus = ActivityStatus.PENDING,
    val priority: ActivityPriority = ActivityPriority.MEDIUM,
    val dueDate: String? = null, // Accept as String and convert in service
    val assignedTo: Long, // User who should perform the activity
    val entityType: String, // 'LEAD', 'CONTACT', 'DEAL', etc.
    val entityId: Long, // ID of the related entity
    val outcome: String? = null,
    val duration: Int? = null // in minutes
)

data class ActivityResponse(
    val id: Long,
    val type: ActivityType,
    val subject: String,
    val description: String?,
    val status: ActivityStatus,
    val priority: ActivityPriority,
    val dueDate: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val assignedTo: Long,
    val assignedToName: String? = null, // User's name for display
    val assignedBy: Long,
    val assignedByName: String? = null, // User's name for display
    val entityType: String,
    val entityId: Long,
    val entityName: String? = null, // Name of the related entity for display
    val outcome: String?,
    val duration: Int?,
    val companyId: Long,
    val activityDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ActivityUpdateRequest(
    val type: ActivityType? = null,
    val subject: String? = null,
    val description: String? = null,
    val status: ActivityStatus? = null,
    val priority: ActivityPriority? = null,
    val dueDate: String? = null, // Accept as String and convert in service
    val assignedTo: Long? = null,
    val outcome: String? = null,
    val duration: Int? = null
)
