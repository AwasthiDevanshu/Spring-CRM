package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("contacts")
data class Contact(
    @Id
    val id: Long? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val jobTitle: String? = null,
    val department: String? = null,
    val companyId: Long,
    val leadId: Long? = null, // If converted from lead
    val isActive: Boolean = true,
    val notes: String? = null,
    val assignedUserId: Long? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val fullName: String
        get() = "$firstName $lastName"
}

