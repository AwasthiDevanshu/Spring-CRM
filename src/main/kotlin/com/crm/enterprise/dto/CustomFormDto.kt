package com.crm.enterprise.dto

import com.crm.enterprise.entity.FieldType
import com.crm.enterprise.entity.FormStatus
import com.crm.enterprise.entity.FormAccessType
import java.time.LocalDateTime

data class CustomFormDto(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val companyId: Long,
    val createdBy: Long,
    val status: FormStatus = FormStatus.DRAFT,
    val isPublic: Boolean = true,
    val accessType: FormAccessType = FormAccessType.PUBLIC,
    val leadId: Long? = null,
    val contactId: Long? = null,
    val allowMultipleSubmissions: Boolean = false,
    val expiresAt: LocalDateTime? = null,
    val maxSubmissions: Int? = null,
    val submissionExpiryDays: Int? = null,
    val redirectUrl: String? = null,
    val successMessage: String? = null,
    val theme: String? = null,
    val settings: String? = null,
    val fields: List<CustomFormFieldDto> = emptyList(),
    val submissionCount: Long = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class CustomFormFieldDto(
    val id: String? = null,
    val formId: Long? = null,
    val name: String,
    val label: String,
    val type: FieldType,
    val isRequired: Boolean = false,
    val placeholder: String? = null,
    val helpText: String? = null,
    val options: String? = null,
    val validation: String? = null,
    val order: Int = 0,
    val isActive: Boolean = true
)

data class CustomFormSubmissionDto(
    val id: Long? = null,
    val formId: Long,
    val submittedBy: String? = null,
    val submittedByEmail: String? = null,
    val submittedByPhone: String? = null,
    val submissionData: Map<String, Any>,
    val status: String = "PENDING",
    val processedAt: LocalDateTime? = null,
    val processedBy: Long? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime? = null
)

data class FormSubmissionRequest(
    val formId: Long,
    val submissionData: Map<String, Any>,
    val submittedBy: String? = null,
    val submittedByEmail: String? = null,
    val submittedByPhone: String? = null
)

data class FormAnalyticsDto(
    val formId: Long,
    val formName: String,
    val totalSubmissions: Long,
    val submissionsToday: Long,
    val submissionsThisWeek: Long,
    val submissionsThisMonth: Long,
    val conversionRate: Double? = null,
    val averageSubmissionTime: Double? = null,
    val topReferrers: List<String> = emptyList(),
    val submissionTrends: Map<String, Long> = emptyMap()
)

data class CustomFormAccessDto(
    val id: Long? = null,
    val formId: Long,
    val leadId: Long? = null,
    val contactId: Long? = null,
    val accessToken: String,
    val isActive: Boolean = true,
    val expiresAt: LocalDateTime? = null,
    val lastAccessedAt: LocalDateTime? = null,
    val submissionCount: Int = 0,
    val maxSubmissions: Int? = null,
    val shareUrl: String? = null,
    val createdAt: LocalDateTime? = null
)

data class FormAccessRequest(
    val formId: Long,
    val leadId: Long? = null,
    val contactId: Long? = null,
    val expiryDays: Int? = null,
    val maxSubmissions: Int? = null
)

data class FormSubmissionWithFiles(
    val formId: Long,
    val submissionData: Map<String, Any>,
    val files: List<FormFileDto> = emptyList(),
    val submittedBy: String? = null,
    val submittedByEmail: String? = null,
    val submittedByPhone: String? = null,
    val accessToken: String? = null
)

data class FormFileDto(
    val fieldName: String,
    val fileName: String,
    val fileType: String,
    val fileSize: Long,
    val fileData: String, // Base64 encoded file data
    val mimeType: String
)
