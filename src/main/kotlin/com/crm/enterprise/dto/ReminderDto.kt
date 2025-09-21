package com.crm.enterprise.dto

import com.crm.enterprise.entity.ReminderPriority
import com.crm.enterprise.entity.ReminderStatus
import com.crm.enterprise.entity.ReminderType
import java.time.LocalDateTime

data class ReminderRequest(
    val type: ReminderType,
    val title: String,
    val description: String? = null,
    val reminderDate: String, // Accept as String and convert in service
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val assignedTo: Long, // User who should receive the reminder
    val entityType: String? = null, // 'LEAD', 'CONTACT', 'DEAL', 'ACTIVITY', etc.
    val entityId: Long? = null, // ID of the related entity
    val activityId: Long? = null // If reminder is for a specific activity
)

data class ReminderResponse(
    val id: Long,
    val type: ReminderType,
    val title: String,
    val description: String?,
    val reminderDate: LocalDateTime,
    val status: ReminderStatus,
    val priority: ReminderPriority,
    val assignedTo: Long,
    val assignedToName: String? = null, // User's name for display
    val assignedBy: Long,
    val assignedByName: String? = null, // User's name for display
    val entityType: String?,
    val entityId: Long?,
    val entityName: String? = null, // Name of the related entity for display
    val activityId: Long?,
    val companyId: Long,
    val sentAt: LocalDateTime?,
    val acknowledgedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ReminderUpdateRequest(
    val type: ReminderType? = null,
    val title: String? = null,
    val description: String? = null,
    val reminderDate: String? = null, // Accept as String and convert in service
    val priority: ReminderPriority? = null,
    val assignedTo: Long? = null,
    val status: ReminderStatus? = null
)

data class ReminderFilter(
    val type: ReminderType? = null,
    val status: ReminderStatus? = null,
    val priority: ReminderPriority? = null,
    val assignedTo: Long? = null,
    val entityType: String? = null,
    val entityId: Long? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 50
)
