package com.crm.enterprise.dto

import java.time.LocalDateTime

data class ContactRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val jobTitle: String? = null,
    val department: String? = null,
    val leadId: Long? = null, // If converted from lead
    val notes: String? = null,
    val assignedUserId: Long? = null
)

data class ContactResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val jobTitle: String? = null,
    val department: String? = null,
    val companyId: Long,
    val leadId: Long? = null,
    val isActive: Boolean,
    val notes: String? = null,
    val assignedUserId: Long? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ContactUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val jobTitle: String? = null,
    val department: String? = null,
    val notes: String? = null,
    val assignedUserId: Long? = null
)
