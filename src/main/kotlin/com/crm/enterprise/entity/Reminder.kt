package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

enum class ReminderType {
    ACTIVITY_DUE, FOLLOW_UP, MEETING, CALL, EMAIL, TASK, CUSTOM,
    PAYMENT_DUE, PAYMENT_OVERDUE, DELIVERY_CONFIRMATION
}

enum class ReminderStatus {
    PENDING, SENT, ACKNOWLEDGED, CANCELLED
}

enum class ReminderPriority {
    LOW, MEDIUM, HIGH, URGENT
}

@Table("reminders")
data class Reminder(
    @Id val id: Long? = null,
    val type: ReminderType,
    val title: String,
    val description: String? = null,
    val reminderDate: LocalDateTime,
    val status: ReminderStatus = ReminderStatus.PENDING,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    @org.springframework.data.relational.core.mapping.Column("assigned_to")
    val assignedTo: Long, // User who should receive the reminder
    @org.springframework.data.relational.core.mapping.Column("assigned_by")
    val assignedBy: Long, // User who created the reminder
    @org.springframework.data.relational.core.mapping.Column("entity_type")
    val entityType: String? = null, // 'LEAD', 'CONTACT', 'DEAL', 'ACTIVITY', etc.
    @org.springframework.data.relational.core.mapping.Column("entity_id")
    val entityId: Long? = null, // ID of the related entity
    @org.springframework.data.relational.core.mapping.Column("activity_id")
    val activityId: Long? = null, // If reminder is for a specific activity
    @org.springframework.data.relational.core.mapping.Column("company_id")
    val companyId: Long,
    @org.springframework.data.relational.core.mapping.Column("sent_at")
    val sentAt: LocalDateTime? = null,
    @org.springframework.data.relational.core.mapping.Column("acknowledged_at")
    val acknowledgedAt: LocalDateTime? = null,
    @org.springframework.data.relational.core.mapping.Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @org.springframework.data.relational.core.mapping.Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @org.springframework.data.relational.core.mapping.Column("deleted_at")
    val deletedAt: LocalDateTime? = null
)
