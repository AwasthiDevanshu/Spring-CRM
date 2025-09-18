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
    val assignedUserId: Long? = null,
    val relatedEntityType: String? = null,
    val relatedEntityId: Long? = null
)

data class ActivityResponse(
    val id: Long,
    val type: ActivityType,
    val subject: String,
    val description: String?,
    val status: ActivityStatus,
    val priority: ActivityPriority,
    val dueDate: LocalDateTime?,
    val completedDate: LocalDateTime?,
    val assignedUserId: Long?,
    val relatedEntityType: String?,
    val relatedEntityId: Long?,
    val companyId: Long,
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
    val assignedUserId: Long? = null,
    val relatedEntityType: String? = null,
    val relatedEntityId: Long? = null
)
