package com.crm.enterprise.service

import com.crm.enterprise.dto.CustomFieldConfiguration
import com.crm.enterprise.dto.CustomFieldRequest
import com.crm.enterprise.dto.CustomFieldResponse
import com.crm.enterprise.dto.CustomFieldUpdateRequest
import com.crm.enterprise.dto.CoreFieldResponse
import com.crm.enterprise.entity.CustomField
import com.crm.enterprise.repository.CustomFieldRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CustomFieldService(
    private val customFieldRepository: CustomFieldRepository
) {

    fun getFieldConfiguration(companyId: Long, entityType: String): CustomFieldConfiguration {
        val customFields = customFieldRepository.findByCompanyIdAndEntityType(companyId, entityType)
            .map { toCustomFieldResponse(it) }
        
        val coreFields = getCoreFieldsForEntityType(entityType)
        
        return CustomFieldConfiguration(
            entityType = entityType,
            coreFields = coreFields,
            customFields = customFields,
            totalFields = coreFields.size + customFields.size
        )
    }
    
    private fun getCoreFieldsForEntityType(entityType: String): List<CoreFieldResponse> {
        return when (entityType.uppercase()) {
            "LEAD" -> listOf(
                CoreFieldResponse("firstName", "firstName", "First Name", "TEXT", true),
                CoreFieldResponse("lastName", "lastName", "Last Name", "TEXT", true),
                CoreFieldResponse("email", "email", "Email", "EMAIL", true),
                CoreFieldResponse("phone", "phone", "Phone", "PHONE", false),
                CoreFieldResponse("company", "company", "Company", "TEXT", false),
                CoreFieldResponse("jobTitle", "jobTitle", "Job Title", "TEXT", false),
                CoreFieldResponse("status", "status", "Status", "SELECT", true, 
                    listOf("NEW", "CONTACTED", "QUALIFIED", "PROPOSAL", "NEGOTIATION", "CLOSED_WON", "CLOSED_LOST")),
                CoreFieldResponse("source", "source", "Source", "SELECT", true,
                    listOf("WEBSITE", "PHONE", "EMAIL", "FACEBOOK_ADS", "GOOGLE_ADS", "REFERRAL", "OTHER")),
                CoreFieldResponse("score", "score", "Score", "NUMBER", false),
                CoreFieldResponse("notes", "notes", "Notes", "TEXTAREA", false)
            )
            "CONTACT" -> listOf(
                CoreFieldResponse("firstName", "firstName", "First Name", "TEXT", true),
                CoreFieldResponse("lastName", "lastName", "Last Name", "TEXT", true),
                CoreFieldResponse("email", "email", "Email", "EMAIL", true),
                CoreFieldResponse("phone", "phone", "Phone", "PHONE", false),
                CoreFieldResponse("company", "company", "Company", "TEXT", false),
                CoreFieldResponse("jobTitle", "jobTitle", "Job Title", "TEXT", false),
                CoreFieldResponse("status", "status", "Status", "SELECT", true,
                    listOf("ACTIVE", "INACTIVE", "PROSPECT", "CUSTOMER", "PARTNER")),
                CoreFieldResponse("notes", "notes", "Notes", "TEXTAREA", false)
            )
            "DEAL" -> listOf(
                CoreFieldResponse("name", "name", "Deal Name", "TEXT", true),
                CoreFieldResponse("description", "description", "Description", "TEXTAREA", false),
                CoreFieldResponse("value", "value", "Value", "NUMBER", true),
                CoreFieldResponse("currency", "currency", "Currency", "SELECT", true,
                    listOf("USD", "EUR", "GBP", "INR", "CAD", "AUD")),
                CoreFieldResponse("status", "status", "Status", "SELECT", true,
                    listOf("PROSPECTING", "QUALIFICATION", "PROPOSAL", "NEGOTIATION", "CLOSED_WON", "CLOSED_LOST")),
                CoreFieldResponse("probability", "probability", "Probability (%)", "NUMBER", false),
                CoreFieldResponse("expectedCloseDate", "expectedCloseDate", "Expected Close Date", "DATE", false),
                CoreFieldResponse("notes", "notes", "Notes", "TEXTAREA", false)
            )
            else -> emptyList()
        }
    }

    fun getAllCustomFields(companyId: Long): List<CustomFieldResponse> {
        return customFieldRepository.findByCompanyId(companyId)
            .map { toCustomFieldResponse(it) }
    }

    fun getCustomFieldById(id: Long, companyId: Long): CustomFieldResponse? {
        return customFieldRepository.findById(id)
            .filter { it.companyId == companyId }
            .map { toCustomFieldResponse(it) }
            .orElse(null)
    }

    fun createCustomField(request: CustomFieldRequest, companyId: Long, createdBy: Long = 1L): CustomFieldResponse {
        // Check if field with same name already exists
        val existingField = customFieldRepository.findByCompanyIdAndFieldNameAndEntityType(
            companyId, request.fieldName, request.entityType
        )
        
        if (existingField != null) {
            throw IllegalArgumentException("Custom field with name '${request.fieldName}' already exists for entity type '${request.entityType}'")
        }

        val customField = CustomField(
            entityType = request.entityType,
            fieldName = request.fieldName,
            fieldLabel = request.fieldLabel,
            fieldType = request.fieldType,
            isRequired = request.isRequired,
            defaultValue = request.defaultValue,
            options = request.options,
            validationRules = request.validationRules,
            displayOrder = request.displayOrder,
            companyId = companyId,
            createdBy = createdBy
        )

        return toCustomFieldResponse(customFieldRepository.save(customField))
    }

    fun updateCustomField(id: Long, request: CustomFieldUpdateRequest, companyId: Long): CustomFieldResponse? {
        return customFieldRepository.findById(id)
            .filter { it.companyId == companyId }
            .map { existingField ->
                val updatedField = existingField.copy(
                    fieldLabel = request.fieldLabel ?: existingField.fieldLabel,
                    fieldType = request.fieldType ?: existingField.fieldType,
                    isRequired = request.isRequired ?: existingField.isRequired,
                    defaultValue = request.defaultValue ?: existingField.defaultValue,
                    options = request.options ?: existingField.options,
                    validationRules = request.validationRules ?: existingField.validationRules,
                    displayOrder = request.displayOrder ?: existingField.displayOrder,
                    isActive = request.isActive ?: existingField.isActive,
                    updatedAt = LocalDateTime.now()
                )
                toCustomFieldResponse(customFieldRepository.save(updatedField))
            }.orElse(null)
    }

    fun deleteCustomField(id: Long, companyId: Long): Boolean {
        return customFieldRepository.findById(id)
            .filter { it.companyId == companyId }
            .map {
                customFieldRepository.delete(it)
                true
            }.orElse(false)
    }

    fun toCustomFieldResponse(customField: CustomField): CustomFieldResponse {
        return CustomFieldResponse(
            id = customField.id ?: 0L,
            entityType = customField.entityType,
            fieldName = customField.fieldName,
            fieldLabel = customField.fieldLabel,
            fieldType = customField.fieldType,
            isRequired = customField.isRequired,
            defaultValue = customField.defaultValue,
            options = customField.options,
            validationRules = customField.validationRules,
            displayOrder = customField.displayOrder,
            isActive = customField.isActive,
            companyId = customField.companyId,
            createdBy = customField.createdBy,
            createdAt = customField.createdAt,
            updatedAt = customField.updatedAt
        )
    }
}
