package com.crm.enterprise.dto

import java.time.LocalDateTime

data class CompanyRequest(
    val name: String,
    val description: String? = null,
    val website: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null
)

data class CompanyResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val website: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CompanyUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val website: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null
)

data class CompanyStatsResponse(
    val id: Long,
    val name: String,
    val totalUsers: Int,
    val totalLeads: Int,
    val totalContacts: Int,
    val totalDeals: Int,
    val totalRevenue: Double,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)
