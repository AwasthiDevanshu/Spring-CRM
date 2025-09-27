package com.crm.enterprise.repository

import com.crm.enterprise.entity.Lead
import com.crm.enterprise.entity.LeadStatus
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LeadRepository : CrudRepository<Lead, Long> {
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND deleted_at IS NULL")
    fun findByCompanyId(companyId: Long): List<Lead>
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND status = :status AND deleted_at IS NULL")
    fun findByCompanyIdAndStatus(companyId: Long, status: LeadStatus): List<Lead>
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND assigned_user_id = :assignedUserId AND deleted_at IS NULL")
    fun findByCompanyIdAndAssignedUserId(companyId: Long, assignedUserId: Long): List<Lead>
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND email = :email AND deleted_at IS NULL")
    fun findByCompanyIdAndEmail(companyId: Long, email: String): Lead?
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND deleted_at IS NULL ORDER BY score DESC")
    fun findByCompanyIdOrderByScoreDesc(companyId: Long): List<Lead>
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND deleted_at IS NULL AND (first_name LIKE :searchPattern OR last_name LIKE :searchPattern OR email LIKE :searchPattern OR company LIKE :searchPattern)")
    fun findByCompanyIdAndSearchPattern(companyId: Long, searchPattern: String): List<Lead>
    
    // Soft delete methods
    @Query("SELECT * FROM leads WHERE id = :id AND company_id = :companyId AND deleted_at IS NULL")
    fun findByIdAndCompanyIdAndNotDeleted(id: Long, companyId: Long): Lead?
    
    @Query("SELECT * FROM leads WHERE company_id = :companyId AND deleted_at IS NOT NULL")
    fun findDeletedByCompanyId(companyId: Long): List<Lead>
    
    @Query("UPDATE leads SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id AND company_id = :companyId")
    fun softDeleteByIdAndCompanyId(id: Long, companyId: Long): Int
    
    @Query("UPDATE leads SET deleted_at = NULL WHERE id = :id AND company_id = :companyId")
    fun restoreByIdAndCompanyId(id: Long, companyId: Long): Int
}

