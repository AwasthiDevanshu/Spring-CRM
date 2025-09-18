package com.crm.enterprise.repository

import com.crm.enterprise.entity.Lead
import com.crm.enterprise.entity.LeadStatus
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LeadRepository : CrudRepository<Lead, Long> {
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId")
    fun findByCompanyId(companyId: Long): List<Lead>
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND status = :status")
    fun findByCompanyIdAndStatus(companyId: Long, status: LeadStatus): List<Lead>
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND assigned_user_id = :assignedUserId")
    fun findByCompanyIdAndAssignedUserId(companyId: Long, assignedUserId: Long): List<Lead>
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND email = :email")
    fun findByCompanyIdAndEmail(companyId: Long, email: String): Lead?
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId ORDER BY score DESC")
    fun findByCompanyIdOrderByScoreDesc(companyId: Long): List<Lead>
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND (first_name LIKE :searchPattern OR last_name LIKE :searchPattern OR email LIKE :searchPattern OR company LIKE :searchPattern)")
    fun findByCompanyIdAndSearchPattern(companyId: Long, searchPattern: String): List<Lead>
}

