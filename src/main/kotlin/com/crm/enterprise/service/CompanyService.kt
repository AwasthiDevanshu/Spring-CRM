package com.crm.enterprise.service

import com.crm.enterprise.dto.CompanyRequest
import com.crm.enterprise.dto.CompanyResponse
import com.crm.enterprise.dto.CompanyUpdateRequest
import com.crm.enterprise.dto.CompanyStatsResponse
import com.crm.enterprise.entity.Company
import com.crm.enterprise.repository.CompanyRepository
import com.crm.enterprise.repository.UserRepository
import com.crm.enterprise.repository.LeadRepository
import com.crm.enterprise.repository.ContactRepository
import com.crm.enterprise.repository.DealRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val userRepository: UserRepository,
    private val leadRepository: LeadRepository,
    private val contactRepository: ContactRepository,
    private val dealRepository: DealRepository
) {
    
    fun createCompany(companyRequest: CompanyRequest): CompanyResponse {
        val company = Company(
            name = companyRequest.name,
            description = companyRequest.description,
            website = companyRequest.website,
            phone = companyRequest.phone,
            email = companyRequest.email,
            address = companyRequest.address,
            city = companyRequest.city,
            state = companyRequest.state,
            country = companyRequest.country,
            postalCode = companyRequest.postalCode,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val savedCompany = companyRepository.save(company)
        return toCompanyResponse(savedCompany)
    }
    
    fun getCompanyById(id: Long): CompanyResponse? {
        val company = companyRepository.findById(id).orElse(null)
        return if (company != null) {
            toCompanyResponse(company)
        } else null
    }
    
    fun getAllCompanies(): List<CompanyResponse> {
        return companyRepository.findAll()
            .map { toCompanyResponse(it) }
    }
    
    fun getActiveCompanies(): List<CompanyResponse> {
        return companyRepository.findActiveCompanies()
            .map { toCompanyResponse(it) }
    }
    
    fun searchCompanies(searchTerm: String): List<CompanyResponse> {
        return companyRepository.findByNameContaining("%$searchTerm%")
            .map { toCompanyResponse(it) }
    }
    
    fun updateCompany(id: Long, updateRequest: CompanyUpdateRequest): CompanyResponse? {
        val existingCompany = companyRepository.findById(id).orElse(null)
        if (existingCompany == null) {
            return null
        }
        
        val updatedCompany = existingCompany.copy(
            name = updateRequest.name ?: existingCompany.name,
            description = updateRequest.description ?: existingCompany.description,
            website = updateRequest.website ?: existingCompany.website,
            phone = updateRequest.phone ?: existingCompany.phone,
            email = updateRequest.email ?: existingCompany.email,
            address = updateRequest.address ?: existingCompany.address,
            city = updateRequest.city ?: existingCompany.city,
            state = updateRequest.state ?: existingCompany.state,
            country = updateRequest.country ?: existingCompany.country,
            postalCode = updateRequest.postalCode ?: existingCompany.postalCode,
            updatedAt = LocalDateTime.now()
        )
        
        val savedCompany = companyRepository.save(updatedCompany)
        return toCompanyResponse(savedCompany)
    }
    
    fun deleteCompany(id: Long): Boolean {
        return try {
            companyRepository.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getCompanyStats(companyId: Long): CompanyStatsResponse? {
        val company = companyRepository.findById(companyId).orElse(null) ?: return null
        
        val totalUsers = userRepository.findByCompanyId(companyId).size
        val totalLeads = leadRepository.findByCompanyId(companyId).size
        val totalContacts = contactRepository.findByCompanyId(companyId).size
        val totalDeals = dealRepository.findByCompanyId(companyId).size
        
        // Calculate total revenue from won deals
        val totalRevenue = dealRepository.findByCompanyId(companyId)
            .filter { it.status.name == "WON" }
            .sumOf { it.value.toDouble() }
        
        return CompanyStatsResponse(
            id = company.id ?: 0L,
            name = company.name,
            totalUsers = totalUsers,
            totalLeads = totalLeads,
            totalContacts = totalContacts,
            totalDeals = totalDeals,
            totalRevenue = totalRevenue,
            isActive = company.isActive,
            createdAt = company.createdAt
        )
    }
    
    private fun toCompanyResponse(company: Company): CompanyResponse {
        return CompanyResponse(
            id = company.id ?: 0L,
            name = company.name,
            description = company.description,
            website = company.website,
            phone = company.phone,
            email = company.email,
            address = company.address,
            city = company.city,
            state = company.state,
            country = company.country,
            postalCode = company.postalCode,
            isActive = company.isActive,
            createdAt = company.createdAt,
            updatedAt = company.updatedAt
        )
    }
}
