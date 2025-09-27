package com.crm.enterprise.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

enum class FormStatus {
    DRAFT, ACTIVE, INACTIVE, EXPIRED
}

enum class FieldType {
    TEXT, EMAIL, PHONE, NUMBER, TEXTAREA, SELECT, RADIO, CHECKBOX, DATE, FILE, URL, IMAGE
}

enum class FormAccessType {
    PUBLIC, LEAD_SPECIFIC, CONTACT_SPECIFIC
}

@Table("custom_forms")
data class CustomForm(
    @Id val id: Long? = null,
    val name: String,
    val description: String? = null,
    val companyId: Long,
    val createdBy: Long,
    val status: FormStatus = FormStatus.DRAFT,
    val isPublic: Boolean = true,
    val accessType: FormAccessType = FormAccessType.PUBLIC,
    val leadId: Long? = null, // For lead-specific forms
    val contactId: Long? = null, // For contact-specific forms
    val allowMultipleSubmissions: Boolean = false,
    val expiresAt: LocalDateTime? = null,
    val maxSubmissions: Int? = null,
    val submissionExpiryDays: Int? = null, // Days after which form becomes inactive for this lead/contact
    val redirectUrl: String? = null,
    val successMessage: String? = null,
    val theme: String? = null, // JSON string for custom styling
    val settings: String? = null, // JSON string for form settings
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null
)

@Table("custom_form_fields")
data class CustomFormField(
    @Id
    val id: String? = null,
    val formId: Long,
    val name: String,
    val label: String,
    val type: FieldType,
    val isRequired: Boolean = false,
    val placeholder: String? = null,
    val helpText: String? = null,
    val options: String? = null, // JSON string for select/radio/checkbox options
    val validation: String? = null, // JSON string for validation rules
    val order: Int = 0,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)




@Table("custom_form_submissions")
data class CustomFormSubmission(
    @Id
    val id: Long? = null,
    val formId: Long,
    val submittedBy: String? = null, // Email or name of submitter
    val submittedByEmail: String? = null,
    val submittedByPhone: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val referrer: String? = null,
    val submissionData: String, // JSON string of form data
    val status: String = "PENDING", // PENDING, PROCESSED, REJECTED
    val processedAt: LocalDateTime? = null,
    val processedBy: Long? = null,
    val notes: String? = null,
    val leadId: Long? = null, // Link to lead if form was lead-specific
    val contactId: Long? = null, // Link to contact if form was contact-specific
    val accessToken: String? = null, // Token used to access the form
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Table("custom_form_access")
data class CustomFormAccess(
    @Id
    val id: Long? = null,
    val formId: Long,
    val leadId: Long? = null,
    val contactId: Long? = null,
    val accessToken: String, // Unique token for accessing the form
    val isActive: Boolean = true,
    val expiresAt: LocalDateTime? = null,
    val lastAccessedAt: LocalDateTime? = null,
    val submissionCount: Int = 0,
    val maxSubmissions: Int? = null,
    val companyId: Long,
    val createdBy: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
