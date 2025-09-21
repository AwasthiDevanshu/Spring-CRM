package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("payment_reminders")
data class PaymentReminder(
    @Id
    val id: Long? = null,
    val dealId: Long,
    val contactId: Long? = null,
    val type: ReminderType,
    val subject: String,
    val message: String,
    val amount: BigDecimal? = null,
    val dueDate: LocalDateTime,
    val status: ReminderStatus = ReminderStatus.PENDING,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val sentAt: LocalDateTime? = null,
    val acknowledgedAt: LocalDateTime? = null,
    val acknowledgedBy: Long? = null,
    val companyId: Long,
    val createdBy: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null
)
