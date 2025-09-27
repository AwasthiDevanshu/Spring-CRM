package com.crm.enterprise.controller

import com.crm.enterprise.dto.*
import com.crm.enterprise.service.CustomFormService
import com.crm.enterprise.util.RequestUtils
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/custom-forms")
class CustomFormController(
    private val customFormService: CustomFormService,
    private val requestUtils: RequestUtils
) {
    private val logger = LoggerFactory.getLogger(CustomFormController::class.java)

    /**
     * Create a new custom form
     */
    @PostMapping
    fun createForm(
        @RequestBody formDto: CustomFormDto,
        request: HttpServletRequest
    ): ResponseEntity<CustomFormDto> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val userId = requestUtils.extractUserIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val isAdmin = requestUtils.extractIsCompanyAdminFromToken(request) ?: false
            val isSuperuser = requestUtils.extractIsSuperuserFromToken(request) ?: false
            
            if (!isAdmin && !isSuperuser) {
                return ResponseEntity.status(403).build()
            }
            
            val formWithCompany = formDto.copy(companyId = companyId, createdBy = userId)
            val createdForm = customFormService.createForm(formWithCompany)
            
            return ResponseEntity.ok(createdForm)
        } catch (e: Exception) {
            logger.error("Error creating custom form", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Update an existing form
     */
    @PutMapping("/{formId}")
    fun updateForm(
        @PathVariable formId: Long,
        @RequestBody formDto: CustomFormDto,
        request: HttpServletRequest
    ): ResponseEntity<CustomFormDto> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val isAdmin = requestUtils.extractIsCompanyAdminFromToken(request) ?: false
            val isSuperuser = requestUtils.extractIsSuperuserFromToken(request) ?: false
            
            if (!isAdmin && !isSuperuser) {
                return ResponseEntity.status(403).build()
            }
            
            val updatedForm = customFormService.updateForm(formId, formDto, companyId)
            return ResponseEntity.ok(updatedForm)
        } catch (e: Exception) {
            logger.error("Error updating custom form $formId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Get all forms for the company
     */
    @GetMapping
    fun getForms(request: HttpServletRequest): ResponseEntity<List<CustomFormDto>> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val forms = customFormService.getFormsByCompany(companyId)
            return ResponseEntity.ok(forms)
        } catch (e: Exception) {
            logger.error("Error fetching custom forms", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Get a specific form by ID
     */
    @GetMapping("/{formId}")
    fun getForm(
        @PathVariable formId: Long,
        request: HttpServletRequest
    ): ResponseEntity<CustomFormDto> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val form = customFormService.getFormById(formId, companyId)
            return ResponseEntity.ok(form)
        } catch (e: Exception) {
            logger.error("Error fetching custom form $formId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Get a public form for submission (no auth required)
     */
    @GetMapping("/public/{formId}")
    fun getPublicForm(@PathVariable formId: Long): ResponseEntity<CustomFormDto> {
        try {
            val form = customFormService.getPublicForm(formId)
            return ResponseEntity.ok(form)
        } catch (e: Exception) {
            logger.error("Error fetching public form $formId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Submit a form (no auth required)
     */
    @PostMapping("/submit")
    fun submitForm(
        @RequestBody submissionRequest: FormSubmissionRequest,
        request: HttpServletRequest
    ): ResponseEntity<CustomFormSubmissionDto> {
        try {
            val submission = customFormService.submitForm(submissionRequest)
            return ResponseEntity.ok(submission)
        } catch (e: Exception) {
            logger.error("Error submitting form", e)
            return ResponseEntity.badRequest().build()
        }
    }

    /**
     * Get form submissions
     */
    @GetMapping("/{formId}/submissions")
    fun getFormSubmissions(
        @PathVariable formId: Long,
        request: HttpServletRequest
    ): ResponseEntity<List<CustomFormSubmissionDto>> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val submissions = customFormService.getFormSubmissions(formId, companyId)
            return ResponseEntity.ok(submissions)
        } catch (e: Exception) {
            logger.error("Error fetching form submissions for form $formId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Get form analytics
     */
    @GetMapping("/{formId}/analytics")
    fun getFormAnalytics(
        @PathVariable formId: Long,
        request: HttpServletRequest
    ): ResponseEntity<FormAnalyticsDto> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val analytics = customFormService.getFormAnalytics(formId, companyId)
            return ResponseEntity.ok(analytics)
        } catch (e: Exception) {
            logger.error("Error fetching form analytics for form $formId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Delete a form
     */
    @DeleteMapping("/{formId}")
    fun deleteForm(
        @PathVariable formId: Long,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val deleted = customFormService.deleteForm(formId, companyId)
            return if (deleted) {
                ResponseEntity.ok("Form deleted successfully")
            } else {
                ResponseEntity.internalServerError().build()
            }
        } catch (e: Exception) {
            logger.error("Error deleting form $formId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Create form access for lead/contact
     */
    @PostMapping("/{formId}/access")
    fun createFormAccess(
        @PathVariable formId: Long,
        @RequestBody accessRequest: FormAccessRequest,
        request: HttpServletRequest
    ): ResponseEntity<CustomFormAccessDto> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val userId = requestUtils.extractUserIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val isAdmin = requestUtils.extractIsCompanyAdminFromToken(request) ?: false
            val isSuperuser = requestUtils.extractIsSuperuserFromToken(request) ?: false
            
            if (!isAdmin && !isSuperuser) {
                return ResponseEntity.status(403).build()
            }
            
            // Use formId from path variable, not from request body
            val accessRequestWithFormId = accessRequest.copy(formId = formId)
            val access = customFormService.createFormAccess(accessRequestWithFormId, companyId, userId)
            return ResponseEntity.ok(access)
        } catch (e: Exception) {
            logger.error("Error creating form access for form $formId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Get form by access token (public endpoint)
     */
    @GetMapping("/public/access/{accessToken}")
    fun getFormByAccessToken(@PathVariable accessToken: String): ResponseEntity<CustomFormDto> {
        try {
            val form = customFormService.getFormByAccessToken(accessToken)
            return ResponseEntity.ok(form)
        } catch (e: Exception) {
            logger.error("Error fetching form by access token", e)
            return ResponseEntity.badRequest().build()
        }
    }

    /**
     * Submit form with access token (public endpoint)
     */

    /**
     * Get form access history
     */
    @GetMapping("/{formId}/access-history")
    fun getFormAccessHistory(
        @PathVariable formId: Long,
        @RequestParam(required = false) leadId: Long?,
        @RequestParam(required = false) contactId: Long?,
        request: HttpServletRequest
    ): ResponseEntity<List<CustomFormAccessDto>> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val accessHistory = customFormService.getFormAccessHistory(formId, leadId, contactId, companyId)
            return ResponseEntity.ok(accessHistory)
        } catch (e: Exception) {
            logger.error("Error fetching form access history for form $formId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Deactivate form access
     */
    @DeleteMapping("/access/{accessId}")
    fun deactivateFormAccess(
        @PathVariable accessId: Long,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val deactivated = customFormService.deactivateFormAccess(accessId, companyId)
            return if (deactivated) {
                ResponseEntity.ok("Form access deactivated successfully")
            } else {
                ResponseEntity.internalServerError().build()
            }
        } catch (e: Exception) {
            logger.error("Error deactivating form access $accessId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Regenerate access token
     */
    @PostMapping("/access/{accessId}/regenerate")
    fun regenerateAccessToken(
        @PathVariable accessId: Long,
        request: HttpServletRequest
    ): ResponseEntity<CustomFormAccessDto> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val access = customFormService.regenerateAccessToken(accessId, companyId)
            return ResponseEntity.ok(access)
        } catch (e: Exception) {
            logger.error("Error regenerating access token for access $accessId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Generate WhatsApp share URL for form access
     */
    @GetMapping("/access/{accessId}/whatsapp-url")
    fun getWhatsAppShareUrl(
        @PathVariable accessId: Long,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            val baseUrl = "http://localhost:3001"
            val whatsAppUrl = customFormService.generateWhatsAppShareUrl(accessId, baseUrl, companyId)
            
            return if (whatsAppUrl != null) {
                ResponseEntity.ok(mapOf("whatsAppUrl" to whatsAppUrl))
            } else {
                ResponseEntity.ok(mapOf("error" to "No phone number available for WhatsApp sharing"))
            }
        } catch (e: Exception) {
            logger.error("Error generating WhatsApp URL for access $accessId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Get form share URL
     */
    @GetMapping("/{formId}/share-url")
    fun getFormShareUrl(
        @PathVariable formId: Long,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
                ?: return ResponseEntity.badRequest().build()
            
            // Verify form belongs to company
            customFormService.getFormById(formId, companyId)
            
            val shareUrl = "http://localhost:3001/forms"
            
            return ResponseEntity.ok(mapOf("shareUrl" to shareUrl))
        } catch (e: Exception) {
            logger.error("Error generating share URL for form $formId", e)
            return ResponseEntity.internalServerError().build()
        }
    }


    /**
     * Public endpoint to submit form with access token
     */
    @PostMapping("/public/submit")
    fun submitFormWithAccess(
        @RequestParam formData: Map<String, String>,
        @RequestParam(required = false) files: Map<String, MultipartFile>,
        @RequestHeader("X-Access-Token") accessToken: String
    ): ResponseEntity<Map<String, String>> {
        return try {
            val submissionRequest = FormSubmissionWithFiles(
                formId = 0L, // Will be determined by the service
                submissionData = formData.mapValues { it.value as Any },
                files = files.map { FormFileDto(
                    fieldName = it.key,
                    fileName = it.value.originalFilename ?: "",
                    fileType = it.value.contentType ?: "",
                    fileSize = it.value.size,
                    fileData = "", // Will be processed by the service
                    mimeType = it.value.contentType ?: ""
                ) },
                accessToken = accessToken
            )
            val result = customFormService.submitFormWithAccess(submissionRequest)
            ResponseEntity.ok(mapOf("success" to "true", "message" to "Form submitted successfully"))
        } catch (e: Exception) {
            logger.error("Error submitting form: {}", e.message, e)
            ResponseEntity.badRequest().body(mapOf("success" to "false", "message" to (e.message ?: "Submission failed")))
        }
    }
}
