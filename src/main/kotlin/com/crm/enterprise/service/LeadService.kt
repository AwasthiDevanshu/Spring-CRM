package com.crm.enterprise.service

import com.crm.enterprise.dto.LeadRequest
import com.crm.enterprise.dto.LeadResponse
import com.crm.enterprise.dto.LeadUpdateRequest
import com.crm.enterprise.entity.Lead
import com.crm.enterprise.repository.LeadRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LeadService(
    private val leadRepository: LeadRepository
) {
    
    fun createLead(leadRequest: LeadRequest, companyId: Long): LeadResponse {
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
        return toLeadResponse(savedLead)
    }
    
    fun getLeadById(id: Long, companyId: Long): LeadResponse? {
        val lead = leadRepository.findById(id).orElse(null)
        return if (lead != null && lead.companyId == companyId) {
            toLeadResponse(lead)
        } else null
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
    
    fun updateLead(id: Long, updateRequest: LeadUpdateRequest, companyId: Long): LeadResponse? {
        val existingLead = leadRepository.findById(id).orElse(null)
        if (existingLead == null || existingLead.companyId != companyId) {
            return null
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
    
    fun deleteLead(id: Long, companyId: Long): Boolean {
        val lead = leadRepository.findById(id).orElse(null)
        return if (lead != null && lead.companyId == companyId) {
            leadRepository.deleteById(id)
            true
        } else false
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

