package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("notes")
data class Note(
    @Id
    val id: Long? = null,
    val content: String,
    @org.springframework.data.relational.core.mapping.Column("entity_type")
    val entityType: String, // 'LEAD', 'CONTACT', 'DEAL'
    @org.springframework.data.relational.core.mapping.Column("entity_id")
    val entityId: Long,
    @org.springframework.data.relational.core.mapping.Column("created_by")
    val createdBy: Long,
    @org.springframework.data.relational.core.mapping.Column("company_id")
    val companyId: Long,
    @org.springframework.data.relational.core.mapping.Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @org.springframework.data.relational.core.mapping.Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
