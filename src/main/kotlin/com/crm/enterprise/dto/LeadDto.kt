package com.crm.enterprise.dto

import com.crm.enterprise.entity.LeadSource
import com.crm.enterprise.entity.LeadStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class LeadRequest(
    @field:NotBlank(message = "First name is required")
    @field:Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    @field:Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    val lastName: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
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

