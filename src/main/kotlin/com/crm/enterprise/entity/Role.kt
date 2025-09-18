package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("roles")
data class Role(
    @Id
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val permissions: String? = null, // JSON string of permissions
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

