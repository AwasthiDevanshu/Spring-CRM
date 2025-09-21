package com.crm.enterprise.service

import com.crm.enterprise.dto.ContactRequest
import com.crm.enterprise.dto.ContactResponse
import com.crm.enterprise.dto.ContactUpdateRequest
import com.crm.enterprise.entity.Contact
import com.crm.enterprise.repository.ContactRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ContactService(
    private val contactRepository: ContactRepository,
    private val databaseHandler: DatabaseHandler,
    private val queryGenerator: QueryGenerator,
    private val schemaManager: DatabaseSchemaManager
) {

    private val logger = LoggerFactory.getLogger(ContactService::class.java)

    init {
        // Validate and fix schema on startup
        try {
            val expectedSchema = schemaManager.getExpectedSchema("contacts")
            val result = schemaManager.validateAndFixTableSchema("contacts", expectedSchema)
            if (result.isFailure) {
                logger.error("Failed to validate contacts table schema: ${result.exceptionOrNull()?.message}")
            } else {
                logger.info("Contacts table schema validated successfully")
            }
        } catch (e: Exception) {
            logger.error("Error validating contacts table schema: ${e.message}", e)
        }
    }
    
    fun createContact(contactRequest: ContactRequest, companyId: Long): ContactResponse {
        return try {
            logger.info("Creating contact: ${contactRequest.firstName} ${contactRequest.lastName} for company $companyId")
            
            // Prepare data for insertion
            val contactData = mapOf(
                "first_name" to contactRequest.firstName,
                "last_name" to contactRequest.lastName,
                "email" to contactRequest.email,
                "phone" to (contactRequest.phone ?: ""),
                "job_title" to (contactRequest.jobTitle ?: ""),
                "department" to (contactRequest.department ?: ""),
                "company_id" to companyId,
                "lead_id" to (contactRequest.leadId ?: 0),
                "is_active" to true,
                "notes" to (contactRequest.notes ?: ""),
                "assigned_user_id" to (contactRequest.assignedUserId ?: 0),
                "created_by_id" to 1L, // Default to user ID 1 for now
                "created_at" to LocalDateTime.now(),
                "updated_at" to LocalDateTime.now()
            )
            
            // Generate and execute insert query
            val insertQuery = queryGenerator.generateInsertQuery("contacts", contactData)
            val insertResult = databaseHandler.insert(insertQuery, contactData, "CREATE_CONTACT")
            
            if (insertResult.isFailure) {
                logger.error("Failed to create contact: ${insertResult.exceptionOrNull()?.message}")
                throw insertResult.exceptionOrNull()!!
            }
            
            val contactId = insertResult.getOrNull()!!
            logger.info("Successfully created contact with ID: $contactId")
            
            // Fetch the created contact
            val selectQuery = queryGenerator.generateSelectQuery(
                "contacts", 
                listOf("*"), 
                mapOf("id" to contactId)
            )
            val fetchResult = databaseHandler.queryForObject(selectQuery, mapOf("id" to contactId), { row ->
                Contact(
                    id = row["id"] as Long,
                    firstName = row["first_name"] as String,
                    lastName = row["last_name"] as String,
                    email = row["email"] as String,
                    phone = row["phone"] as? String,
                    jobTitle = row["job_title"] as? String,
                    department = row["department"] as? String,
                    companyId = row["company_id"] as Long,
                    leadId = row["lead_id"] as? Long,
                    isActive = row["is_active"] as Boolean,
                    notes = row["notes"] as? String,
                    assignedUserId = row["assigned_user_id"] as? Long,
                    createdAt = (row["created_at"] as java.sql.Timestamp).toLocalDateTime(),
                    updatedAt = (row["updated_at"] as java.sql.Timestamp).toLocalDateTime()
                )
            }, "FETCH_CONTACT")
            
            if (fetchResult.isFailure) {
                logger.error("Failed to fetch created contact: ${fetchResult.exceptionOrNull()?.message}")
                throw fetchResult.exceptionOrNull()!!
            }
            
            val contact = fetchResult.getOrNull()!!
            toContactResponse(contact)
        } catch (e: Exception) {
            logger.error("Error creating contact: ${e.message}", e)
            throw e
        }
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
