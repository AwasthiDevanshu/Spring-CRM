package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

enum class ActivityType {
    CALL, EMAIL, MEETING, TASK, NOTE, REMINDER, OTHER
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
    @org.springframework.data.relational.core.mapping.Column("user_id")
    val userId: Long,
    @org.springframework.data.relational.core.mapping.Column("lead_id")
    val leadId: Long? = null,
    @org.springframework.data.relational.core.mapping.Column("contact_id")
    val contactId: Long? = null,
    @org.springframework.data.relational.core.mapping.Column("deal_id")
    val dealId: Long? = null,
    @org.springframework.data.relational.core.mapping.Column("company_id")
    val companyId: Long,
    @org.springframework.data.relational.core.mapping.Column("activity_date")
    val activityDate: LocalDateTime = LocalDateTime.now(),
    @org.springframework.data.relational.core.mapping.Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @org.springframework.data.relational.core.mapping.Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

