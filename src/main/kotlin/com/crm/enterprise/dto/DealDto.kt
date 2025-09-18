package com.crm.enterprise.dto

import com.crm.enterprise.entity.DealStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class DealRequest(
    val name: String,
    val description: String? = null,
    val value: BigDecimal,
    val currency: String = "USD",
    val status: DealStatus = DealStatus.OPEN,
    val probability: Int = 0, // 0-100
    val expectedCloseDate: LocalDateTime? = null,
    val contactId: Long? = null,
    val pipelineId: Long,
    val stageId: Long,
    val assignedUserId: Long? = null
)

data class DealResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val value: BigDecimal,
    val currency: String,
    val status: DealStatus,
    val probability: Int,
    val expectedCloseDate: LocalDateTime? = null,
    val actualCloseDate: LocalDateTime? = null,
    val contactId: Long? = null,
    val pipelineId: Long,
    val stageId: Long,
    val assignedUserId: Long? = null,
    val companyId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class DealUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val value: BigDecimal? = null,
    val currency: String? = null,
    val status: DealStatus? = null,
    val probability: Int? = null,
    val expectedCloseDate: LocalDateTime? = null,
    val actualCloseDate: LocalDateTime? = null,
    val contactId: Long? = null,
    val pipelineId: Long? = null,
    val stageId: Long? = null,
    val assignedUserId: Long? = null
)
