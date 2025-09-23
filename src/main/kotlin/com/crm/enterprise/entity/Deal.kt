package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

enum class DealStatus {
    OPEN, WON, LOST, CANCELLED
}

enum class PaymentType {
    ONE_TIME, RECURRING, EMI
}

enum class PaymentStatus {
    PENDING, PAID, OVERDUE, CANCELLED
}

@Table("deals")
data class Deal(
    @Id val id: Long? = null,
    val name: String,
    val description: String? = null,
    val value: BigDecimal,
    val currency: String = "INR",
    val status: DealStatus = DealStatus.OPEN,
    val probability: Int = 0, // 0-100
    val expectedCloseDate: LocalDateTime? = null,
    val actualCloseDate: LocalDateTime? = null,
    val contactId: Long? = null,
    val leadId: Long? = null, // Link to lead if converted from lead
    val pipelineId: Long,
    val stageId: Long,
    val assignedUserId: Long? = null,
    val companyId: Long,
    // Payment-related fields
    val paymentType: PaymentType = PaymentType.ONE_TIME,
    val totalAmount: BigDecimal? = null, // Total amount for EMI/recurring
    val installmentAmount: BigDecimal? = null, // Amount per installment
    val installmentCount: Int? = null, // Number of installments
    val installmentFrequency: String? = null, // MONTHLY, QUARTERLY, etc.
    val nextPaymentDate: LocalDateTime? = null,
    val lastPaymentDate: LocalDateTime? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val deliveryDate: LocalDateTime? = null, // When product/service was delivered
    val deliveryStatus: String? = null, // PENDING, DELIVERED, PARTIAL
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null
)

