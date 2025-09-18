package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("pipeline_stages")
data class PipelineStage(
    @Id
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val position: Int,
    val probability: Int, // 0-100
    val isActive: Boolean = true,
    val pipelineId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

