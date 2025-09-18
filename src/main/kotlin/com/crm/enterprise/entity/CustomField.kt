package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("custom_fields")
data class CustomField(
    @Id
    val id: Long? = null,
    val entityType: String, // LEAD, CONTACT, DEAL, etc.
    val fieldName: String,
    val fieldLabel: String,
    val fieldType: String, // TEXT, NUMBER, EMAIL, PHONE, DATE, SELECT, MULTI_SELECT, CHECKBOX, TEXTAREA
    val isRequired: Boolean = false,
    val defaultValue: String? = null,
    val options: String? = null, // JSON string for SELECT and MULTI_SELECT options
    val validationRules: String? = null, // JSON string for validation rules
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val companyId: Long,
    val createdBy: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
