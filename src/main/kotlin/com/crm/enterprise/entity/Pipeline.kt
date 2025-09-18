package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("pipelines")
data class Pipeline(
    @Id
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val companyId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

