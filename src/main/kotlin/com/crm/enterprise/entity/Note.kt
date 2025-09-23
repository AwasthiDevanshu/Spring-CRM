package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("notes")
data class Note(
    @Id val id: Long? = null,
    val content: String,
    @Column("entity_type") val entityType: String, // 'LEAD', 'CONTACT', 'DEAL'
    @Column("entity_id") val entityId: Long,
    @Column("created_by") val createdBy: Long,
    @Column("company_id") val companyId: Long,
    @Column("created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column("updated_at") val updatedAt: LocalDateTime = LocalDateTime.now()
)
