package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class User(
    @Id
    val id: Long? = null,
    val email: String,
    val username: String,
    val hashedPassword: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val isActive: Boolean = true,
    val isSuperuser: Boolean = false,
    val isCompanyAdmin: Boolean = false,
    val lastLogin: LocalDateTime? = null,
    val companyId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val fullName: String
        get() = "$firstName $lastName"
}

