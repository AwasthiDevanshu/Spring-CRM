package com.crm.enterprise.service

import com.crm.enterprise.dto.LeadRequest
import com.crm.enterprise.dto.LeadResponse
import com.crm.enterprise.dto.LeadUpdateRequest
import com.crm.enterprise.entity.Lead
import com.crm.enterprise.entity.LeadStatus
import com.crm.enterprise.repository.LeadRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class LeadService(
    private val leadRepository: LeadRepository,
    private val auditTrailService: AuditTrailService
) {
    
    fun createLead(leadRequest: LeadRequest, companyId: Long, performedBy: Long): LeadResponse {
        val lead = Lead(
            firstName = leadRequest.firstName,
            lastName = leadRequest.lastName,
            email = leadRequest.email,
            phone = leadRequest.phone,
            company = leadRequest.company,
            jobTitle = leadRequest.jobTitle,
            status = leadRequest.status,
            source = leadRequest.source,
            score = leadRequest.score,
            notes = leadRequest.notes,
            assignedUserId = leadRequest.assignedUserId,
            companyId = companyId
        )
        
        val savedLead = leadRepository.save(lead)
        
        // Log audit trail
        val leadName = "${savedLead.firstName} ${savedLead.lastName}".trim()
        auditTrailService.logEntityCreation("LEAD", savedLead.id!!, leadName, performedBy, companyId)
        
        return toLeadResponse(savedLead)
    }
    
    fun getLeadById(id: Long, companyId: Long): LeadResponse? {
        val lead = leadRepository.findByIdAndCompanyIdAndNotDeleted(id, companyId)
        return lead?.let { toLeadResponse(it) }
    }
    
    fun getLeadsByCompany(companyId: Long): List<LeadResponse> {
        return leadRepository.findByCompanyId(companyId)
            .map { toLeadResponse(it) }
    }
    
    fun getLeadsByStatus(companyId: Long, status: com.crm.enterprise.entity.LeadStatus): List<LeadResponse> {
        return leadRepository.findByCompanyIdAndStatus(companyId, status)
            .map { toLeadResponse(it) }
    }
    
    fun getLeadsByAssignedUser(companyId: Long, assignedUserId: Long): List<LeadResponse> {
        return leadRepository.findByCompanyIdAndAssignedUserId(companyId, assignedUserId)
            .map { toLeadResponse(it) }
    }
    
    fun searchLeads(companyId: Long, searchTerm: String): List<LeadResponse> {
        val searchPattern = "%$searchTerm%"
        return leadRepository.findByCompanyIdAndSearchPattern(companyId, searchPattern)
            .map { toLeadResponse(it) }
    }
    
    fun updateLead(id: Long, updateRequest: LeadUpdateRequest, companyId: Long, performedBy: Long): LeadResponse? {
        val existingLead = leadRepository.findByIdAndCompanyIdAndNotDeleted(id, companyId)
        if (existingLead == null) {
            return null
        }
        
        val leadName = "${existingLead.firstName} ${existingLead.lastName}".trim()
        
        // Log field changes for audit trail
        updateRequest.firstName?.let { 
            if (it != existingLead.firstName) {
                auditTrailService.logEntityUpdate("LEAD", id, leadName, "firstName", existingLead.firstName, it, performedBy, companyId)
            }
        }
        updateRequest.lastName?.let { 
            if (it != existingLead.lastName) {
                auditTrailService.logEntityUpdate("LEAD", id, leadName, "lastName", existingLead.lastName, it, performedBy, companyId)
            }
        }
        updateRequest.email?.let { 
            if (it != existingLead.email) {
                auditTrailService.logEntityUpdate("LEAD", id, leadName, "email", existingLead.email, it, performedBy, companyId)
            }
        }
        updateRequest.status?.let { 
            if (it != existingLead.status) {
                auditTrailService.logStatusChange("LEAD", id, leadName, existingLead.status.name, it.name, performedBy, companyId)
            }
        }
        updateRequest.assignedUserId?.let { 
            if (it != existingLead.assignedUserId) {
                auditTrailService.logAssignment("LEAD", id, leadName, "User ID: $it", performedBy, companyId)
            }
        }
        
        val updatedLead = existingLead.copy(
            firstName = updateRequest.firstName ?: existingLead.firstName,
            lastName = updateRequest.lastName ?: existingLead.lastName,
            email = updateRequest.email ?: existingLead.email,
            phone = updateRequest.phone ?: existingLead.phone,
            company = updateRequest.company ?: existingLead.company,
            jobTitle = updateRequest.jobTitle ?: existingLead.jobTitle,
            status = updateRequest.status ?: existingLead.status,
            source = updateRequest.source ?: existingLead.source,
            score = updateRequest.score ?: existingLead.score,
            notes = updateRequest.notes ?: existingLead.notes,
            assignedUserId = updateRequest.assignedUserId ?: existingLead.assignedUserId,
            updatedAt = LocalDateTime.now()
        )
        
        val savedLead = leadRepository.save(updatedLead)
        return toLeadResponse(savedLead)
    }
    
    fun deleteLead(id: Long, companyId: Long, performedBy: Long): Boolean {
        val lead = leadRepository.findByIdAndCompanyIdAndNotDeleted(id, companyId)
        return if (lead != null) {
            val leadName = "${lead.firstName} ${lead.lastName}".trim()
            auditTrailService.logEntityDeletion("LEAD", id, leadName, performedBy, companyId)
            leadRepository.softDeleteByIdAndCompanyId(id, companyId)
            true
        } else false
    }
    
    fun restoreLead(id: Long, companyId: Long, performedBy: Long): Boolean {
        val lead = leadRepository.findDeletedByCompanyId(companyId).find { it.id == id }
        return if (lead != null) {
            val leadName = "${lead.firstName} ${lead.lastName}".trim()
            auditTrailService.logActivity(
                com.crm.enterprise.dto.AuditTrailRequest(
                    entityType = "LEAD",
                    entityId = id,
                    action = "RESTORE",
                    description = "Restored LEAD: $leadName"
                ),
                performedBy,
                companyId
            )
            leadRepository.restoreByIdAndCompanyId(id, companyId)
            true
        } else false
    }
    
    fun getDeletedLeads(companyId: Long): List<LeadResponse> {
        return leadRepository.findDeletedByCompanyId(companyId)
            .map { toLeadResponse(it) }
    }
    
    // Report methods
    fun getLeadsPerformanceReport(companyId: Long, days: Int): com.crm.enterprise.controller.LeadsPerformanceReport {
        val cutoffDate = LocalDateTime.now().minusDays(days.toLong())
        val allLeads = leadRepository.findByCompanyId(companyId)
        val periodLeads = allLeads.filter { it.createdAt.isAfter(cutoffDate) }
        val previousPeriodLeads = allLeads.filter { 
            it.createdAt.isAfter(cutoffDate.minusDays(days.toLong())) && 
            it.createdAt.isBefore(cutoffDate) 
        }
        
        val totalLeads = periodLeads.size
        val qualifiedLeads = periodLeads.count { it.status == LeadStatus.QUALIFIED }
        val conversionRate = if (totalLeads > 0) (qualifiedLeads.toDouble() / totalLeads) * 100 else 0.0
        val averageLeadScore = if (periodLeads.isNotEmpty()) periodLeads.map { it.score }.average() else 0.0
        
        val previousTotalLeads = previousPeriodLeads.size
        val changePercent = if (previousTotalLeads > 0) {
            ((totalLeads - previousTotalLeads).toDouble() / previousTotalLeads) * 100
        } else 0.0
        
        return com.crm.enterprise.controller.LeadsPerformanceReport(
            totalLeads = totalLeads,
            conversionRate = conversionRate,
            qualifiedLeads = qualifiedLeads,
            averageLeadScore = averageLeadScore,
            periodComparison = com.crm.enterprise.controller.PeriodComparison(
                changePercent = changePercent,
                changeType = if (changePercent >= 0) "increase" else "decrease"
            )
        )
    }
    
    fun getNewLeadsThisMonth(companyId: Long): Int {
        val startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        return leadRepository.findByCompanyId(companyId)
            .count { it.createdAt.isAfter(startOfMonth) }
    }
    
    fun getLeadSourcesDistribution(companyId: Long): com.crm.enterprise.controller.LeadSourcesDistribution {
        val leads = leadRepository.findByCompanyId(companyId)
        val total = leads.size.toDouble()
        
        if (total == 0.0) {
            return com.crm.enterprise.controller.LeadSourcesDistribution(0.0, 0.0, 0.0, 0.0)
        }
        
        val website = leads.count { it.source == com.crm.enterprise.entity.LeadSource.WEBSITE } / total * 100
        val referrals = leads.count { it.source == com.crm.enterprise.entity.LeadSource.REFERRAL } / total * 100
        val socialMedia = leads.count { it.source == com.crm.enterprise.entity.LeadSource.SOCIAL_MEDIA } / total * 100
        val other = leads.count { it.source == com.crm.enterprise.entity.LeadSource.OTHER } / total * 100
        
        return com.crm.enterprise.controller.LeadSourcesDistribution(
            website = website,
            referrals = referrals,
            socialMedia = socialMedia,
            other = other
        )
    }

    private fun toLeadResponse(lead: Lead): LeadResponse {
        return LeadResponse(
            id = lead.id ?: 0L,
            firstName = lead.firstName,
            lastName = lead.lastName,
            fullName = lead.fullName,
            email = lead.email,
            phone = lead.phone,
            company = lead.company,
            jobTitle = lead.jobTitle,
            status = lead.status,
            source = lead.source,
            score = lead.score,
            notes = lead.notes,
            assignedUserId = lead.assignedUserId,
            companyId = lead.companyId,
            createdAt = lead.createdAt,
            updatedAt = lead.updatedAt
        )
    }
}

