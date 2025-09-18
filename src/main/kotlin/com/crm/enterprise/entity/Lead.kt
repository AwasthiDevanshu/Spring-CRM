package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

enum class LeadStatus {
    NEW, CONTACTED, QUALIFIED, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST
}

enum class LeadSource {
    WEBSITE, PHONE, EMAIL, REFERRAL, SOCIAL_MEDIA, ADVERTISEMENT, TRADE_SHOW, FACEBOOK_ADS, GOOGLE_ADS, OTHER
}

@Table("leads")
data class Lead(
    @Id
    val id: Long? = null,
    @org.springframework.data.relational.core.mapping.Column("first_name")
    val firstName: String,
    @org.springframework.data.relational.core.mapping.Column("last_name")
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val company: String? = null,
    @org.springframework.data.relational.core.mapping.Column("job_title")
    val jobTitle: String? = null,
    val status: LeadStatus = LeadStatus.NEW,
    val source: LeadSource = LeadSource.OTHER,
    val score: Int = 0,
    val notes: String? = null,
    @org.springframework.data.relational.core.mapping.Column("assigned_user_id")
    val assignedUserId: Long? = null,
    @org.springframework.data.relational.core.mapping.Column("company_id")
    val companyId: Long,
    @org.springframework.data.relational.core.mapping.Column("created_by_id")
    val createdById: Long = 1L, // Default to admin user
    @org.springframework.data.relational.core.mapping.Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @org.springframework.data.relational.core.mapping.Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val fullName: String
        get() = "$firstName $lastName"
}

