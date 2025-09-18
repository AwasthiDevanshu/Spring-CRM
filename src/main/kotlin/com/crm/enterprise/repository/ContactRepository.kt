package com.crm.enterprise.repository

import com.crm.enterprise.entity.Contact
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ContactRepository : CrudRepository<Contact, Long> {
    
    @Query("SELECT * FROM contacts WHERE company_id = :companyId")
    fun findByCompanyId(companyId: Long): List<Contact>
    
    @Query("SELECT * FROM contacts WHERE company_id = :companyId AND is_active = true")
    fun findActiveByCompanyId(companyId: Long): List<Contact>
    
    @Query("SELECT * FROM contacts WHERE company_id = :companyId AND assigned_user_id = :assignedUserId")
    fun findByCompanyIdAndAssignedUserId(companyId: Long, assignedUserId: Long): List<Contact>
    
    @Query("SELECT * FROM contacts WHERE company_id = :companyId AND email = :email")
    fun findByCompanyIdAndEmail(companyId: Long, email: String): Contact?
    
    @Query("SELECT * FROM contacts WHERE company_id = :companyId AND lead_id = :leadId")
    fun findByCompanyIdAndLeadId(companyId: Long, leadId: Long): Contact?
    
    @Query("SELECT * FROM contacts WHERE company_id = :companyId AND (first_name LIKE :searchPattern OR last_name LIKE :searchPattern OR email LIKE :searchPattern)")
    fun findByCompanyIdAndSearchPattern(companyId: Long, searchPattern: String): List<Contact>
}

