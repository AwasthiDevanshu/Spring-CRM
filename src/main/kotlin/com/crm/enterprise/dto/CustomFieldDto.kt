package com.crm.enterprise.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Request DTO for creating a custom field")
data class CustomFieldRequest(
    @field:Schema(description = "Entity type this field applies to", example = "LEAD")
    val entityType: String,
    @field:Schema(description = "Field name (internal identifier)", example = "custom_industry")
    val fieldName: String,
    @field:Schema(description = "Field label (display name)", example = "Industry")
    val fieldLabel: String,
    @field:Schema(description = "Field type", example = "SELECT")
    val fieldType: String,
    @field:Schema(description = "Whether the field is required", example = "false")
    val isRequired: Boolean = false,
    @field:Schema(description = "Default value for the field", example = "Technology")
    val defaultValue: String? = null,
    @field:Schema(description = "Options for SELECT and MULTI_SELECT fields (JSON string)", example = "[\"Technology\", \"Healthcare\", \"Finance\"]")
    val options: String? = null,
    @field:Schema(description = "Validation rules (JSON string)", example = "{\"minLength\": 2, \"maxLength\": 50}")
    val validationRules: String? = null,
    @field:Schema(description = "Display order", example = "1")
    val displayOrder: Int = 0
)

@Schema(description = "Response DTO for a custom field")
data class CustomFieldResponse(
    @field:Schema(description = "Unique identifier of the custom field", example = "1")
    val id: Long,
    @field:Schema(description = "Entity type this field applies to", example = "LEAD")
    val entityType: String,
    @field:Schema(description = "Field name (internal identifier)", example = "custom_industry")
    val fieldName: String,
    @field:Schema(description = "Field label (display name)", example = "Industry")
    val fieldLabel: String,
    @field:Schema(description = "Field type", example = "SELECT")
    val fieldType: String,
    @field:Schema(description = "Whether the field is required", example = "false")
    val isRequired: Boolean,
    @field:Schema(description = "Default value for the field", example = "Technology")
    val defaultValue: String? = null,
    @field:Schema(description = "Options for SELECT and MULTI_SELECT fields (JSON string)", example = "[\"Technology\", \"Healthcare\", \"Finance\"]")
    val options: String? = null,
    @field:Schema(description = "Validation rules (JSON string)", example = "{\"minLength\": 2, \"maxLength\": 50}")
    val validationRules: String? = null,
    @field:Schema(description = "Display order", example = "1")
    val displayOrder: Int,
    @field:Schema(description = "Whether the field is active", example = "true")
    val isActive: Boolean,
    @field:Schema(description = "ID of the company this field belongs to", example = "1")
    val companyId: Long,
    @field:Schema(description = "ID of the user who created this field", example = "1")
    val createdBy: Long,
    @field:Schema(description = "Timestamp when the field was created", example = "2024-01-14T10:00:00")
    val createdAt: LocalDateTime,
    @field:Schema(description = "Timestamp when the field was last updated", example = "2024-01-14T10:00:00")
    val updatedAt: LocalDateTime
)

@Schema(description = "Request DTO for updating a custom field")
data class CustomFieldUpdateRequest(
    @field:Schema(description = "Field label (display name)", example = "Industry")
    val fieldLabel: String? = null,
    @field:Schema(description = "Field type", example = "SELECT")
    val fieldType: String? = null,
    @field:Schema(description = "Whether the field is required", example = "false")
    val isRequired: Boolean? = null,
    @field:Schema(description = "Default value for the field", example = "Technology")
    val defaultValue: String? = null,
    @field:Schema(description = "Options for SELECT and MULTI_SELECT fields (JSON string)", example = "[\"Technology\", \"Healthcare\", \"Finance\"]")
    val options: String? = null,
    @field:Schema(description = "Validation rules (JSON string)", example = "{\"minLength\": 2, \"maxLength\": 50}")
    val validationRules: String? = null,
    @field:Schema(description = "Display order", example = "1")
    val displayOrder: Int? = null,
    @field:Schema(description = "Whether the field is active", example = "true")
    val isActive: Boolean? = null
)

@Schema(description = "Configuration response for custom fields by entity type")
data class CustomFieldConfiguration(
    @field:Schema(description = "Entity type", example = "LEAD")
    val entityType: String,
    @field:Schema(description = "List of core fields for this entity type")
    val coreFields: List<CoreFieldResponse>,
    @field:Schema(description = "List of custom fields for this entity type")
    val customFields: List<CustomFieldResponse>,
    @field:Schema(description = "Total number of fields")
    val totalFields: Int
)

@Schema(description = "Response DTO for a core field")
data class CoreFieldResponse(
    @field:Schema(description = "Field identifier", example = "firstName")
    val id: String,
    @field:Schema(description = "Field name (internal identifier)", example = "firstName")
    val fieldName: String,
    @field:Schema(description = "Field label (display name)", example = "First Name")
    val fieldLabel: String,
    @field:Schema(description = "Field type", example = "TEXT")
    val fieldType: String,
    @field:Schema(description = "Whether the field is required", example = "true")
    val isRequired: Boolean,
    @field:Schema(description = "Options for SELECT fields", example = "[\"NEW\", \"CONTACTED\", \"QUALIFIED\"]")
    val options: List<String>? = null
)
