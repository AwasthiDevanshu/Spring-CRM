package com.crm.enterprise.dto

import com.crm.enterprise.entity.LeadSource
import com.crm.enterprise.entity.LeadStatus
import java.time.LocalDateTime

data class LeadRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val company: String? = null,
    val jobTitle: String? = null,
    val status: LeadStatus = LeadStatus.NEW,
    val source: LeadSource = LeadSource.OTHER,
    val score: Int = 0,
    val notes: String? = null,
    val assignedUserId: Long? = null
)

data class LeadResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val company: String? = null,
    val jobTitle: String? = null,
    val status: LeadStatus,
    val source: LeadSource,
    val score: Int,
    val notes: String? = null,
    val assignedUserId: Long? = null,
    val companyId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class LeadUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val company: String? = null,
    val jobTitle: String? = null,
    val status: LeadStatus? = null,
    val source: LeadSource? = null,
    val score: Int? = null,
    val notes: String? = null,
    val assignedUserId: Long? = null
)

