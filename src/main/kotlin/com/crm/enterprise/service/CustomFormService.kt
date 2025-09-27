package com.crm.enterprise.service

import com.crm.enterprise.dto.*
import com.crm.enterprise.entity.*
import com.crm.enterprise.repository.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class CustomFormService(
    private val leadRepository: LeadRepository,
    private val contactRepository: ContactRepository,
    private val customFormRepository: CustomFormRepository,
    private val customFormFieldRepository: CustomFormFieldRepository,
    private val customFormSubmissionRepository: CustomFormSubmissionRepository,
    private val customFormAccessRepository: CustomFormAccessRepository,
    private val fileUploadService: FileUploadService,
    private val formActivityService: FormActivityService,
    private val whatsAppFormService: WhatsAppFormService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(CustomFormService::class.java)

    /**
     * Create a new custom form
     */
    fun createForm(formDto: CustomFormDto): CustomFormDto {
        val form = CustomForm(
            name = formDto.name,
            description = formDto.description,
            companyId = formDto.companyId,
            createdBy = formDto.createdBy,
            status = formDto.status,
            isPublic = formDto.isPublic,
            allowMultipleSubmissions = formDto.allowMultipleSubmissions,
            expiresAt = formDto.expiresAt,
            maxSubmissions = formDto.maxSubmissions,
            redirectUrl = formDto.redirectUrl,
            successMessage = formDto.successMessage,
            theme = formDto.theme,
            settings = formDto.settings
        )
        
        val savedForm = customFormRepository.save(form)
        
        // Save form fields
        val fields = formDto.fields.map { fieldDto ->
            CustomFormField(
                formId = savedForm.id!!,
                name = fieldDto.name,
                label = fieldDto.label,
                type = fieldDto.type,
                isRequired = fieldDto.isRequired,
                placeholder = fieldDto.placeholder,
                helpText = fieldDto.helpText,
                options = fieldDto.options,
                validation =  ObjectMapper().writeValueAsString(fieldDto.validation),
                order = fieldDto.order,
                isActive = fieldDto.isActive
            )
        }
        
        customFormFieldRepository.saveAll(fields)
        
        logger.info("Created custom form: ${savedForm.id} for company: ${formDto.companyId}")
        
        return convertToDto(savedForm, fields)
    }

    /**
     * Update an existing form
     */
    fun updateForm(formId: Long, formDto: CustomFormDto, companyId: Long): CustomFormDto {
        val existingForm = customFormRepository.findById(formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        if (existingForm.companyId != companyId) {
            throw RuntimeException("Unauthorized to update this form")
        }
        
        val updatedForm = existingForm.copy(
            name = formDto.name,
            description = formDto.description,
            status = formDto.status,
            isPublic = formDto.isPublic,
            allowMultipleSubmissions = formDto.allowMultipleSubmissions,
            expiresAt = formDto.expiresAt,
            maxSubmissions = formDto.maxSubmissions,
            redirectUrl = formDto.redirectUrl,
            successMessage = formDto.successMessage,
            theme = formDto.theme,
            settings = formDto.settings,
            updatedAt = LocalDateTime.now()
        )
        
        val savedForm = customFormRepository.save(updatedForm)
        
        // Update form fields
        val existingFields = customFormFieldRepository.findByFormIdOrderByOrderAsc(formId)
        customFormFieldRepository.deleteAll(existingFields)
        
        val newFields = formDto.fields.map { fieldDto ->
            CustomFormField(
                formId = savedForm.id!!,
                name = fieldDto.name,
                label = fieldDto.label,
                type = fieldDto.type,
                isRequired = fieldDto.isRequired,
                placeholder = fieldDto.placeholder,
                helpText = fieldDto.helpText,
                options = fieldDto.options,
                validation = ObjectMapper().writeValueAsString(fieldDto.validation),
                order = fieldDto.order,
                isActive = fieldDto.isActive
            )
        }
        
        customFormFieldRepository.saveAll(newFields)
        
        logger.info("Updated custom form: $formId")
        
        return convertToDto(savedForm, newFields)
    }

    /**
     * Get all forms for a company
     */
    fun getFormsByCompany(companyId: Long): List<CustomFormDto> {
        val forms = customFormRepository.findByCompanyIdAndDeletedAtIsNull(companyId)
        return forms.map { form ->
            val fields = customFormFieldRepository.findByFormIdOrderByOrderAsc(form.id!!)
            val submissionCount = customFormRepository.countSubmissionsByFormId(form.id)
            convertToDto(form, fields, submissionCount)
        }
    }

    /**
     * Get a specific form by ID
     */
    fun getFormById(formId: Long, companyId: Long): CustomFormDto {
        val form = customFormRepository.findById(formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        if (form.companyId != companyId) {
            throw RuntimeException("Unauthorized to access this form")
        }
        
        val fields = customFormFieldRepository.findByFormIdOrderByOrderAsc(formId)
        val submissionCount = customFormRepository.countSubmissionsByFormId(formId)
        
        return convertToDto(form, fields, submissionCount)
    }

    /**
     * Get a public form for submission
     */
    fun getPublicForm(formId: Long): CustomFormDto {
        val form = customFormRepository.findPublicFormById(formId, LocalDateTime.now())
            ?: throw RuntimeException("Form not found or not accessible")
        
        val fields = customFormFieldRepository.findActiveFieldsByFormId(formId)
        
        return convertToDto(form, fields)
    }

    /**
     * Submit a form
     */
    fun submitForm(submissionRequest: FormSubmissionRequest): CustomFormSubmissionDto {
        val form = customFormRepository.findPublicFormById(submissionRequest.formId, LocalDateTime.now())
            ?: throw RuntimeException("Form not found or not accessible")
        
        // Check if form allows multiple submissions
        if (!form.allowMultipleSubmissions && submissionRequest.submittedByEmail != null) {
            val existingSubmission = customFormSubmissionRepository.findByFormIdAndEmail(
                submissionRequest.formId, 
                submissionRequest.submittedByEmail
            )
            if (existingSubmission.isNotEmpty()) {
                throw RuntimeException("Form does not allow multiple submissions")
            }
        }
        
        // Check max submissions limit
        if (form.maxSubmissions != null) {
            val currentSubmissions = customFormRepository.countSubmissionsByFormId(submissionRequest.formId)
            if (currentSubmissions >= form.maxSubmissions) {
                throw RuntimeException("Form has reached maximum submission limit")
            }
        }
        
        val submission = CustomFormSubmission(
            formId = submissionRequest.formId,
            submittedBy = submissionRequest.submittedBy,
            submittedByEmail = submissionRequest.submittedByEmail,
            submittedByPhone = submissionRequest.submittedByPhone,
            submissionData = objectMapper.writeValueAsString(submissionRequest.submissionData),
            status = "PENDING"
        )
        
        val savedSubmission = customFormSubmissionRepository.save(submission)
        
        logger.info("Form submitted: ${savedSubmission.id} for form: ${submissionRequest.formId}")
        
        return convertSubmissionToDto(savedSubmission)
    }

    /**
     * Get form submissions
     */
    fun getFormSubmissions(formId: Long, companyId: Long): List<CustomFormSubmissionDto> {
        val form = customFormRepository.findById(formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        if (form.companyId != companyId) {
            throw RuntimeException("Unauthorized to access this form")
        }
        
        val submissions = customFormSubmissionRepository.findByFormIdOrderByCreatedAtDesc(formId)
        return submissions.map { convertSubmissionToDto(it) }
    }

    /**
     * Get form analytics
     */
    fun getFormAnalytics(formId: Long, companyId: Long): FormAnalyticsDto {
        val form = customFormRepository.findById(formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        if (form.companyId != companyId) {
            throw RuntimeException("Unauthorized to access this form")
        }
        
        val totalSubmissions = customFormRepository.countSubmissionsByFormId(formId)
        val submissionsToday = customFormSubmissionRepository.countSubmissionsSince(
            formId, 
            LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
        )
        val submissionsThisWeek = customFormSubmissionRepository.countSubmissionsSince(
            formId, 
            LocalDateTime.now().minusWeeks(1)
        )
        val submissionsThisMonth = customFormSubmissionRepository.countSubmissionsSince(
            formId, 
            LocalDateTime.now().minusMonths(1)
        )
        
        return FormAnalyticsDto(
            formId = formId,
            formName = form.name,
            totalSubmissions = totalSubmissions,
            submissionsToday = submissionsToday,
            submissionsThisWeek = submissionsThisWeek,
            submissionsThisMonth = submissionsThisMonth
        )
    }

    /**
     * Delete a form (soft delete)
     */
    fun deleteForm(formId: Long, companyId: Long): Boolean {
        val form = customFormRepository.findById(formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        if (form.companyId != companyId) {
            throw RuntimeException("Unauthorized to delete this form")
        }
        
        val updatedForm = form.copy(deletedAt = LocalDateTime.now())
        customFormRepository.save(updatedForm)
        
        logger.info("Deleted custom form: $formId")
        
        return true
    }

    /**
     * Create form access for a lead or contact
     */
    fun createFormAccess(accessRequest: FormAccessRequest, companyId: Long, createdBy: Long): CustomFormAccessDto {
        val form = customFormRepository.findById(accessRequest.formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        if (form.companyId != companyId) {
            throw RuntimeException("Unauthorized to create access for this form")
        }
        
        // Check if access already exists
        val existingAccess = when {
            accessRequest.leadId != null -> customFormAccessRepository.findByFormIdAndLeadIdAndIsActiveTrue(
                accessRequest.formId, accessRequest.leadId
            )
            accessRequest.contactId != null -> customFormAccessRepository.findByFormIdAndContactIdAndIsActiveTrue(
                accessRequest.formId, accessRequest.contactId
            )
            else -> null
        }
        
        if (existingAccess != null) {
            // Update existing access
            val updatedAccess = existingAccess.copy(
                isActive = true,
                expiresAt = accessRequest.expiryDays?.let { 
                    LocalDateTime.now().plusDays(it.toLong()) 
                },
                maxSubmissions = accessRequest.maxSubmissions,
                updatedAt = LocalDateTime.now()
            )
            val savedAccess = customFormAccessRepository.save(updatedAccess)
            return convertAccessToDto(savedAccess)
        }
        
        // Create new access
        val accessToken = generateAccessToken()
        val expiresAt = accessRequest.expiryDays?.let { 
            LocalDateTime.now().plusDays(it.toLong()) 
        }
        
        val formAccess = CustomFormAccess(
            formId = accessRequest.formId,
            leadId = accessRequest.leadId,
            contactId = accessRequest.contactId,
            accessToken = accessToken,
            isActive = true,
            expiresAt = expiresAt,
            maxSubmissions = accessRequest.maxSubmissions,
            companyId = companyId,
            createdBy = createdBy
        )
        
        val savedAccess = customFormAccessRepository.save(formAccess)
        
        // Create FORM_SHARED activity
        formActivityService.createFormSharedActivity(
            formId = accessRequest.formId,
            formName = form.name,
            leadId = accessRequest.leadId,
            contactId = accessRequest.contactId,
            assignedTo = createdBy,
            assignedBy = createdBy,
            companyId = companyId,
            accessToken = accessToken
        )
        
        logger.info("Created form access for form ${accessRequest.formId}, lead: ${accessRequest.leadId}, contact: ${accessRequest.contactId}")
        
        return convertAccessToDto(savedAccess)
    }

    /**
     * Get form by access token (for public access)
     */
    fun getFormByAccessToken(accessToken: String): CustomFormDto {
        val access = customFormAccessRepository.findActiveAccessByToken(accessToken, LocalDateTime.now())
            ?: throw RuntimeException("Invalid or expired access token")
        
        val form = customFormRepository.findById(access.formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        // Update last accessed time
        val updatedAccess = access.copy(lastAccessedAt = LocalDateTime.now())
        customFormAccessRepository.save(updatedAccess)
        
        val fields = customFormFieldRepository.findActiveFieldsByFormId(access.formId)
        
        return convertToDto(form, fields)
    }

    /**
     * Submit form with access token
     */
    fun submitFormWithAccess(submissionRequest: FormSubmissionWithFiles): CustomFormSubmissionDto {
        val access = customFormAccessRepository.findActiveAccessByToken(
            submissionRequest.accessToken ?: "", 
            LocalDateTime.now()
        ) ?: throw RuntimeException("Invalid or expired access token")
        
        val form = customFormRepository.findById(access.formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        // Check submission limits
        if (access.maxSubmissions != null && access.submissionCount >= access.maxSubmissions) {
            throw RuntimeException("Maximum submissions reached for this form access")
        }
        
        // Handle file uploads
        val processedSubmissionData = processFileUploads(
            submissionRequest.submissionData, 
            submissionRequest.files, 
            form.companyId, 
            form.id!!
        )
        
        val submission = CustomFormSubmission(
            formId = form.id,
            submittedBy = submissionRequest.submittedBy,
            submittedByEmail = submissionRequest.submittedByEmail,
            submittedByPhone = submissionRequest.submittedByPhone,
            submissionData = objectMapper.writeValueAsString(processedSubmissionData),
            status = "PENDING",
            leadId = access.leadId,
            contactId = access.contactId,
            accessToken = submissionRequest.accessToken
        )
        
        val savedSubmission = customFormSubmissionRepository.save(submission)
        
        // Create FORM_SUBMITTED activity
        formActivityService.createFormSubmittedActivity(
            formId = form.id,
            formName = form.name,
            leadId = access.leadId,
            contactId = access.contactId,
            assignedTo = access.createdBy,
            assignedBy = access.createdBy,
            companyId = form.companyId,
            submissionData = processedSubmissionData
        )
        
        // Update lead/contact data from form submission
        if (access.leadId != null) {
            formActivityService.updateLeadFromFormSubmission(access.leadId, processedSubmissionData)
        }
        if (access.contactId != null) {
            formActivityService.updateContactFromFormSubmission(access.contactId, processedSubmissionData)
        }
        
        // Update access submission count
        val updatedAccess = access.copy(
            submissionCount = access.submissionCount + 1,
            updatedAt = LocalDateTime.now()
        )
        customFormAccessRepository.save(updatedAccess)
        
        logger.info("Form submitted with access token: ${submissionRequest.accessToken}")
        
        return convertSubmissionToDto(savedSubmission)
    }

    /**
     * Get form access history for a lead/contact
     */
    fun getFormAccessHistory(formId: Long, leadId: Long?, contactId: Long?, companyId: Long): List<CustomFormAccessDto> {
        val form = customFormRepository.findById(formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        if (form.companyId != companyId) {
            throw RuntimeException("Unauthorized to access this form")
        }
        
        val accessHistory = when {
            leadId != null -> customFormAccessRepository.findLeadAccessHistory(formId, leadId)
            contactId != null -> customFormAccessRepository.findContactAccessHistory(formId, contactId)
            else ->customFormAccessRepository.findByFormIdAndIsActiveTrue(formId)
        }
        return accessHistory.map { convertAccessToDto(it) }
    }

    /**
     * Deactivate form access
     */
    fun deactivateFormAccess(accessId: Long, companyId: Long): Boolean {
        val access = customFormAccessRepository.findById(accessId)
            .orElseThrow { RuntimeException("Form access not found") }
        
        if (access.companyId != companyId) {
            throw RuntimeException("Unauthorized to modify this form access")
        }
        
        val updatedAccess = access.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
        customFormAccessRepository.save(updatedAccess)
        
        logger.info("Deactivated form access: $accessId")
        
        return true
    }

    /**
     * Regenerate access token
     */
    fun regenerateAccessToken(accessId: Long, companyId: Long): CustomFormAccessDto {
        val access = customFormAccessRepository.findById(accessId)
            .orElseThrow { RuntimeException("Form access not found") }
        
        if (access.companyId != companyId) {
            throw RuntimeException("Unauthorized to modify this form access")
        }
        
        val newAccessToken = generateAccessToken()
        val updatedAccess = access.copy(
            accessToken = newAccessToken,
            isActive = true,
            updatedAt = LocalDateTime.now()
        )
        
        val savedAccess = customFormAccessRepository.save(updatedAccess)
        
        logger.info("Regenerated access token for access: $accessId")
        
        return convertAccessToDto(savedAccess)
    }

    private fun processFileUploads(
        submissionData: Map<String, Any>, 
        files: List<FormFileDto>, 
        companyId: Long, 
        formId: Long
    ): Map<String, Any> {
        val processedData = submissionData.toMutableMap()
        
        files.forEach { file ->
            try {
                val filePath = fileUploadService.uploadFileFromBase64(
                    file.fileData,
                    file.fileName,
                    file.mimeType,
                    companyId,
                    formId
                )
                
                processedData[file.fieldName] = mapOf(
                    "fileName" to file.fileName,
                    "filePath" to filePath,
                    "fileType" to file.fileType,
                    "fileSize" to file.fileSize,
                    "mimeType" to file.mimeType
                )
            } catch (e: Exception) {
                logger.error("Error processing file upload for field ${file.fieldName}", e)
                processedData[file.fieldName] = mapOf(
                    "error" to "Failed to upload file: ${e.message}"
                )
            }
        }
        
        return processedData
    }

    private fun generateAccessToken(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * Generate WhatsApp share URL for form access
     */
    fun generateWhatsAppShareUrl(
        accessId: Long,
        baseUrl: String,
        companyId: Long
    ): String? {
        val access = customFormAccessRepository.findById(accessId)
            .orElseThrow { RuntimeException("Form access not found") }
        
        if (access.companyId != companyId) {
            throw RuntimeException("Unauthorized to access this form access")
        }
        
        val form = customFormRepository.findById(access.formId)
            .orElseThrow { RuntimeException("Form not found") }
        
        // Get phone number from lead or contact
        var phone = ""
        if(access.leadId != null) {
            // Get lead phone - you'll need to inject LeadRepository
            val lead: Lead = leadRepository.findById(access.leadId).getOrNull()!!
            phone = lead.phone ?: ""
        }
        if(phone == "" && access.contactId != null) {
            // Get lead phone - you'll need to inject LeadRepository
            val contact: Contact = contactRepository.findById(access.contactId).getOrNull()!!
            phone = contact.phone ?: ""
        }

        if(phone == ""){
            return null;
        }
        
        return whatsAppFormService.generateFormShareWhatsAppUrl(
                phone = phone,
                formName = form.name,
                formDescription = form.description,
                accessToken = access.accessToken,
                leadId = access.leadId,
                contactId = access.contactId,
                baseUrl = baseUrl
            )

    }

    private fun convertAccessToDto(access: CustomFormAccess): CustomFormAccessDto {
        return CustomFormAccessDto(
            id = access.id,
            formId = access.formId,
            leadId = access.leadId,
            contactId = access.contactId,
            accessToken = access.accessToken,
            isActive = access.isActive,
            expiresAt = access.expiresAt,
            lastAccessedAt = access.lastAccessedAt,
            submissionCount = access.submissionCount,
            maxSubmissions = access.maxSubmissions,
            shareUrl = null, // Will be set by controller
            createdAt = access.createdAt
        )
    }

    private fun convertToDto(form: CustomForm, fields: List<CustomFormField>, submissionCount: Long = 0): CustomFormDto {
        return CustomFormDto(
            id = form.id,
            name = form.name,
            description = form.description,
            companyId = form.companyId,
            createdBy = form.createdBy,
            status = form.status,
            isPublic = form.isPublic,
            allowMultipleSubmissions = form.allowMultipleSubmissions,
            expiresAt = form.expiresAt,
            maxSubmissions = form.maxSubmissions,
            redirectUrl = form.redirectUrl,
            successMessage = form.successMessage,
            theme = form.theme,
            settings = form.settings,
            fields = fields.map { field ->
                CustomFormFieldDto(
                    id = field.id,
                    formId = field.formId,
                    name = field.name,
                    label = field.label,
                    type = field.type,
                    isRequired = field.isRequired,
                    placeholder = field.placeholder,
                    helpText = field.helpText,
                    options = field.options,
                    validation = ObjectMapper().readValue(field.validation, Validation::class.java),
                    order = field.order,
                    isActive = field.isActive
                )
            },
            submissionCount = submissionCount,
            createdAt = form.createdAt,
            updatedAt = form.updatedAt
        )
    }

    private fun convertSubmissionToDto(submission: CustomFormSubmission): CustomFormSubmissionDto {
        val submissionData = try {
            objectMapper.readValue(submission.submissionData, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            emptyMap<String, Any>()
        }
        
        return CustomFormSubmissionDto(
            id = submission.id,
            formId = submission.formId,
            submittedBy = submission.submittedBy,
            submittedByEmail = submission.submittedByEmail,
            submittedByPhone = submission.submittedByPhone,
            submissionData = submissionData,
            status = submission.status,
            processedAt = submission.processedAt,
            processedBy = submission.processedBy,
            notes = submission.notes,
            createdAt = submission.createdAt
        )
    }
}
