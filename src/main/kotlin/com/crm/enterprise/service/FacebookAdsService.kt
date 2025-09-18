// Temporarily disabled due to Facebook SDK compilation issues
/*
package com.crm.enterprise.service

import com.crm.enterprise.dto.FacebookAdsIntegrationRequest
import com.crm.enterprise.dto.FacebookAdsIntegrationResponse
import com.crm.enterprise.dto.FacebookAdsSyncResponse
import com.crm.enterprise.entity.FacebookAdsIntegration
import com.crm.enterprise.entity.FacebookAdsLead
import com.crm.enterprise.entity.Lead as CrmLead
import com.crm.enterprise.entity.LeadSource
import com.crm.enterprise.entity.LeadStatus
import com.crm.enterprise.repository.FacebookAdsIntegrationRepository
import com.crm.enterprise.repository.FacebookAdsLeadRepository
import com.crm.enterprise.repository.LeadRepository
import com.facebook.ads.sdk.APIContext
import com.facebook.ads.sdk.APIException
import com.facebook.ads.sdk.Lead as FacebookLead
import com.facebook.ads.sdk.LeadgenForm
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class FacebookAdsService(
    private val facebookAdsIntegrationRepository: FacebookAdsIntegrationRepository,
    private val facebookAdsLeadRepository: FacebookAdsLeadRepository,
    private val leadRepository: LeadRepository,
    private val objectMapper: ObjectMapper
) {

    fun createIntegration(request: FacebookAdsIntegrationRequest, companyId: Long): FacebookAdsIntegrationResponse {
        // Validate Facebook credentials by making a test API call
        try {
            val context = APIContext(request.accessToken)
            val adAccount = com.facebook.ads.sdk.AdAccount(request.adAccountId, context)
            adAccount.get().execute() // Test API call
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Facebook credentials: ${e.message}")
        }

        val integration = FacebookAdsIntegration(
            companyId = companyId,
            accessToken = request.accessToken,
            adAccountId = request.adAccountId,
            appId = request.appId,
            appSecret = request.appSecret,
            syncFrequency = request.syncFrequency,
            autoCreateLeads = request.autoCreateLeads,
            leadSource = request.leadSource
        )

        val savedIntegration = facebookAdsIntegrationRepository.save(integration)
        return toIntegrationResponse(savedIntegration)
    }

    fun getIntegrations(companyId: Long): List<FacebookAdsIntegrationResponse> {
        return facebookAdsIntegrationRepository.findByCompanyId(companyId)
            .map { toIntegrationResponse(it) }
    }

    fun getIntegration(id: Long, companyId: Long): FacebookAdsIntegrationResponse? {
        return facebookAdsIntegrationRepository.findById(id)
            .filter { it.companyId == companyId }
            .map { toIntegrationResponse(it) }
            .orElse(null)
    }

    fun updateIntegration(id: Long, request: FacebookAdsIntegrationRequest, companyId: Long): FacebookAdsIntegrationResponse? {
        return facebookAdsIntegrationRepository.findById(id)
            .filter { it.companyId == companyId }
            .map { existingIntegration ->
                val updatedIntegration = existingIntegration.copy(
                    accessToken = request.accessToken,
                    adAccountId = request.adAccountId,
                    appId = request.appId,
                    appSecret = request.appSecret,
                    syncFrequency = request.syncFrequency,
                    autoCreateLeads = request.autoCreateLeads,
                    leadSource = request.leadSource,
                    updatedAt = LocalDateTime.now()
                )
                toIntegrationResponse(facebookAdsIntegrationRepository.save(updatedIntegration))
            }.orElse(null)
    }

    fun deleteIntegration(id: Long, companyId: Long): Boolean {
        return facebookAdsIntegrationRepository.findById(id)
            .filter { it.companyId == companyId }
            .map {
                facebookAdsIntegrationRepository.delete(it)
                true
            }.orElse(false)
    }

    @Transactional
    fun syncLeads(integrationId: Long, companyId: Long): FacebookAdsSyncResponse {
        val integration = facebookAdsIntegrationRepository.findById(integrationId)
            .filter { it.companyId == companyId && it.isActive }
            .orElseThrow { IllegalArgumentException("Integration not found or inactive") }

        val context = APIContext(integration.accessToken)
        val adAccount = com.facebook.ads.sdk.AdAccount(integration.adAccountId, context)

        var leadsSynced = 0
        var leadsCreated = 0
        var errors = 0
        val errorMessages = mutableListOf<String>()

        try {
            // Get leadgen forms for the ad account
            val forms = adAccount.getLeadgenForms().requestAllFields().execute()
            
            for (form in forms) {
                try {
                    // Get leads for each form
                    val leads = form.getLeads().requestAllFields().execute()
                    
                    for (lead in leads) {
                        try {
                            // Check if lead already exists
                            val existingLead = facebookAdsLeadRepository.findByFacebookLeadId(lead.id)
                            if (existingLead != null) {
                                continue
                            }

                            // Save Facebook lead data
                            val leadData = objectMapper.writeValueAsString(lead)
                            val facebookLead = FacebookAdsLead(
                                companyId = companyId,
                                facebookLeadId = lead.id,
                                adId = lead.adId,
                                adSetId = lead.adsetId,
                                campaignId = lead.campaignId,
                                formId = lead.formId,
                                leadData = leadData
                            )
                            facebookAdsLeadRepository.save(facebookLead)
                            leadsSynced++

                            // Auto-create CRM lead if enabled
                            if (integration.autoCreateLeads) {
                                try {
                                    val crmLead = createCrmLeadFromFacebookLead(lead, companyId, integration.leadSource)
                                    leadsCreated++
                                } catch (e: Exception) {
                                    errors++
                                    errorMessages.add("Failed to create CRM lead for Facebook lead ${lead.id}: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            errors++
                            errorMessages.add("Failed to process Facebook lead ${lead.id}: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    errors++
                    errorMessages.add("Failed to get leads for form ${form.id}: ${e.message}")
                }
            }

            // Update last sync time
            val updatedIntegration = integration.copy(lastSyncAt = LocalDateTime.now())
            facebookAdsIntegrationRepository.save(updatedIntegration)

        } catch (e: Exception) {
            errors++
            errorMessages.add("Failed to sync leads: ${e.message}")
        }

        return FacebookAdsSyncResponse(
            success = errors == 0,
            leadsSynced = leadsSynced,
            leadsCreated = leadsCreated,
            errors = errors,
            syncTime = LocalDateTime.now(),
            errorMessages = errorMessages
        )
    }

    private fun createCrmLeadFromFacebookLead(facebookLead: FacebookLead, companyId: Long, leadSource: String): CrmLead {
        val fieldData = facebookLead.fieldData
        val fieldMap = fieldData.associate { it.name to it.values.firstOrNull() ?: "" }

        val firstName = fieldMap["first_name"] ?: fieldMap["full_name"]?.split(" ")?.firstOrNull() ?: "Unknown"
        val lastName = fieldMap["last_name"] ?: fieldMap["full_name"]?.split(" ")?.drop(1)?.joinToString(" ") ?: ""
        val email = fieldMap["email"] ?: ""
        val phone = fieldMap["phone_number"] ?: fieldMap["phone"] ?: ""

        val crmLead = CrmLead(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone.takeIf { it.isNotEmpty() },
            company = fieldMap["company_name"] ?: fieldMap["company"] ?: "",
            jobTitle = fieldMap["job_title"] ?: fieldMap["job"] ?: "",
            status = LeadStatus.NEW,
            source = LeadSource.valueOf(leadSource),
            score = 50, // Default score for Facebook leads
            notes = "Imported from Facebook Ads\nForm ID: ${facebookLead.formId}\nAd ID: ${facebookLead.adId}",
            companyId = companyId,
            createdById = 1L // Default to admin user
        )

        return leadRepository.save(crmLead)
    }

    fun getLeads(companyId: Long): List<FacebookAdsLead> {
        return facebookAdsLeadRepository.findByCompanyId(companyId)
    }

    fun processNewLeads(companyId: Long): FacebookAdsSyncResponse {
        val newLeads = facebookAdsLeadRepository.findAllNewLeads()
        var leadsCreated = 0
        var errors = 0
        val errorMessages = mutableListOf<String>()

        for (lead in newLeads) {
            try {
                val integration = facebookAdsIntegrationRepository.findByCompanyId(lead.companyId)
                    .firstOrNull { it.isActive && it.autoCreateLeads }
                    ?: continue

                val facebookLead = objectMapper.readValue(lead.leadData, Lead::class.java)
                createCrmLeadFromFacebookLead(facebookLead, lead.companyId, integration.leadSource)

                // Update lead status
                val updatedLead = lead.copy(
                    status = "PROCESSED",
                    processedAt = LocalDateTime.now()
                )
                facebookAdsLeadRepository.save(updatedLead)
                leadsCreated++

            } catch (e: Exception) {
                errors++
                errorMessages.add("Failed to process lead ${lead.facebookLeadId}: ${e.message}")
                
                val updatedLead = lead.copy(
                    status = "ERROR",
                    errorMessage = e.message,
                    processedAt = LocalDateTime.now()
                )
                facebookAdsLeadRepository.save(updatedLead)
            }
        }

        return FacebookAdsSyncResponse(
            success = errors == 0,
            leadsSynced = newLeads.size,
            leadsCreated = leadsCreated,
            errors = errors,
            syncTime = LocalDateTime.now(),
            errorMessages = errorMessages
        )
    }

    private fun toIntegrationResponse(integration: FacebookAdsIntegration): FacebookAdsIntegrationResponse {
        return FacebookAdsIntegrationResponse(
            id = integration.id ?: 0L,
            companyId = integration.companyId,
            adAccountId = integration.adAccountId,
            appId = integration.appId,
            isActive = integration.isActive,
            lastSyncAt = integration.lastSyncAt,
            syncFrequency = integration.syncFrequency,
            autoCreateLeads = integration.autoCreateLeads,
            leadSource = integration.leadSource,
            createdAt = integration.createdAt,
            updatedAt = integration.updatedAt
        )
    }
}
*/
