package com.crm.enterprise.service

import com.crm.enterprise.entity.*
import com.crm.enterprise.repository.ActivityRepository
import com.crm.enterprise.repository.LeadRepository
import com.crm.enterprise.repository.ContactRepository
import com.crm.enterprise.repository.CustomFormAccessRepository
import com.crm.enterprise.repository.CustomFormRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class FormActivityService(
    private val customFormRepository: CustomFormRepository,
    private val activityRepository: ActivityRepository,
    private val leadRepository: LeadRepository,
    private val contactRepository: ContactRepository,
    private val customFormAccessRepository: CustomFormAccessRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(FormActivityService::class.java)

    /**
     * Create activity when form is shared
     */
    fun createFormSharedActivity(
        formId: Long,
        formName: String,
        leadId: Long?,
        contactId: Long?,
        assignedTo: Long,
        assignedBy: Long,
        companyId: Long,
        accessToken: String
    ): Activity {
        val entityType = when {
            leadId != null -> "LEAD"
            contactId != null -> "CONTACT"
            else -> "FORM"
        }
        
        val entityId = leadId ?: contactId ?: formId
        
        val activity = Activity(
            type = ActivityType.FORM_SHARED,
            subject = "Form Shared: $formName",
            description = "Form '$formName' has been shared with ${entityType.lowercase()}. Access token: ${accessToken.take(8)}...",
            status = ActivityStatus.COMPLETED,
            priority = ActivityPriority.MEDIUM,
            assignedTo = assignedTo,
            assignedBy = assignedBy,
            entityType = entityType,
            entityId = entityId,
            companyId = companyId,
            activityDate = LocalDateTime.now(),
            completedAt = LocalDateTime.now()
        )
        
        val savedActivity = activityRepository.save(activity)
        logger.info("Created FORM_SHARED activity for form $formId, $entityType $entityId")
        
        return savedActivity
    }

    /**
     * Create activity when form is submitted
     */
    fun createFormSubmittedActivity(
        formId: Long,
        formName: String,
        leadId: Long?,
        contactId: Long?,
        assignedTo: Long,
        assignedBy: Long,
        companyId: Long,
        submissionData: Map<String, Any>
    ): Activity {
        val entityType = when {
            leadId != null -> "LEAD"
            contactId != null -> "CONTACT"
            else -> "FORM"
        }
        
        val entityId = leadId ?: contactId ?: formId
        
        // Extract key information from submission data
        val keyData = extractKeySubmissionData(submissionData)
        
        val activity = Activity(
            type = ActivityType.FORM_SUBMITTED,
            subject = "Form Submitted: $formName",
            description = "Form '$formName' has been submitted by ${entityType.lowercase()}. Key data: $keyData",
            status = ActivityStatus.PENDING,
            priority = ActivityPriority.HIGH,
            assignedTo = assignedTo,
            assignedBy = assignedBy,
            entityType = entityType,
            entityId = entityId,
            companyId = companyId,
            activityDate = LocalDateTime.now(),
            dueDate = LocalDateTime.now().plusHours(24) // Follow up within 24 hours
        )
        
        val savedActivity = activityRepository.save(activity)
        logger.info("Created FORM_SUBMITTED activity for form $formId, $entityType $entityId")
        
        return savedActivity
    }

    /**
     * Create follow-up activity for unsubmitted forms
     */
    fun createFormFollowUpActivity(
        formId: Long,
        formName: String,
        leadId: Long?,
        contactId: Long?,
        assignedTo: Long,
        assignedBy: Long,
        companyId: Long,
        accessToken: String
    ): Activity {
        val entityType = when {
            leadId != null -> "LEAD"
            contactId != null -> "CONTACT"
            else -> "FORM"
        }
        
        val entityId = leadId ?: contactId ?: formId
        
        val activity = Activity(
            type = ActivityType.FORM_FOLLOW_UP,
            subject = "Form Follow-up Required: $formName",
            description = "Form '$formName' was shared with ${entityType.lowercase()} but not submitted. Follow up required. Access token: ${accessToken.take(8)}...",
            status = ActivityStatus.PENDING,
            priority = ActivityPriority.HIGH,
            assignedTo = assignedTo,
            assignedBy = assignedBy,
            entityType = entityType,
            entityId = entityId,
            companyId = companyId,
            activityDate = LocalDateTime.now(),
            dueDate = LocalDateTime.now().plusHours(72) // Follow up within 72 hours
        )
        
        val savedActivity = activityRepository.save(activity)
        logger.info("Created FORM_FOLLOW_UP activity for form $formId, $entityType $entityId")
        
        return savedActivity
    }

    /**
     * Update lead data from form submission
     */
    fun updateLeadFromFormSubmission(
        leadId: Long,
        submissionData: Map<String, Any>
    ): Boolean {
        return try {
            val lead = leadRepository.findById(leadId)
                .orElseThrow { RuntimeException("Lead not found") }
            
            val updatedLead = lead.copy(
                // Update basic fields if provided in form
                firstName = submissionData["firstName"]?.toString() ?: lead.firstName,
                lastName = submissionData["lastName"]?.toString() ?: lead.lastName,
                email = submissionData["email"]?.toString() ?: lead.email,
                phone = submissionData["phone"]?.toString() ?: lead.phone,
                company = submissionData["company"]?.toString() ?: lead.company,
                jobTitle = submissionData["jobTitle"]?.toString() ?: lead.jobTitle,
                updatedAt = LocalDateTime.now()
            )
            
            leadRepository.save(updatedLead)
            logger.info("Updated lead $leadId with form submission data")
            true
        } catch (e: Exception) {
            logger.error("Error updating lead $leadId from form submission", e)
            false
        }
    }

    /**
     * Update contact data from form submission
     */
    fun updateContactFromFormSubmission(
        contactId: Long,
        submissionData: Map<String, Any>
    ): Boolean {
        return try {
            val contact = contactRepository.findById(contactId)
                .orElseThrow { RuntimeException("Contact not found") }
            
            val updatedContact = contact.copy(
                // Update basic fields if provided in form
                firstName = submissionData["firstName"]?.toString() ?: contact.firstName,
                lastName = submissionData["lastName"]?.toString() ?: contact.lastName,
                email = submissionData["email"]?.toString() ?: contact.email,
                phone = submissionData["phone"]?.toString() ?: contact.phone,
                jobTitle = submissionData["jobTitle"]?.toString() ?: contact.jobTitle,
                department = submissionData["department"]?.toString() ?: contact.department,
                updatedAt = LocalDateTime.now()
            )
            
            contactRepository.save(updatedContact)
            logger.info("Updated contact $contactId with form submission data")
            true
        } catch (e: Exception) {
            logger.error("Error updating contact $contactId from form submission", e)
            false
        }
    }

    /**
     * Scheduled task to create follow-up activities for unsubmitted forms
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    fun createFollowUpActivitiesForUnsubmittedForms() {
        try {
            logger.info("Running scheduled task to create follow-up activities for unsubmitted forms")
            
            val cutoffTime = LocalDateTime.now().minusHours(48) // Forms shared 48+ hours ago
            val unsubmittedAccesses = customFormAccessRepository.findByCreatedAtBeforeAndSubmissionCount(cutoffTime, 0)
            
            for (access in unsubmittedAccesses) {
                // Check if follow-up activity already exists
                val existingFollowUp = activityRepository.findByEntityTypeAndEntityIdAndTypeAndStatus(
                    if (access.leadId != null) "LEAD" else "CONTACT",
                    access.leadId ?: access.contactId ?: 0L,
                    "FORM_FOLLOW_UP",
                    ActivityStatus.PENDING
                )
                
                if (existingFollowUp.isEmpty()) {
                    // Get form name (you might need to fetch this from form repository)
                    val formName = customFormRepository.findById(access.formId).getOrNull()

                    createFormFollowUpActivity(
                        formId = access.formId,
                        formName = formName?.name ?: "Custom form",
                        leadId = access.leadId,
                        contactId = access.contactId,
                        assignedTo = access.createdBy,
                        assignedBy = access.createdBy,
                        companyId = access.companyId,
                        accessToken = access.accessToken
                    )
                }
            }
            
            logger.info("Created follow-up activities for ${unsubmittedAccesses.size} unsubmitted forms")
        } catch (e: Exception) {
            logger.error("Error in scheduled task for form follow-up activities", e)
        }
    }

    private fun extractKeySubmissionData(submissionData: Map<String, Any>): String {
        val keyFields = listOf("firstName", "lastName", "email", "phone", "company", "jobTitle", "message", "comments")
        val extractedData = submissionData.filterKeys { it in keyFields }
            .map { "${it.key}: ${it.value}" }
            .take(3) // Show only first 3 fields
        
        return if (extractedData.isNotEmpty()) {
            extractedData.joinToString(", ")
        } else {
            "No key data extracted"
        }
    }
}
