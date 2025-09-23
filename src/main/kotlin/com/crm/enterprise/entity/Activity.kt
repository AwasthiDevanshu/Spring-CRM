package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
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
    @Id val id: Long? = null,
    val type: ActivityType,
    val subject: String,
    val description: String? = null,
    val outcome: String? = null,
    val duration: Int? = null, // in minutes
    val status: ActivityStatus = ActivityStatus.PENDING,
    val priority: ActivityPriority = ActivityPriority.MEDIUM,
    @Column("assigned_to") val assignedTo: Long, // User who should perform the activity
    @Column("assigned_by") val assignedBy: Long, // User who assigned the activity
    @Column("entity_type") val entityType: String, // 'LEAD', 'CONTACT', 'DEAL', etc.
    @Column("entity_id") val entityId: Long, // ID of the related entity
    @Column("company_id") val companyId: Long,
    @Column("activity_date") val activityDate: LocalDateTime = LocalDateTime.now(),
    @Column("due_date") val dueDate: LocalDateTime? = null,
    @Column("completed_at") val completedAt: LocalDateTime? = null,
    @Column("created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column("updated_at") val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column("deleted_at") val deletedAt: LocalDateTime? = null
)

