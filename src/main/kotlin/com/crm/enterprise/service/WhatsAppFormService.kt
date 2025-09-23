package com.crm.enterprise.service

import com.crm.enterprise.entity.*
import com.crm.enterprise.repository.LeadRepository
import com.crm.enterprise.repository.ContactRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class WhatsAppFormService(
    private val leadRepository: LeadRepository,
    private val contactRepository: ContactRepository
) {
    private val logger = LoggerFactory.getLogger(WhatsAppFormService::class.java)

    /**
     * Generate WhatsApp message for form sharing
     */
    fun generateFormShareMessage(
        formName: String,
        formDescription: String?,
        accessToken: String,
        leadId: Long?,
        contactId: Long?,
        baseUrl: String
    ): String {
        val entityName = getEntityName(leadId, contactId)
        val formUrl = "$baseUrl/forms/access/$accessToken"
        
        val message = buildString {
            appendLine("👋 Hi $entityName!")
            appendLine()
            appendLine("📋 *$formName*")
            if (formDescription != null) {
                appendLine(formDescription)
                appendLine()
            }
            appendLine("Please fill out this form at your convenience:")
            appendLine("🔗 $formUrl")
            appendLine()
            appendLine("This form will help us better understand your needs and provide you with the best possible service.")
            appendLine()
            appendLine("If you have any questions, feel free to reach out!")
            appendLine()
            appendLine("Best regards,")
            appendLine("Your Sales Team")
        }
        
        return message
    }

    /**
     * Generate WhatsApp message for form follow-up
     */
    fun generateFormFollowUpMessage(
        formName: String,
        accessToken: String,
        leadId: Long?,
        contactId: Long?,
        baseUrl: String
    ): String {
        val entityName = getEntityName(leadId, contactId)
        val formUrl = "$baseUrl/forms/access/$accessToken"
        
        val message = buildString {
            appendLine("👋 Hi $entityName!")
            appendLine()
            appendLine("I wanted to follow up on the *$formName* I shared with you earlier.")
            appendLine()
            appendLine("🔗 Form link: $formUrl")
            appendLine()
            appendLine("This form will only take a few minutes to complete and will help us:")
            appendLine("• Better understand your requirements")
            appendLine("• Provide you with a customized solution")
            appendLine("• Save time in our next conversation")
            appendLine()
            appendLine("If you have any questions or need assistance, please don't hesitate to contact me!")
            appendLine()
            appendLine("Looking forward to hearing from you!")
        }
        
        return message
    }

    /**
     * Generate WhatsApp message for form submission confirmation
     */
    fun generateFormSubmissionConfirmationMessage(
        formName: String,
        leadId: Long?,
        contactId: Long?
    ): String {
        val entityName = getEntityName(leadId, contactId)
        
        val message = buildString {
            appendLine("✅ Thank you, $entityName!")
            appendLine()
            appendLine("We've received your submission for *$formName*.")
            appendLine()
            appendLine("Our team will review your information and get back to you within 24 hours.")
            appendLine()
            appendLine("If you have any urgent questions, feel free to contact us directly.")
            appendLine()
            appendLine("We appreciate your time and look forward to working with you!")
        }
        
        return message
    }

    /**
     * Generate WhatsApp message for form expiry reminder
     */
    fun generateFormExpiryReminderMessage(
        formName: String,
        accessToken: String,
        leadId: Long?,
        contactId: Long?,
        baseUrl: String,
        hoursRemaining: Int
    ): String {
        val entityName = getEntityName(leadId, contactId)
        val formUrl = "$baseUrl/forms/access/$accessToken"
        
        val message = buildString {
            appendLine("⏰ Reminder: $entityName")
            appendLine()
            appendLine("The *$formName* will expire in $hoursRemaining hours.")
            appendLine()
            appendLine("🔗 Complete it now: $formUrl")
            appendLine()
            appendLine("Don't miss out on this opportunity to share your requirements with us!")
            appendLine()
            appendLine("If you need more time, just let me know and I can extend the deadline.")
        }
        
        return message
    }

    /**
     * Generate WhatsApp message for form resubmission
     */
    fun generateFormResubmissionMessage(
        formName: String,
        accessToken: String,
        leadId: Long?,
        contactId: Long?,
        baseUrl: String
    ): String {
        val entityName = getEntityName(leadId, contactId)
        val formUrl = "$baseUrl/forms/access/$accessToken"
        
        val message = buildString {
            appendLine("🔄 New Form: $entityName")
            appendLine()
            appendLine("I've created a new *$formName* for you with updated information.")
            appendLine()
            appendLine("🔗 New form link: $formUrl")
            appendLine()
            appendLine("This form has been customized based on our previous conversation and will help us provide you with an even better solution.")
            appendLine()
            appendLine("Please take a few minutes to complete it when convenient.")
            appendLine()
            appendLine("Thank you for your continued interest!")
        }
        
        return message
    }

    /**
     * Get entity name for personalization
     */
    private fun getEntityName(leadId: Long?, contactId: Long?): String {
        return when {
            leadId != null -> {
                try {
                    val lead = leadRepository.findById(leadId).orElse(null)
                    lead?.let { "${it.firstName} ${it.lastName}".trim() } ?: "there"
                } catch (e: Exception) {
                    "there"
                }
            }
            contactId != null -> {
                try {
                    val contact = contactRepository.findById(contactId).orElse(null)
                    contact?.let { "${it.firstName} ${it.lastName}".trim() } ?: "there"
                } catch (e: Exception) {
                    "there"
                }
            }
            else -> "there"
        }
    }

    /**
     * Format phone number for WhatsApp
     */
    fun formatPhoneForWhatsApp(phone: String): String {
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")
        return when {
            digitsOnly.length == 10 -> "1$digitsOnly" // US number
            digitsOnly.length == 11 && digitsOnly.startsWith("1") -> digitsOnly
            digitsOnly.length > 10 -> digitsOnly
            else -> phone
        }
    }

    /**
     * Generate WhatsApp URL
     */
    fun generateWhatsAppUrl(phone: String, message: String): String {
        val formattedPhone = formatPhoneForWhatsApp(phone)
        val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")
        return "https://wa.me/$formattedPhone?text=$encodedMessage"
    }

    /**
     * Generate WhatsApp URL for form sharing
     */
    fun generateFormShareWhatsAppUrl(
        phone: String,
        formName: String,
        formDescription: String?,
        accessToken: String,
        leadId: Long?,
        contactId: Long?,
        baseUrl: String
    ): String {
        val message = generateFormShareMessage(
            formName, formDescription, accessToken, leadId, contactId, baseUrl
        )
        return generateWhatsAppUrl(phone, message)
    }

    /**
     * Generate WhatsApp URL for form follow-up
     */
    fun generateFormFollowUpWhatsAppUrl(
        phone: String,
        formName: String,
        accessToken: String,
        leadId: Long?,
        contactId: Long?,
        baseUrl: String
    ): String {
        val message = generateFormFollowUpMessage(
            formName, accessToken, leadId, contactId, baseUrl
        )
        return generateWhatsAppUrl(phone, message)
    }
}
