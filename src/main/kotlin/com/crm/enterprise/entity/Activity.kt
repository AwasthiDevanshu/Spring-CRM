package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

enum class ActivityType {
    CALL, EMAIL, MEETING, TASK, NOTE, REMINDER, OTHER,
    FORM_SHARED, FORM_SUBMITTED, FORM_FOLLOW_UP
}

enum class ActivityStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}

enum class ActivityPriority {
    LOW, MEDIUM, HIGH, URGENT
}

@Table("activities")
data class Activity(
    @Id
    val id: Long? = null,
    val type: ActivityType,
    val subject: String,
    val description: String? = null,
    val outcome: String? = null,
    val duration: Int? = null, // in minutes
    val status: ActivityStatus = ActivityStatus.PENDING,
    val priority: ActivityPriority = ActivityPriority.MEDIUM,
    @org.springframework.data.relational.core.mapping.Column("assigned_to")
    val assignedTo: Long, // User who should perform the activity
    @org.springframework.data.relational.core.mapping.Column("assigned_by")
    val assignedBy: Long, // User who assigned the activity
    @org.springframework.data.relational.core.mapping.Column("entity_type")
    val entityType: String, // 'LEAD', 'CONTACT', 'DEAL', etc.
    @org.springframework.data.relational.core.mapping.Column("entity_id")
    val entityId: Long, // ID of the related entity
    @org.springframework.data.relational.core.mapping.Column("company_id")
    val companyId: Long,
    @org.springframework.data.relational.core.mapping.Column("activity_date")
    val activityDate: LocalDateTime = LocalDateTime.now(),
    @org.springframework.data.relational.core.mapping.Column("due_date")
    val dueDate: LocalDateTime? = null,
    @org.springframework.data.relational.core.mapping.Column("completed_at")
    val completedAt: LocalDateTime? = null,
    @org.springframework.data.relational.core.mapping.Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @org.springframework.data.relational.core.mapping.Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @org.springframework.data.relational.core.mapping.Column("deleted_at")
    val deletedAt: LocalDateTime? = null
)

