package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

enum class DealStatus {
    OPEN, WON, LOST, CANCELLED
}

@Table("deals")
data class Deal(
    @Id
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val value: BigDecimal,
    val currency: String = "USD",
    val status: DealStatus = DealStatus.OPEN,
    val probability: Int = 0, // 0-100
    val expectedCloseDate: LocalDateTime? = null,
    val actualCloseDate: LocalDateTime? = null,
    val contactId: Long? = null,
    val pipelineId: Long,
    val stageId: Long,
    val assignedUserId: Long? = null,
    val companyId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

