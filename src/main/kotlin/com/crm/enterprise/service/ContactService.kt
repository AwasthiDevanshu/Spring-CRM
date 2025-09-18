package com.crm.enterprise.service

import com.crm.enterprise.dto.ContactRequest
import com.crm.enterprise.dto.ContactResponse
import com.crm.enterprise.dto.ContactUpdateRequest
import com.crm.enterprise.entity.Contact
import com.crm.enterprise.repository.ContactRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ContactService(
    private val contactRepository: ContactRepository
) {
    
    fun createContact(contactRequest: ContactRequest, companyId: Long): ContactResponse {
        val contact = Contact(
            firstName = contactRequest.firstName,
            lastName = contactRequest.lastName,
            email = contactRequest.email,
            phone = contactRequest.phone,
            jobTitle = contactRequest.jobTitle,
            department = contactRequest.department,
            companyId = companyId,
            leadId = contactRequest.leadId,
            isActive = true,
            notes = contactRequest.notes,
            assignedUserId = contactRequest.assignedUserId
        )
        
        val savedContact = contactRepository.save(contact)
        return toContactResponse(savedContact)
    }
    
    fun getContactById(id: Long, companyId: Long): ContactResponse? {
        val contact = contactRepository.findById(id).orElse(null)
        return if (contact != null && contact.companyId == companyId) {
            toContactResponse(contact)
        } else null
    }
    
    fun getContactsByCompany(companyId: Long): List<ContactResponse> {
        return contactRepository.findByCompanyId(companyId)
            .map { toContactResponse(it) }
    }
    
    fun getContactsByAssignedUser(companyId: Long, assignedUserId: Long): List<ContactResponse> {
        return contactRepository.findByCompanyIdAndAssignedUserId(companyId, assignedUserId)
            .map { toContactResponse(it) }
    }
    
    fun searchContacts(companyId: Long, searchTerm: String): List<ContactResponse> {
        val searchPattern = "%$searchTerm%"
        return contactRepository.findByCompanyIdAndSearchPattern(companyId, searchPattern)
            .map { toContactResponse(it) }
    }
    
    fun updateContact(id: Long, updateRequest: ContactUpdateRequest, companyId: Long): ContactResponse? {
        val existingContact = contactRepository.findById(id).orElse(null)
        if (existingContact == null || existingContact.companyId != companyId) {
            return null
        }
        
        val updatedContact = existingContact.copy(
            firstName = updateRequest.firstName ?: existingContact.firstName,
            lastName = updateRequest.lastName ?: existingContact.lastName,
            email = updateRequest.email ?: existingContact.email,
            phone = updateRequest.phone ?: existingContact.phone,
            jobTitle = updateRequest.jobTitle ?: existingContact.jobTitle,
            department = updateRequest.department ?: existingContact.department,
            notes = updateRequest.notes ?: existingContact.notes,
            assignedUserId = updateRequest.assignedUserId ?: existingContact.assignedUserId,
            updatedAt = LocalDateTime.now()
        )
        
        val savedContact = contactRepository.save(updatedContact)
        return toContactResponse(savedContact)
    }
    
    fun deleteContact(id: Long, companyId: Long): Boolean {
        val contact = contactRepository.findById(id).orElse(null)
        return if (contact != null && contact.companyId == companyId) {
            contactRepository.deleteById(id)
            true
        } else false
    }
    
    private fun toContactResponse(contact: Contact): ContactResponse {
        return ContactResponse(
            id = contact.id ?: 0L,
            firstName = contact.firstName,
            lastName = contact.lastName,
            fullName = contact.fullName,
            email = contact.email,
            phone = contact.phone,
            jobTitle = contact.jobTitle,
            department = contact.department,
            companyId = contact.companyId,
            leadId = contact.leadId,
            isActive = contact.isActive,
            notes = contact.notes,
            assignedUserId = contact.assignedUserId,
            createdAt = contact.createdAt,
            updatedAt = contact.updatedAt
        )
    }
}
